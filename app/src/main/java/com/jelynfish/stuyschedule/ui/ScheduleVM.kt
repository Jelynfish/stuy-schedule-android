package com.jelynfish.stuyschedule.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.jelynfish.stuyschedule.api.Api
import com.jelynfish.stuyschedule.api.ApiData
import com.jelynfish.stuyschedule.api.Day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
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
        parseLocalSchedule("local.json")
        getTodaysDate()
    }
    private fun parseLocalSchedule(file: String) {
        val jsonString = getApplication<Application>()
            .assets.open(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val schedule = Gson().fromJson(jsonString, ApiData::class.java)
        _uiState.value = _uiState.value.copy(
            schedule = schedule
        )
    }

    private fun getTodaysDate() {
        val currTime = Calendar.getInstance()
        val today = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(currTime.time)
        _uiState.value = _uiState.value.copy(
            currentDay = today
        )
        Log.d("ScheduleVM", today)
    }
}

data class UIState(
    val schedule: ApiData? = null,
    val currentDay: String = "",
    val mode: Int = 0 //light mode = 0, dark mode = 1
)