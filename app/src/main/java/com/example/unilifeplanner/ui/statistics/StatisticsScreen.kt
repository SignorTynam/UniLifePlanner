package com.example.unilifeplanner.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.statistics.components.CourseStatusDistributionChart
import com.example.unilifeplanner.ui.statistics.components.CreditsInsightSection
import com.example.unilifeplanner.ui.statistics.components.ExamsStatsSection
import com.example.unilifeplanner.ui.statistics.components.LessonsStatsSection
import com.example.unilifeplanner.ui.statistics.components.StatisticsDashboardHeader
import com.example.unilifeplanner.ui.statistics.components.StatisticsEmptyState
import com.example.unilifeplanner.ui.statistics.components.StatisticsInsightsSection
import com.example.unilifeplanner.ui.statistics.components.StatisticsKpiGrid

@Composable
fun StatisticsScreen(
    onMenuClick: () -> Unit,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Statistiche",
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        if (uiState.isEmpty) {
            StatisticsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { StatisticsDashboardHeader(uiState = uiState) }
                item { StatisticsKpiGrid(uiState = uiState) }
                item { CourseStatusDistributionChart(uiState = uiState) }
                item { CreditsInsightSection(uiState = uiState) }
                item { LessonsStatsSection(uiState = uiState) }
                item { ExamsStatsSection(uiState = uiState) }
                item { StatisticsInsightsSection(insights = uiState.insights) }
            }
        }
    }
}
