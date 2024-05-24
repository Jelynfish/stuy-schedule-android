package com.jelynfish.stuyschedule

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ScheduleRepo
import java.util.Calendar

class ScheduleWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val repo = ScheduleRepo(context, ApiClient.api)

            // Fetch the weekly schedule
            val schedule = if (repo.doesLocalExist()) {
                repo.parseLocalSchedule()
            } else {
                repo.getWeeklySchedule()
            }

            // Get today's schedule
            val todaySchedule = repo.getTodaySchedule(schedule)

            // Determine the current period
            val currTime = Calendar.getInstance()
            val currentPeriod = todaySchedule.let {  repo.getPeriod(it, currTime) }

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
    }
}