package com.jelynfish.stuyscheduleapp.api

data class ApiData(
    val scheduleType: String,
    val days: List<Day>,
)

data class Day(
    val day: String,
    val bell: BellSchedule?,
    val block: String?,
    val testing: String?,
    val announcement: String?
)

data class BellSchedule(
    val scheduleType: String,
    val scheduleName: String,
    val schedule: List<Period>
)

data class Period(
    val name: String,
    val startTime: String,
    val duration: Int,
)
