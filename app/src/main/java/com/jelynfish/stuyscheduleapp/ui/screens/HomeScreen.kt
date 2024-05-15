package com.jelynfish.stuyscheduleapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.jelynfish.stuyscheduleapp.ui.UIState
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen(uiState: UIState) {
    var currentTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(1000) // Wait for 1 second
            currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) // Update the current time
        }
    }

    Column {
        Text("This is the home screen")
        Text("The time is: ${currentTime.hour}:${currentTime.minute}:${currentTime.second}")
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(uiState = UIState())
}