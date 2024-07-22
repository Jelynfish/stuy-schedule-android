package com.jelynfish.stuyschedule.api

data class ApiData(
    val scheduleType: String,
    val days: List<Day>,
)

data class Day(
//    Month Day, Year format
//    There is always a day
    val day: String,

//    The bell schedule of the day
//    Will be NULL if there is no school
    val bell: BellSchedule?,

//    A and B days, etc.
    val block: String?,

//    Today's testing
//    Not used
    val testing: String?,

//    The announcement for the day
//    Will be NULL if there is no announcement
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
    var endTime: String = ""
)