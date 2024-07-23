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
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let { Log.d("ScheduleWidget", "Received intent action: $it") }
        if (intent.action == UPDATE_WIDGET_ACTION || intent.action == Intent.ACTION_BOOT_COMPLETED) {
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

                // Determine the layout based on the hour
                val hour = currTime.get(Calendar.HOUR_OF_DAY)

                val layoutId =
                    if (todaySchedule.bell == null) { // No School
                        R.layout.no_school_layout
                    } else {
                        if (hour < 7) {
                            R.layout.before_school_layout
                        } else if (hour > 15) {
                            R.layout.after_school_layout
                        } else {
                            R.layout.schedule_widget
                        }
                    }

                // Update the widget with the current period
                appWidgetIds.forEach { appWidgetId ->
                    val views = RemoteViews(context.packageName, layoutId)
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
                Log.d("ScheduleWidget", "Updated the widget.")

                scheduleNextUpdate(context, layoutId)
            }
        }

        fun scheduleNextUpdate(context: Context, layoutId: Int) {
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

            when (layoutId) {
                R.layout.no_school_layout -> Log.d("ScheduleWidget", "There is no school today. Stopping per minute update.")
                R.layout.before_school_layout -> Log.d("ScheduleWidget", "Time is before school. Stopping per minute update..")
                R.layout.after_school_layout -> Log.d("ScheduleWidget", "It is after school hours. Stopping per minute update.")
                else -> { // During school
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