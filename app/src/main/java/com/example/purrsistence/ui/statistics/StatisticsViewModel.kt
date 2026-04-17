package com.example.purrsistence.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.StatisticsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: StatisticsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentJob: Job? = null

    init {
        loadStats(0)
    }

    private fun loadStats(offset: Int) {
        currentJob?.cancel()

        _uiState.value = _uiState.value.copy(
            weekOffset = offset,
            isLoading = true
        )

        currentJob = viewModelScope.launch {
            repository.getWeeklyStats(offset).collect { (daily, goals) ->
                _uiState.value = _uiState.value.copy(
                    dailyStats = daily,
                    goalStats = goals,
                    isLoading = false
                )
            }
        }
    }

    fun previousWeek(){
        loadStats(_uiState.value.weekOffset -1)
    }

    fun nextWeek(){
        loadStats(_uiState.value.weekOffset +1)
    }
}