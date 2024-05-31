package com.jelynfish.stuyschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jelynfish.stuyschedule.R
import com.jelynfish.stuyschedule.api.Day
import com.jelynfish.stuyschedule.api.Period
import com.jelynfish.stuyschedule.ui.UIState
import com.jelynfish.stuyschedule.utils.getTimeElapsed
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
            .padding(12.dp)
    ) {
        //Announcement Box
        Box(modifier = Modifier.height(200.dp)) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.todaySchedule.announcement?.let {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.LightGray)
                            .padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Announcement",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center

                            )
                        }
                    }
                }
//        Text("This is the home screen")
//        Text("Today is ${uiState.todaySchedule.day}")
//        Text(
//            "The time is: ${currentTime.get(Calendar.HOUR_OF_DAY)}:${
//                currentTime.get(Calendar.MINUTE)
//            }:${currentTime.get(Calendar.SECOND)}"
//        )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Today's Schedule",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = uiState.todaySchedule.bell?.scheduleName ?: "No school",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)
                )
            }
        }
        Box(
            Modifier.clip(RoundedCornerShape(12.dp))
                .background(color=Color.LightGray)
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment=Alignment.CenterHorizontally
            ) {
                Text(text = "Current Period")
                Text(currentPeriod.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = uiState.todaySchedule.block ?: "No School",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier= Modifier.padding(0.dp,6.dp,0.dp,0.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier=Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Minutes Into",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "$timeElapsed",
                            color = Color.Green,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Minutes to End",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${currentPeriod.duration - timeElapsed}",
                            color = Color.Red,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
fun HomePreview() {
    HomeScreen(
        uiState = UIState(todaySchedule = Day(day="May 31, 2024", block="A2", bell = null, testing="SS and Science", announcement = "Mandatory Penis Inspection in 3rd Floor Bathroom")),
        getPeriod = { _ -> Period("Period 1", "0:00", 1440)},
        refreshSchedule = {}
    )
}
