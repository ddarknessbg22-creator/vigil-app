package com.vigil.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.vigil.app.data.streakDays
import com.vigil.app.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home-screen widget showing the current clean-day count.
 *
 * Refreshes on the system schedule, at boot, and immediately after a check-in
 * or a new run starts. Tapping it opens the app.
 */
class StreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refresh(context)
    }

    override fun onEnabled(context: Context) = refresh(context)
    override fun onDisabled(context: Context) = Unit

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, StreakWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                try {
                    val db = VigilDatabase.getInstance(context)
                    val runs = withContext(Dispatchers.IO) { db.journal().runsOnce() }
                    val now = System.currentTimeMillis()
                    val active = runs.firstOrNull { it.end == null }
                    val days = active?.streakDays(now) ?: 0L
                    val best = runs.maxOfOrNull { it.streakDays(now) } ?: 0L

                    val open = PendingIntent.getActivity(
                        context, 21,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val views = RemoteViews(context.packageName, R.layout.widget_streak).apply {
                        setTextViewText(R.id.widget_number, days.toString())
                        setTextViewText(R.id.widget_label, if (days == 1L) "day clean" else "days clean")
                        setTextViewText(R.id.widget_best, "best $best")
                        setOnClickPendingIntent(R.id.widget_root, open)
                    }
                    manager.updateAppWidget(ids, views)
                } catch (_: Exception) {
                    // Widget stays as-is on failure; next scheduled update retries.
                }
            }
        }
    }
}
