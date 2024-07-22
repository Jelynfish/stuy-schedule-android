package com.jelynfish.stuyschedule.api

import android.content.Context
import com.google.gson.Gson
import com.jelynfish.stuyschedule.utils.getEndTime
import com.jelynfish.stuyschedule.utils.getTodayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class ScheduleRepo(private val context: Context, private val api: ApiService) {

//    Gets weekly schedule from the local schedule if the schedule is already fetched,
//    Otherwise, refreshes from the API.
    suspend fun getWeeklySchedule(): ApiData {
        return withContext(Dispatchers.IO) {
            if (doesLocalExist())
                parseLocalSchedule()
            else refreshWeeklySchedule()
        }
    }

//    Gets weekly schedule from the API
//    Saves the schedule to a local JSON file
    suspend fun refreshWeeklySchedule(): ApiData {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getData().execute()
                if (response.isSuccessful) {
                    val schedule = response.body()
                    if (schedule != null) {
                        schedule.days.forEach { day ->
                            day.bell?.let { bellSchedule ->
                                bellSchedule.schedule.forEach {
                                    it.endTime = getEndTime(it.startTime, it.duration)
                                }
                            }
                        }
                        saveScheduleToJson(schedule)
                        return@withContext schedule
                    } else {
                        throw Exception("Failed to fetch weekly schedule")
                    }
                } else {
                    throw Exception("Unsuccessful response: ${response.code()}")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

//    Saves fetched schedule to JSON file
    private fun saveScheduleToJson(schedule: ApiData) {
        val jsonString = Gson().toJson(schedule)
        val file = File(context.filesDir, "schedule_data.json")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonString.toByteArray())
        }
    }

//    Parses the local JSON schedule data
    private fun parseLocalSchedule(): ApiData {
        val file = File(context.filesDir, "schedule_data.json")
        val jsonString = file.readText()
        return Gson().fromJson(jsonString, ApiData::class.java)
    }

//    Checks if the local JSON schedule data exists
    private fun doesLocalExist(): Boolean {
        val file = File(context.filesDir, "schedule_data.json")
        return file.exists()
    }

//    From the schedule data, returns today's schedule data
    fun getTodaySchedule(schedule: ApiData): Day {
        val todayDate = getTodayDate()
        val todaySchedule = schedule.days.firstOrNull { day ->
            day.day == todayDate
        } ?: Day(getTodayDate(), null, null, null, null)
        return todaySchedule
    }

//    Returns the current period given a schedule and a time.
    fun getCurrentPeriod(todaySchedule: Day, currTime: Calendar): Period {
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

                if (currTime >= tempStart && currTime < tempEnd) {
                    return it
                }
            }
        }

        return Period(
            name = "No matching period",
            startTime = "0:00",
            duration = 1440
        )
    }
}