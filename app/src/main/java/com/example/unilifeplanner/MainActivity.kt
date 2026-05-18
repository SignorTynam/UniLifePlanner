package com.example.unilifeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.unilifeplanner.navigation.AppNavigation
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniLifePlannerTheme {
                AppNavigation()
            }
        }
    }
}
