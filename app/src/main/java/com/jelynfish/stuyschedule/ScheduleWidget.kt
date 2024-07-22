package com.jelynfish.stuyschedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ScheduleRepo
import com.jelynfish.stuyschedule.utils.getTimeElapsed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        updateWidgets(context, appWidgetManager, appWidgetIds)
//        DailyUpdateWorker.scheduleDailyWork(context)
        scheduleNextUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
        // Register service
//        val serviceIntent = Intent(context, TimeTickService::class.java)
//        context.startService(serviceIntent)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context)
        cancelUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let { Log.d("ScheduleWidget", "Received intent action: $it") }
        if (intent.action == UPDATE_WIDGET_ACTION) {
            Log.d("ScheduleWidget", "I received a scheduled update.")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val UPDATE_WIDGET_ACTION = "com.jelynfish.stuyschedule.UPDATE_WIDGET"
        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                val repo = ScheduleRepo(context, ApiClient.api)
                val schedule = repo.getWeeklySchedule()
                val todaySchedule = repo.getTodaySchedule(schedule)

                // Determine the current period
                val currTime = Calendar.getInstance()
                val currentPeriod = todaySchedule.let { repo.getCurrentPeriod(it, currTime) }
                val timeElapsed = getTimeElapsed(currTime, currentPeriod.startTime)

                // Update the widget with the current period
                appWidgetIds.forEach { appWidgetId ->
                    val views = RemoteViews(context.packageName, R.layout.schedule_widget)
                    views.setTextViewText(R.id.curr_period, currentPeriod.name)
                    views.setTextViewText(R.id.time_into, timeElapsed.toString())
                    views.setTextViewText(
                        R.id.time_left,
                        (currentPeriod.duration - timeElapsed).toString()
                    )
                    todaySchedule.block?.let {
                        views.setTextViewText(R.id.curr_day, todaySchedule.block)
                    } ?: {
                        views.setTextViewText(
                            R.id.curr_day,
                            "No School"
                        )
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
            Log.d("ScheduleWidget", "Updated the widget.")
        }

        fun scheduleNextUpdate(context: Context) {
            val currTime = Calendar.getInstance()
            val beforeSchool = Calendar.getInstance().apply {
                timeInMillis = currTime.timeInMillis
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }
            val afterSchool = Calendar.getInstance().apply {
                timeInMillis = currTime.timeInMillis
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            Log.d("ScheduleWidget", "Scheduling next update")
            val intent = Intent(context, ScheduleWidget::class.java).apply {
                action = UPDATE_WIDGET_ACTION
            }
            Log.d("ScheduleWidget", "Creating PendingIntent")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (currTime < beforeSchool) Log.d("ScheduleWidget", "Time is before school. Stopping per minute update..")
            else if (currTime > afterSchool) Log.d("ScheduleWidget", "It is after school hours. Stopping per minute update.")
            else {
                val triggerAtMillis = SystemClock.elapsedRealtime() + 60000
                Log.d("ScheduleWidget", "Scheduling alarm at $triggerAtMillis")
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("ScheduleWidget", "Next update scheduled.")
            }
        }

        fun cancelUpdates(context: Context) {
            val intent = Intent(context, ScheduleWidget::class.java)
            intent.action = UPDATE_WIDGET_ACTION
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}