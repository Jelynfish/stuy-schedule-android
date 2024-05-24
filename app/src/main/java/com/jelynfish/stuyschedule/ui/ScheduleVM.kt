package com.jelynfish.stuyschedule.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.jelynfish.stuyschedule.api.ApiClient
import com.jelynfish.stuyschedule.api.ApiData
import com.jelynfish.stuyschedule.api.Day
import com.jelynfish.stuyschedule.api.Period
import com.jelynfish.stuyschedule.api.ScheduleRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleVM(app: Application) : AndroidViewModel(app) {

    private val scheduleRepo = ScheduleRepo(app.applicationContext, ApiClient.api)

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    init {
        getWeekly()
    }

    private fun getWeekly() {
        if (scheduleRepo.doesLocalExist()) {
            getWeeklyFromJSON()
        } else {
            getWeeklyFromAPI()
        }
        getTodaySchedule()
    }

    fun refreshSchedule() {
        getWeeklyFromAPI()
    }

    private fun getWeeklyFromAPI() {
        val schedule = scheduleRepo.getWeeklySchedule()
        _uiState.value = _uiState.value.copy(schedule = schedule)

    }

    private fun getWeeklyFromJSON() {
        val schedule = scheduleRepo.parseLocalSchedule()
        _uiState.value = _uiState.value.copy(schedule = schedule)
    }

    private fun getTodaySchedule() {
        val schedule = _uiState.value.schedule
        val todaySchedule = scheduleRepo.getTodaySchedule(schedule)
        _uiState.value = _uiState.value.copy(
            todaySchedule = todaySchedule
        )
    }

    fun whatPeriod(currTime: Calendar): Period {
        val todaySchedule = _uiState.value.todaySchedule
        return scheduleRepo.getPeriod(todaySchedule, currTime)
    }
}

data class UIState(
    val schedule: ApiData = ApiData("", emptyList()),
    val todaySchedule: Day = Day("", null, null, null, null),
)