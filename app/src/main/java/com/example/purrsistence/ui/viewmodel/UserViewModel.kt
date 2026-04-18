package com.example.purrsistence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrsistence.data.local.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Centralized source of truth for the current user
    val currentUserId: Int = 1

    val user = userRepository
        .observeUser(currentUserId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
    
    val userBalance = userRepository
        .getUserBalance(currentUserId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    fun buyCat(catId: String, price: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.buyCat(currentUserId, catId, price)
        }
    }
}