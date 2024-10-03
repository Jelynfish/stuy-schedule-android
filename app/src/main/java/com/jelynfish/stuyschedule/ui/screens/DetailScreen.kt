package com.jelynfish.stuyschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jelynfish.stuyschedule.R
import com.jelynfish.stuyschedule.api.Period
import com.jelynfish.stuyschedule.ui.UIState
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun DetailScreen(
    uiState: UIState,
    getPeriod: (Calendar) -> Period,
    refreshSchedule: () -> Unit
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    val currentPeriod = getPeriod(currentTime)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Wait for 1 second
            currentTime = Calendar.getInstance() // Update the current time
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text("This is the detail screen.")
            Text(
                "The time is: ${currentTime.get(Calendar.HOUR_OF_DAY)}:${currentTime.get(Calendar.MINUTE)}:${
                    currentTime.get(
                        Calendar.SECOND
                    )
                }"
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.weight(8f)
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp)

        ) {
            uiState.todaySchedule.let {today ->
                today.bell?.let {bell ->
                    bell.schedule.forEach {
                        if (!it.name.contains("Before") && !it.name.contains("After")) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.displayLarge,
                                    color = if (it.name == currentPeriod.name) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${it.startTime} - ${it.endTime}",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = if (it.name == currentPeriod.name) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        //Refresh Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Button(onClick = { refreshSchedule() }) {
                Text(stringResource(id = R.string.refresh))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailPreview() {
    DetailScreen(
        uiState = UIState(),
        getPeriod = { _ -> Period("No current period", "0:00", 1440)},
        refreshSchedule = {}
    )
}