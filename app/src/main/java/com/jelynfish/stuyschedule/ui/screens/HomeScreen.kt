package com.jelynfish.stuyschedule.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jelynfish.stuyschedule.R
import com.jelynfish.stuyschedule.api.Period
import com.jelynfish.stuyschedule.ui.UIState
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    uiState: UIState,
    getPeriod: (Calendar) -> Period,
    refreshSchedule: () -> Unit
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var currentPeriod = getPeriod(currentTime)
    var timeElapsed = getTimeElapsed(currentTime, currentPeriod.startTime)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Wait for 1 second
            currentTime = Calendar.getInstance() // Update the current time
            timeElapsed = getTimeElapsed(currentTime, currentPeriod.startTime)
            if (currentPeriod.duration <= timeElapsed) {
                currentPeriod = getPeriod(currentTime)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("This is the home screen")
        Text("Today is ${uiState.todaySchedule?.day}")
        Text("The time is: ${currentTime.get(Calendar.HOUR_OF_DAY)}:${currentTime.get(Calendar.MINUTE)}:${currentTime.get(Calendar.SECOND)}")
        Text(currentPeriod.name)
        Text("${timeElapsed}", color = Color.Green)
        Text("${currentPeriod.duration - timeElapsed}", color = Color.Red)
        Button(onClick = { refreshSchedule() }) {
            Text(stringResource(id = R.string.refresh))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(
        uiState = UIState(),
        getPeriod = { _ -> Period("No current period", "0:00", 1440)},
        refreshSchedule = {}
    )
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