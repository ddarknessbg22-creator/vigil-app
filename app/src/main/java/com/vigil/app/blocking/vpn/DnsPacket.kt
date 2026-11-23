package com.vigil.app.blocking.vpn

import java.nio.ByteBuffer

/**
 * DNS wire-format helpers for the tun interface.
 *
 * A tun device hands us complete IPv4/IPv6 packets (IP header included, no
 * Ethernet header). We parse the IP and UDP headers to find the DNS payload,
 * and we rebuild those headers when we answer a query ourselves.
 *
 * Scope: UDP/53 only. Anything else is returned as null and the caller simply
 * drops the packet, which is why a parse failure must never happen for the
 * traffic we route into the tunnel.
 */
object DnsPacket {

    /** A parsed DNS question plus everything needed to answer it. */
    class Query(
        val host: String,
        val id: Int,
        val question: ByteArray,
        val payload: ByteArray,
        val version: Int,
        val srcIp: ByteArray,
        val dstIp: ByteArray,
        val srcPort: Int,
        val dstPort: Int
    )

    private const val IPPROTO_UDP = 17

    /** Returns null when the packet is not a UDP/53 query we understand. */
    fun parse(packet: ByteArray): Query? {
        if (packet.size < 28) return null
        val version = packet[0].toInt() shr 4 and 0x0F

        val headerLen: Int
        val protocol: Int
        val srcIp: ByteArray
        val dstIp: ByteArray
        when (version) {
            4 -> {
                val ihl = (packet[0].toInt() and 0x0F) * 4
                if (ihl < 20 || packet.size < ihl + 8) return null
                if (packet[9].toInt() and 0xFF != IPPROTO_UDP) return null
                headerLen = ihl
                protocol = packet[9].toInt() and 0xFF
                srcIp = packet.copyOfRange(12, 16)
                dstIp = packet.copyOfRange(16, 20)
            }

            6 -> {
                if (packet.size < 48) return null
                // Extension headers are not handled; those queries are left
                // to bypass rather than being mangled by a partial answer.
                if (packet[6].toInt() and 0xFF != IPPROTO_UDP) return null
                headerLen = 40
                protocol = packet[6].toInt() and 0xFF
                srcIp = packet.copyOfRange(8, 24)
                dstIp = packet.copyOfRange(24, 40)
            }

            else -> return null
        }
        if (protocol != IPPROTO_UDP) return null
        val udpOffset = headerLen
        val srcPort = u16(packet, udpOffset)
        val dstPort = u16(packet, udpOffset + 2)
        if (dstPort != 53 || srcPort == 0) return null

        val payloadOffset = udpOffset + 8
        if (packet.size <= payloadOffset + 12) return null
        val payload = packet.copyOfRange(payloadOffset, packet.size)

        // DNS transaction id and the question section.
        val id = u16(packet, payloadOffset)
        val qdCount = u16(packet, payloadOffset + 4)
        if (qdCount < 1) return null
        val flags = u16(packet, payloadOffset + 2)
        if (flags and 0x8000 != 0) return null // already a response

        var cursor = payloadOffset + 12
        val name = StringBuilder()
        var guard = 0
        while (cursor < packet.size && guard++ < 128) {
            val length = packet[cursor].toInt() and 0xFF
            if (length == 0) { cursor++; break }
            if (length and 0xC0 != 0) return null // compression inside a question
            if (cursor + 1 + length > packet.size) return null
            if (name.isNotEmpty()) name.append('.')
            for (i in 1..length) name.append((packet[cursor + i].toInt() and 0xFF).toChar())
            cursor += length + 1
        }
        val host = name.toString().trimEnd('.').lowercase()
        if (host.isEmpty()) return null

        // Question = QNAME + QTYPE(2) + QCLASS(2)
        val questionEnd = (cursor + 4).coerceAtMost(packet.size)
        val question = packet.copyOfRange(payloadOffset + 12, questionEnd)

        return Query(
            host = host,
            id = id,
            question = question,
            payload = payload,
            version = version,
            srcIp = srcIp,
            dstIp = dstIp,
            srcPort = srcPort,
            dstPort = dstPort
        )
    }

    /** NXDOMAIN answer, wrapped in a fresh IP/UDP header. */
    fun nxDomain(query: Query): ByteArray {
        val rcodeFlags = 0x8183 // response | RD | RA | NXDOMAIN
        val dns = buildDns(query, rcodeFlags, 0, 0)
        return wrap(query, dns)
    }

