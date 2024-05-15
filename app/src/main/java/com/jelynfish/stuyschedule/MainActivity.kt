package com.jelynfish.stuyschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jelynfish.stuyschedule.ui.ScheduleVM
import com.jelynfish.stuyschedule.ui.theme.StuyScheduleAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScheduleVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StuyScheduleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScheduleApp(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}