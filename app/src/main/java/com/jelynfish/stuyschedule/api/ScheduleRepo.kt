package com.jelynfish.stuyschedule.api

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleRepo(private val context: Context, private val api: ApiService) {
    fun getWeeklySchedule(): ApiData {
        try {
            val response = api.getData().execute()
            if (response.isSuccessful) {
                val schedule = response.body()
                schedule?.let {
                    it.days.forEach { day ->
                        day.bell?.let { bellSchedule ->
                            bellSchedule.schedule.forEach {
                                it.endTime = getEndTime(it.startTime, it.duration)
                            }
                        }
                    }
                    saveScheduleToJson(schedule)
                    return schedule
                } ?: throw Exception("Failed to fetch weekly schedule")
            }
        } catch (e: Exception) {
            throw e
        }
        throw Exception("Empty response body")
    }
    private fun saveScheduleToJson(schedule: ApiData) {
        val jsonString = Gson().toJson(schedule)
        val file = File(context.filesDir, "schedule_data.json")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonString.toByteArray())
        }
    }

    fun parseLocalSchedule(): ApiData {
        val file = File(context.filesDir, "schedule_data.json")
        val jsonString = file.readText()
        return Gson().fromJson(jsonString, ApiData::class.java)
    }

    fun doesLocalExist(): Boolean {
        val file = File(context.filesDir, "schedule_data.json")
        return file.exists()
    }

    fun getTodaySchedule(schedule: ApiData): Day {
        val todayDate = getTodayDate()
        val todaySchedule = schedule.days.firstOrNull {
            day -> day.day == todayDate
        } ?: Day(getTodayDate(), null, null, null, null)
        return todaySchedule
    }

    private fun getTodayDate(): String {
        val currTime = Calendar.getInstance()
        return SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(currTime.time)
    }

    private fun getEndTime(startTime: String, duration: Int): String {
        val parts = startTime.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()

        var newHours = hours
        var newMinutes = minutes + duration

        if (newMinutes >= 60) {
            newHours += newMinutes / 60
            newMinutes %= 60
        }

        return String.format("%02d:%02d", newHours, newMinutes)
    }

    fun getPeriod(todaySchedule: Day, currTime: Calendar): Period {
        todaySchedule.bell?.let { bell ->
            bell.schedule.forEach {
                val startTimeComponents = it.startTime.split(":")
                val endTimeComponents = it.endTime.split(":")

                val startHour = startTimeComponents[0].toInt()
                val startMinute = startTimeComponents[1].toInt()

                val endHour = endTimeComponents[0].toInt()
                val endMinute = endTimeComponents[1].toInt()

                val tempStart = Calendar.getInstance().apply {
                    timeInMillis = currTime.timeInMillis
                    set(Calendar.HOUR_OF_DAY, startHour)
                    set(Calendar.MINUTE, startMinute)
                    set(Calendar.SECOND, 0)
                }

                val tempEnd = Calendar.getInstance().apply {
                    timeInMillis = currTime.timeInMillis
                    set(Calendar.HOUR_OF_DAY, endHour)
                    set(Calendar.MINUTE, endMinute)
                    set(Calendar.SECOND, 0)
                }

                if (currTime.after(tempStart) && currTime.before(tempEnd)) {
                    return it
                }
            }
        }

        return Period(
            name = "No matching period",
            startTime = "0:00",
            duration = 0
        )
    }
}