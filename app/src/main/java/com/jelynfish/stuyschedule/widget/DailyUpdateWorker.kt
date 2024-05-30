package com.jelynfish.stuyschedule.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jelynfish.stuyschedule.ScheduleWidget
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ScheduleRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DailyUpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        updateTodaySchedule(applicationContext)

        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, ScheduleWidget::class.java))
        ScheduleWidget.updateWidgets(applicationContext, appWidgetManager, appWidgetIds)

        return Result.success()
    }
    private fun updateTodaySchedule(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val repo = ScheduleRepo(context, ApiClient.api)
            val schedule = repo.getWeeklySchedule()

            val todaySchedule = repo.getTodaySchedule(schedule)

            todaySchedule.let {
                Log.d("DailyUpdateWorker", "Today's schedule updated: $it")
            }
        }
    }

    companion object {
        fun scheduleDailyWork(context: Context) {
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyUpdateWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "dailyUpdate",
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWorkRequest
            )
        }
    }

}