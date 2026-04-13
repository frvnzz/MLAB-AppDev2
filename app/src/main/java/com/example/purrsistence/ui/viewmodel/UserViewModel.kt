package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Centralized source of truth for the current user
    val currentUserId: Int = 1

    // TODO: unit test the getUserBalance function from the repository (+ Dao)
    val userBalance = userRepository
        .getUserBalance(currentUserId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
}