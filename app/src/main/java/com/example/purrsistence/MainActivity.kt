package com.example.purrsistence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.domain.time.SystemTimeProvider
import com.example.purrsistence.ui.GoalViewModel
import com.example.purrsistence.ui.screens.MainScreen
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import com.example.purrsistence.ui.tracking.TrackingViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var goalViewModel: GoalViewModel
    private lateinit var trackingViewModel: TrackingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // goal database and repository
        val db = AppDatabase.getInstance(this)
        val dao = db.dao()
        val repo = GoalRepository(dao)

        val timeProvider = SystemTimeProvider()
        val trackingRepo = TrackingRepositoryImpl(dao, timeProvider)

        // shared preferences (for storing last selected goal from GoalBottomDrawer)
        val prefs = getSharedPreferences("purrsistence_prefs", MODE_PRIVATE)

        // create ViewModel instances for this activity
        goalViewModel = GoalViewModel(repo, prefs)
        trackingViewModel = TrackingViewModel(trackingRepo, timeProvider)

        val exampleUser = User(
            username = "testuser",
            balance = 100,
            friends = listOf("alice", "bob"),
            collectedCatsIds = listOf("cat1", "cat2")
        )

        lifecycleScope.launch {
            dao.insertUser(exampleUser)
        }

        setContent {
            PurrsistenceTheme {
                // pass created ViewModels to MainScreen (scaffold)
                MainScreen(
                    goalViewModel = goalViewModel,
                    trackingViewModel = trackingViewModel
                )
            }
        }
    }
}