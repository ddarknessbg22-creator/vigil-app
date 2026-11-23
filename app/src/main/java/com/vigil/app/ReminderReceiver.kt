package com.vigil.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import com.vigil.app.data.Quotes
import com.vigil.app.data.datastore.Options
import com.vigil.app.ui.main.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.*

class ReminderReceiver:BroadcastReceiver(){
    override fun onReceive(context:Context,intent:Intent){
        val pending=goAsync()
        CoroutineScope(Dispatchers.IO).launch { try {
            val o=VigilApplication.from(context).preferences.options.first();if(!o.reminder)return@launch
            schedule(context,o);if(intent.action!=ACTION)return@launch
            if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return@launch
            val manager=context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel("gentle","Daily reminder",NotificationManager.IMPORTANCE_LOW))
            val text=if(o.quoteNotice) Quotes.featured(Quotes.load(context),LocalDateTime.now(),o.category)?.text else null
            val content=text?:"A quiet moment for yourself, whenever you’re ready."
            val open=PendingIntent.getActivity(context,1,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            manager.notify(14,Notification.Builder(context,"gentle").setSmallIcon(R.drawable.ic_launcher).setContentTitle("VIGIL").setContentText(content).setStyle(Notification.BigTextStyle().bigText(content)).setVisibility(Notification.VISIBILITY_PRIVATE).setContentIntent(open).setAutoCancel(true).build())
        } finally { pending.finish() } }
    }
    companion object {
        const val ACTION="com.vigil.app.REMIND"
        fun schedule(c:Context,o:Options){
            val alarms=c.getSystemService(AlarmManager::class.java);val target=PendingIntent.getBroadcast(c,14,Intent(c,ReminderReceiver::class.java).setAction(ACTION),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            alarms.cancel(target);if(!o.reminder)return
            val now=ZonedDateTime.now();var next=now.toLocalDate().atTime(o.minute/60,o.minute%60).atZone(now.zone);if(!next.isAfter(now))next=next.plusDays(1)
            alarms.set(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),target)
        }
    }
}
