package com.vigil.app.blocking.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.vigil.app.AppServices
import com.vigil.app.R
import com.vigil.app.blocking.BlocklistManager
import com.vigil.app.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Local DNS filter running inside a VpnService tunnel.
 *
 * The tunnel carries only DNS traffic. Blocked names receive NXDOMAIN;
 * everything else is relayed to an upstream resolver.
 *
 * Limitation (stated to the user): HTTPS traffic resolved by a hard-coded
 * resolver inside another app, or via private DNS set outside this tunnel,
 * is not covered by DNS filtering.
 */
class VigilVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null
    private var worker: kotlinx.coroutines.Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val relayPool = Executors.newFixedThreadPool(4)
    private lateinit var blocklist: BlocklistManager

    override fun onCreate() {
        super.onCreate()
        blocklist = AppServices.blocklist(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopFiltering()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification(getString(R.string.protection_short_active)))
        if (running.value) return START_STICKY
        startFiltering()
        return START_STICKY
    }

    private fun startFiltering() {
        worker = scope.launch {
            try {
                blocklist.init()
                val descriptor = buildTunnel() ?: run {
                    Log.w(TAG, "VPN permission missing")
                    running.value = false
                    stopSelf()
                    return@launch
                }
                tun = descriptor
                running.value = true
                AppServices.app(this@VigilVpnService).repository.logEvent("protection_enabled", "DNS filter started")
                readLoop(descriptor)
            } catch (t: Throwable) {
                Log.e(TAG, "Filtering failed", t)
                running.value = false
                AppServices.app(this@VigilVpnService).repository.logEvent("protection_error", t.message ?: "unknown")
                stopFiltering()
            }
        }
    }

    private fun buildTunnel(): ParcelFileDescriptor? {
        val builder = Builder()
            .setSession("VIGIL")
            .setMtu(1500)
            .addAddress(TUN_V4_ADDR, 24)
            .addDnsServer(DNS_V4)
            .addRoute(DNS_V4_SUBNET, 24)
            .setBlocking(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setUnderlyingNetworks(null)
        }

        try {
            builder.addAddress(TUN_V6_ADDR, 64)
            builder.addDnsServer(DNS_V6)
            builder.addRoute(DNS_V6_SUBNET, 64)
        } catch (t: Throwable) {
            Log.w(TAG, "IPv6 tunnel unavailable", t)
        }

        try {
            builder.addDisallowedApplication(packageName)
        } catch (_: Throwable) {
        }

        return builder.establish()
    }

    private fun readLoop(descriptor: ParcelFileDescriptor) {
        val input = FileInputStream(descriptor.fileDescriptor)
        val output = FileOutputStream(descriptor.fileDescriptor)
        val buffer = ByteArray(4096)

        while (scope.isActive && running.value) {
            val length = try {
                input.read(buffer)
            } catch (t: Throwable) {
                if (running.value) Log.w(TAG, "read failed", t)
                break
            }
            if (length <= 0) continue
            val packet = buffer.copyOf(length)
            val query = DnsPacket.parseQuery(packet) ?: continue

            relayPool.execute {
                val response = try {
                    if (blocklist.isBlocked(query.host)) {
                        blockedCount.incrementAndGet()
                        scope.launch {
                            AppServices.app(this@VigilVpnService).repository.logBlocked(query.host)
                        }
                        DnsPacket.nxDomain(packet)
                    } else {
                        relay(packet)
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "dns handling failed for ${query.host}", t)
                    null
                }

                if (response != null) {
                    try {
                        output.write(response)
                    } catch (t: Throwable) {
                        Log.w(TAG, "write failed", t)
                    }
                }
            }
        }

        runCatching { input.close() }
        runCatching { output.close() }
    }

    private fun relay(packet: ByteArray): ByteArray? {
        DatagramSocket().use { socket ->
            try {
                protect(socket)
            } catch (_: Throwable) {
            }
            socket.soTimeout = 4000
            val target = InetSocketAddress(InetAddress.getByName(upstream()), 53)
            socket.send(DatagramPacket(packet, packet.size, target))
            val reply = ByteArray(4096)
            val incoming = DatagramPacket(reply, reply.size)
            socket.receive(incoming)
            return reply.copyOf(incoming.length)
        }
    }

    private fun upstream(): String =
        if (fallbackUpstream) UPSTREAM_FALLBACK else UPSTREAM_PRIMARY

    private fun stopFiltering() {
        running.value = false
        worker?.cancel()
        runCatching { tun?.close() }
        tun = null
        relayPool.shutdownNow()
        scope.launch {
            try {
                AppServices.app(this@VigilVpnService).repository.logEvent("protection_disabled", "DNS filter stopped")
            } catch (_: Exception) {
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        stopFiltering()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopFiltering()
        super.onDestroy()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_protection),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_protection_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val TAG = "VigilVpn"
        private const val CHANNEL_ID = "vigil_protection"
        private const val NOTIF_ID = 1001

        const val ACTION_STOP = "com.vigil.app.STOP_FILTER"
        private const val TUN_V4_ADDR = "10.111.222.1"
        private const val DNS_V4 = "10.111.222.2"
        private const val DNS_V4_SUBNET = "10.111.222.0"
        private const val TUN_V6_ADDR = "fd00:7669:6769::1"
        private const val DNS_V6 = "fd00:7669:6769::2"
        private const val DNS_V6_SUBNET = "fd00:7669:6769::"
        private const val UPSTREAM_PRIMARY = "1.1.1.1"
        private const val UPSTREAM_FALLBACK = "9.9.9.9"

        @Volatile var fallbackUpstream: Boolean = false

        private val running = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = running

        val blockedCount = AtomicLong(0)

        fun start(context: Context) {
            val intent = Intent(context, VigilVpnService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VigilVpnService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
