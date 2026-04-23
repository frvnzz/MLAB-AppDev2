package com.example.purrsistence.ui.state

import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.domain.model.GoalStat

data class StatisticsUiState(
    val dailyStats: List<DailyStat> = emptyList(),
    val goalStats: List<GoalStat> = emptyList(),
    val isLoading: Boolean = true,
    val weekOffset: Int = 0,
)