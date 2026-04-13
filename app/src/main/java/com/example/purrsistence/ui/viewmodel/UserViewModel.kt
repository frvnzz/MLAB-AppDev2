package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val userBalance = userRepository
        .getUserBalance(1)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
}