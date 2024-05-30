package com.jelynfish.stuyschedule.utils

import java.util.Calendar

fun getTimeElapsed(currentTime: Calendar, startTime: String): Long {
    val currentTimeMillis = currentTime.timeInMillis
    val startTimeMillis = parseStartTimeToMillis(startTime)
    val diffMillis = currentTimeMillis - startTimeMillis
    return (diffMillis / (1000 * 60))
}
fun parseStartTimeToMillis(startTime: String): Long {
    val (hours, minutes) = startTime.split(":")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hours.toInt())
    calendar.set(Calendar.MINUTE, minutes.toInt())
    calendar.set(Calendar.SECOND, 0)
    return calendar.timeInMillis
}

fun isSchoolHours(): Int {
    val currTime = Calendar.getInstance()
    val beforeSchool = Calendar.getInstance().apply {
        timeInMillis = currTime.timeInMillis
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 69)
    }

    val afterSchool = Calendar.getInstance().apply {
        timeInMillis = currTime.timeInMillis
        set(Calendar.HOUR_OF_DAY, 17)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    return if (currTime.before(beforeSchool)) -1
    else if (currTime.after(afterSchool)) 1
    else 0
}