    /**
     * Wraps an upstream DNS answer (raw DNS bytes) in IP/UDP headers.
     * Returns null if the payload cannot be used, so the caller can drop it.
     */
    fun wrap(query: Query, dnsPayload: ByteArray): ByteArray? {
        if (dnsPayload.size > 8 + 65535) return null
        return when (query.version) {
            6 -> wrapV6(query, dnsPayload)
            else -> wrapV4(query, dnsPayload)
        }
    }

    private fun wrapV4(query: Query, dns: ByteArray): ByteArray {
        val udpLen = 8 + dns.size
        val total = 20 + udpLen
        val packet = ByteArray(total)

        // IPv4 header
        packet[0] = 0x45
        packet[1] = 0
        putU16(packet, 2, total)
        putU16(packet, 4, (System.nanoTime() and 0xFFFF).toInt())
        putU16(packet, 6, 0)
        packet[8] = 64
        packet[9] = IPPROTO_UDP.toByte()
        putU16(packet, 10, 0)
        query.srcIp.copyInto(packet, 12, 0, 4)   // source = the resolver
        query.dstIp.copyInto(packet, 16, 0, 4)   // destination = the client
        putU16(packet, 10, checksum(packet, 0, 20))

        // UDP header
        putU16(packet, 20, query.dstPort)
        putU16(packet, 22, query.srcPort)
        putU16(packet, 24, udpLen)
        putU16(packet, 26, 0) // checksum 0 is valid for IPv4 UDP

        dns.copyInto(packet, 28, 0, dns.size)
        return packet
    }

    private fun wrapV6(query: Query, dns: ByteArray): ByteArray {
        val udpLen = 8 + dns.size
        val packet = ByteArray(40 + udpLen)

        packet[0] = 0x60
        putU16(packet, 4, udpLen)
        packet[6] = IPPROTO_UDP.toByte()
        packet[7] = 64
        query.srcIp.copyInto(packet, 8, 0, 16)
        query.dstIp.copyInto(packet, 24, 0, 16)

        val udpOffset = 40
        putU16(packet, udpOffset, query.dstPort)
        putU16(packet, udpOffset + 2, query.srcPort)
        putU16(packet, udpOffset + 4, udpLen)
        putU16(packet, udpOffset + 6, 0)

        dns.copyInto(packet, udpOffset + 8, 0, dns.size)

        // UDP checksum is mandatory for IPv6 and covers a pseudo header.
        val pseudo = ByteArray(40 + udpLen)
        query.srcIp.copyInto(pseudo, 0, 0, 16)
        query.dstIp.copyInto(pseudo, 16, 0, 16)
        pseudo[32] = 0
        pseudo[33] = 0
        pseudo[34] = (udpLen shr 8).toByte()
        pseudo[35] = (udpLen and 0xFF).toByte()
        pseudo[36] = 0
        pseudo[37] = 0
        pseudo[38] = 0
        pseudo[39] = IPPROTO_UDP.toByte()
        packet.copyInto(pseudo, 40, udpOffset, udpOffset + udpLen)
        val sum = checksum(pseudo, 0, pseudo.size)
        putU16(packet, udpOffset + 6, if (sum == 0) 0xFFFF else sum)
        return packet
    }

    private fun buildDns(query: Query, flags: Int, answers: Int, authority: Int): ByteArray {
        val buffer = ByteBuffer.allocate(12 + query.question.size)
        buffer.putShort(query.id.toShort())
        buffer.putShort(flags.toShort())
        buffer.putShort(1)            // QDCOUNT
        buffer.putShort(answers.toShort())
        buffer.putShort(authority.toShort())
        buffer.putShort(0)            // ARCOUNT
        buffer.put(query.question)
        return buffer.array()
    }

    private fun u16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

    private fun putU16(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value shr 8 and 0xFF).toByte()
        data[offset + 1] = (value and 0xFF).toByte()
    }

    /** Standard internet 16-bit one's complement checksum. */
    private fun checksum(data: ByteArray, start: Int, length: Int): Int {
        var sum = 0L
        var i = start
        val end = start + length
        while (i + 1 < end) {
            sum += u16(data, i)
            i += 2
        }
        if (i < end) sum += (data[i].toInt() and 0xFF) shl 8
        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        return (sum.inv() and 0xFFFF)
    }
}
