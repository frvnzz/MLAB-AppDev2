package com.example.purrsistence.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.DailyStat
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyChart(dailyStats: List<DailyStat>) {

    val modelProducer = remember { CartesianChartModelProducer() }

    val zone = ZoneId.systemDefault()

    LaunchedEffect(dailyStats) {
        modelProducer.runTransaction {

            val values = dailyStats
                .sortedBy { it.dayOfWeek.value }
                .map { it.totalMinutes }

            columnSeries {
                series(values)
            }
        }
    }

    Column(

    ) {
        Text("Tracked Time per Day", style = MaterialTheme.typography.labelLarge)
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                            ),
                            thickness = 7.dp,
                            fill = Fill(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                    )
                                )
                            )
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    guideline = null
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, x, _ ->
                        val dayIndex = x.toInt()
                        val day = DayOfWeek.of(dayIndex + 1)

                        day.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    }
                )
            ),
            modelProducer = modelProducer
        )
    }


}