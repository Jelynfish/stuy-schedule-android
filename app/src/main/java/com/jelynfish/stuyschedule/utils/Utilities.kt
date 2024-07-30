package com.jelynfish.stuyschedule.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getTodayDate(): String {
    val currTime = Calendar.getInstance()
    return SimpleDateFormat("MMMM d, yyyy", Locale.US).format(currTime.time)
}

fun getTomorrowDate(): String {
    val currTime = Calendar.getInstance()
    currTime.add(Calendar.DAY_OF_YEAR, 1)
    return SimpleDateFormat("MMMM d, yyyy", Locale.US).format(currTime.time)
}

fun aOrAn(preceding: String): String {
    val string = preceding.uppercase()
    string[0].let {
        if (it == 'A' || it == 'E' || it == 'I' || it == 'O' || it == 'U') return "an"
        return "a"
    }
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
