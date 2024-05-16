package com.jelynfish.stuyschedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jelynfish.stuyschedule.ui.ScheduleVM
import com.jelynfish.stuyschedule.ui.screens.DetailScreen
import com.jelynfish.stuyschedule.ui.screens.HomeScreen
import com.jelynfish.stuyschedule.ui.screens.SettingsScreen

object Route {
    const val HOME = "Home"
    const val DETAIL = "Detail"
    const val SETTINGS = "Settings"
}

data class Destination(
    val route: String,
    val textId: Int,
    //val icon: Icon
)

val DESTINATIONS = listOf(
    Destination(
        route = Route.HOME,
        textId = R.string.home,
        //icon = ,
    ),
    Destination(
        route = Route.DETAIL,
        textId = R.string.detail,
        //icon = ,
    ),
    Destination(
        route = Route.SETTINGS,
        textId = R.string.settings,
        //icon =,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleApp(
    viewModel: ScheduleVM
) {
    val destination = remember { mutableStateOf(Route.HOME) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(destination.value) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },

        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                DESTINATIONS.forEach {
                        dest -> NavigationBarItem(
                    label = { Text(text = stringResource(id = dest.textId)) },
                    selected = destination.value == dest.route,
                    onClick = { destination.value = dest.route },
                    icon = { /*TODO*/ })
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (destination.value) {
                Route.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        getPeriod = {cal -> viewModel.whatPeriod(cal)}
                    )
                }

                Route.DETAIL -> {
                    DetailScreen(
                        uiState = uiState
                    )
                }

                Route.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState
                    )
                }
            }
        }
    }
}
