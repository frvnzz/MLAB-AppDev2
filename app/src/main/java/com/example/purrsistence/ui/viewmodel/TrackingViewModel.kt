package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.ui.tracking.TrackingEvent
import com.example.purrsistence.ui.tracking.TrackingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val repository: TrackingRepository,
    private val timeProvider: TimeProvider,
    private val focusBlocker: FocusBlocker
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private val _events = MutableSharedFlow<TrackingEvent>()
    val events: SharedFlow<TrackingEvent> = _events

    private var timerJob: Job? = null
    private var isDeepFocusSession = false

    fun startTrack(goalId: Int, userId: Int, deepFocus: Boolean) {
        viewModelScope.launch{
            val session = repository.startTracking(
                goalId = goalId,
                userId = userId,
                pauseReminder = false
            )

            isDeepFocusSession = deepFocus
            if (isDeepFocusSession) {
                focusBlocker.startBlocking()
            }

            _uiState.value = TrackingUiState(
                trackingId = session.trackingId,
                goalId = session.goalId,
                startTime = session.startTime,
                elapsedMillis = 0L,
                isTracking = true
            )

            startTicker(session.startTime)
            _events.emit(TrackingEvent.NavigateToTrackingScreen)
        }
    }

    fun stopTracking() {
        viewModelScope.launch{
            val trackingId = _uiState.value.trackingId ?: return@launch
            repository.stopTracking(trackingId)

            timerJob?.cancel()
            timerJob = null

            if (isDeepFocusSession) {
                focusBlocker.stopBlocking()
                isDeepFocusSession = false
            }

            _uiState.value = TrackingUiState()
            _events.emit(TrackingEvent.NavigateBackHome)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        if (isDeepFocusSession) {
            focusBlocker.stopBlocking()
            isDeepFocusSession = false
        }
        super.onCleared()
    }

    private fun startTicker(startTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                _uiState.value = _uiState.value.copy(
                    elapsedMillis = timeProvider.now() - startTime
                )
                delay(1000)
            }
        }
    }

}