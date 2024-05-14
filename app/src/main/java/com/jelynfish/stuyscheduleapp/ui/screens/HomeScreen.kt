package com.jelynfish.stuyscheduleapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import com.jelynfish.stuyscheduleapp.ui.UIState
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen(uiState: UIState) {
    val currentTime by rememberUpdatedState(newValue = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))

    LaunchedEffect(key1 = currentTime) {
        val currentSecond = currentTime.second
        delay((60 - currentSecond) * 1000L)  // Delay until the next minute
    }
    Column {
        Text("This is the home screen")
        Text("The time is: ${currentTime.hour}:${currentTime.minute}")
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(uiState = UIState())
}