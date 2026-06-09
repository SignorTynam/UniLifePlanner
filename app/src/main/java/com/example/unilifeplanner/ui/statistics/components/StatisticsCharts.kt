package com.example.unilifeplanner.ui.statistics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.statistics.CourseStatusStatUi
import com.example.unilifeplanner.ui.statistics.StatisticsUiState
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun CourseStatusDistributionChart(
    uiState: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    val stats = uiState.courseStatusStats
    val total = stats.sumOf { it.count }.coerceAtLeast(1)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Distribuzione corsi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                stats.forEach { stat ->
                    if (stat.count > 0) {
                        val fraction = stat.count.toFloat() / total.toFloat()
                        Box(
                            modifier = Modifier
                                .weight(fraction.coerceAtLeast(0.01f))
                                .height(14.dp)
                                .background(statusColor(stat.label))
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.forEach { stat ->
                    StatusLegendRow(
                        label = stat.label,
                        count = stat.count,
                        percentage = (stat.percentage * 100).toInt(),
                        color = statusColor(stat.label)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLegendRow(
    label: String,
    count: Int,
    percentage: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "$count · $percentage%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun statusColor(label: String): Color {
    return when (label) {
        "Da studiare" -> MaterialTheme.colorScheme.tertiary
        "In corso" -> MaterialTheme.colorScheme.secondary
        "Completati" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun CreditsInsightSection(
    uiState: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Crediti formativi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CreditMiniStat(label = "Completati", value = uiState.completedCredits.toString())
                CreditMiniStat(label = "Mancanti", value = uiState.remainingCredits.toString())
                CreditMiniStat(label = "Totali", value = uiState.totalCredits.toString())
            }
            LinearProgressIndicator(
                progress = { uiState.completionPercentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${uiState.completionPercentageText} del percorso completato",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreditMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseStatusDistributionChartPreview() {
    UniLifePlannerTheme {
        CourseStatusDistributionChart(
            uiState = StatisticsUiState(
                isEmpty = false,
                courseStatusStats = listOf(
                    CourseStatusStatUi("Da studiare", 4, 0.33f),
                    CourseStatusStatUi("In corso", 5, 0.42f),
                    CourseStatusStatUi("Completati", 3, 0.25f)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun CourseStatusDistributionChartDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        CreditsInsightSection(
            uiState = StatisticsUiState(
                isEmpty = false,
                completedCredits = 60,
                remainingCredits = 120,
                totalCredits = 180,
                completionPercentage = 0.33f,
                completionPercentageText = "33%"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
