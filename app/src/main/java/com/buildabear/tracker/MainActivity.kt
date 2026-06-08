package com.buildabear.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.buildabear.tracker.data.repository.ActiveFilterStore
import com.buildabear.tracker.navigation.BearTrackerNavGraph
import com.buildabear.tracker.ui.theme.BearTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var activeFilterStore: ActiveFilterStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BearTrackerTheme {
                val navController = rememberNavController()
                BearTrackerNavGraph(
                    navController = navController,
                    activeFilterStore = activeFilterStore,
                )
            }
        }
    }
}
