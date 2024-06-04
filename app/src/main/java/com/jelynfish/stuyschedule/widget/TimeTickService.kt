package com.jelynfish.stuyschedule.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.jelynfish.stuyschedule.ScheduleWidget
import com.jelynfish.stuyschedule.ScheduleWidget.Companion.updateWidgets

class TimeTickService : Service() {

    private var timeTickReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        timeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Handle TIME_TICK event here
                // This method will be called every minute
                Log.d("TimeTickService", "Received TIME_TICK broadcast")
                // Trigger widget update here
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds =  appWidgetManager.getAppWidgetIds(ComponentName(context, ScheduleWidget::class.java))
                updateWidgets(context, appWidgetManager, appWidgetIds)
            }
        }

        // Register the BroadcastReceiver to receive TIME_TICK broadcasts
        registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when the service is destroyed
        timeTickReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun updateWidget(context: Context) {
        // Implement widget update logic here
    }
}