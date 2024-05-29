package com.jelynfish.stuyschedule.widget

import java.util.Calendar

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