package com.example.unilifeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.unilifeplanner.data.datastore.SettingsDataStore
import com.example.unilifeplanner.data.repository.SettingsRepository
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.navigation.AppNavigation
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsRepository = remember {
                SettingsRepository(
                    settingsDataStore = SettingsDataStore(context.applicationContext)
                )
            }
            val themeMode by settingsRepository.themeMode.collectAsState(
                initial = ThemeMode.SYSTEM
            )

            UniLifePlannerTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val initialCourseId = intent
                    ?.getIntExtra(NotificationHelper.EXTRA_COURSE_ID, -1)
                    ?.takeIf { it > 0 }

                AppNavigation(
                    navController = navController,
                    initialCourseId = initialCourseId
                )
            }
        }
    }
}
