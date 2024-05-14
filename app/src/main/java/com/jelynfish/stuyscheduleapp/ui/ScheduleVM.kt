package com.jelynfish.stuyscheduleapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.jelynfish.stuyscheduleapp.api.Api
import com.jelynfish.stuyscheduleapp.api.ApiData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
}

data class UIState(
    val schedule: ApiData? = null,
    val mode: Int = 0 //light mode = 0, dark mode = 1
)