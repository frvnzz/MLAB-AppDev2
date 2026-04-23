package com.example.purrsistence.domain.model

import java.time.DayOfWeek

data class DailyStat(
    val dayOfWeek: DayOfWeek,
    val totalMinutes: Int
)
