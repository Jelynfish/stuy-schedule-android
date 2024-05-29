package com.jelynfish.stuyschedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ScheduleRepo
import com.jelynfish.stuyschedule.widget.DailyUpdateWorker
import java.util.Calendar

class ScheduleWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        updateWidgets(context, appWidgetManager, appWidgetIds)

        DailyUpdateWorker.scheduleDailyWork(context)
        schedulePerMinuteUpdates(context)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_TIME) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds =  appWidgetManager.getAppWidgetIds(ComponentName(context, ScheduleWidget::class.java))
            updateWidgets(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        private const val ACTION_UPDATE_TIME = "com.example.UPDATE_TIME"
        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val repo = ScheduleRepo(context, ApiClient.api)
            val schedule = repo.getWeeklySchedule()
            val todaySchedule = repo.getTodaySchedule(schedule)

            // Determine the current period
            val currTime = Calendar.getInstance()
            val currentPeriod = todaySchedule.let {  repo.getPeriod(it, currTime) }
//            Text("Today is ${uiState.todaySchedule?.day}")
//            Text("The time is: ${currentTime.get(Calendar.HOUR_OF_DAY)}:${currentTime.get(Calendar.MINUTE)}:${currentTime.get(Calendar.SECOND)}")
//            Text(currentPeriod.name)
//            Text("${timeElapsed}", color = Color.Green)
//            Text("${currentPeriod.duration - timeElapsed}", color = Color.Red)

            // Update the widget with the current period
            appWidgetIds.forEach { appWidgetId ->
                val views = RemoteViews(context.packageName, R.layout.schedule_widget)
                views.setTextViewText(
                    R.id.appwidget_text,
                    currentPeriod.name
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun schedulePerMinuteUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleWidget::class.java).apply {
                action = ACTION_UPDATE_TIME
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent)
        }
    }
}