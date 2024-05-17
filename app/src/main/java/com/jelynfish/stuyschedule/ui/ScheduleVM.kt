package com.jelynfish.stuyschedule.ui

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.jelynfish.stuyschedule.api.Api
import com.jelynfish.stuyschedule.api.ApiData
import com.jelynfish.stuyschedule.api.Day
import com.jelynfish.stuyschedule.api.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleVM(app: Application) : AndroidViewModel(app) {
    companion object {
        val api: Api by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api-stuyschedule.vercel.app/api/weekly-schedule")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return@lazy retrofit.create(Api::class.java)
        }
    }

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    init {
        getWeeklyFromJSON()
    }

    fun refreshSchedule() {
        getWeeklySchedule()
    }

    fun whatPeriod(currTime: Calendar): Period {
        _uiState.value.todaySchedule?.let { today ->
            today.bell?.let { bell ->
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
        }
        return Period(
            name = "No matching period",
            startTime = "0:00",
            duration = 0
        )
    }

    fun whatEndTime(period: Period): Date {
        @SuppressLint("SimpleDateFormat")
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.parse(period.endTime)!!
    }

    private fun getWeeklySchedule() {
        _uiState.value = _uiState.value.copy(loading = true)
        val call: Call<ApiData> = api.getData()
        call.enqueue(object : Callback<ApiData> {
            override fun onFailure(p0: Call<ApiData>, p1: Throwable) {
                _uiState.value = _uiState.value.copy(loading = false)
                Log.d("ScheduleVM", "Failed to fetch weekly schedule.")
            }

            override fun onResponse(p0: Call<ApiData>, p1: Response<ApiData>) {
                val schedule = p1.body()

                schedule?.let {s ->
                    s.days.forEach {day ->
                        day.bell?.let { bellSchedule ->
                            bellSchedule.schedule.forEach {
                                it.endTime = getEndTime(it.startTime, it.duration)
                            }
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    schedule = schedule,
                    loading = false,
                )
                getTodaySchedule()
            }
        })
    }

    private fun getWeeklyFromJSON() {
        parseLocalSchedule("local.json")
        getTodaySchedule()
    }

    private fun getTodaySchedule() {
        val schedule = _uiState.value.schedule
        schedule?.let {
            val todaySchedule = it.days.firstOrNull {
                day -> day.day == getTodayDate()
            }
            _uiState.value = _uiState.value.copy(
                todaySchedule = todaySchedule
            )
        }
    }

    private fun parseLocalSchedule(file: String) {
        val jsonString = getApplication<Application>()
            .assets.open(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val schedule = Gson().fromJson(jsonString, ApiData::class.java)
        schedule.days.forEach {day ->
            day.bell?.let { bellSchedule ->
                bellSchedule.schedule.forEach {
                    it.endTime = getEndTime(it.startTime, it.duration)
                }
            }
        }
        _uiState.value = _uiState.value.copy(
            schedule = schedule
        )
    }

    private fun getTodayDate(): String {
        val currTime = Calendar.getInstance()
        val today = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(currTime.time)
        Log.d("ScheduleVM", today)
        return today
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

}

data class UIState(
    val schedule: ApiData? = null,
    val todaySchedule: Day? = null,
    val mode: Int = 0, //light mode = 0, dark mode = 1
    val loading: Boolean = false,
)