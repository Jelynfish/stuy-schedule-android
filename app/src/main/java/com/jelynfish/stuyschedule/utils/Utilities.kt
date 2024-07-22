package com.jelynfish.stuyschedule.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getTodayDate(): String {
    val currTime = Calendar.getInstance()
    return SimpleDateFormat("MMMM d, yyyy", Locale.US).format(currTime.time)
}
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

fun getEndTime(startTime: String, duration: Int): String {
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

// Int function that returns
// -1 if the current time is BEFORE SCHOOL
// 0 if the current time is DURING SCHOOL
// 1 if the current time is AFTER SCHOOL
const val BEFORE_SCHOOL = -1
const val DURING_SCHOOL = 0
const val AFTER_SCHOOL = 1

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

    return if (currTime.before(beforeSchool)) BEFORE_SCHOOL
    else if (currTime.after(afterSchool)) DURING_SCHOOL
    else AFTER_SCHOOL
}
