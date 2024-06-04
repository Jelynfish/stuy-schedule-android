package com.jelynfish.stuyschedule

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.RemoteViews
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ScheduleRepo
import com.jelynfish.stuyschedule.utils.getTimeElapsed
import com.jelynfish.stuyschedule.widget.TimeTickService
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
//        schedulePerMinuteUpdates(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // Register service
//        val serviceIntent = Intent(context, TimeTickService::class.java)
//        context.startService(serviceIntent)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context)

        // Unregister service
//        val serviceIntent = Intent(context, TimeTickService::class.java)
//        context.stopService(serviceIntent)
    }
    companion object {
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
                val currentPeriod = todaySchedule.let { repo.getPeriod(it, currTime) }
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
            Log.d("ScheduleWidget", "Hello. I just updated the widget.")
        }
    }
}