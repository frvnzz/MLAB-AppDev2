package com.example.purrsistence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.focus.SharedPrefsFocusBlocker
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.StatisticsRepository
import com.example.purrsistence.data.local.repository.TrackingRepositoryImpl
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.time.SystemTimeProvider
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import com.example.purrsistence.focus.DeepFocusConfig
import com.example.purrsistence.ui.screens.MainScreen
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import com.example.purrsistence.ui.viewmodel.TrackingViewModel
import com.example.purrsistence.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DATABASE & DAO
        val db = AppDatabase.getInstance(this)
        val dao = db.dao()
        val userDao = db.userDao()

        // REPOSITORIES
        val userRepo = UserRepository(userDao)
        val goalRepo = GoalRepository(dao)
        val timeProvider = SystemTimeProvider()
        val trackingRepo = TrackingRepositoryImpl(dao, timeProvider)
        val statisticsRepo = StatisticsRepository(dao)

        // shared preferences (for storing last selected goal from GoalBottomDrawer)
        val prefs = getSharedPreferences(DeepFocusConfig.PREFS_NAME, MODE_PRIVATE)
        val focusBlocker = SharedPrefsFocusBlocker(prefs)

        // create ViewModel instances for this activity
        userViewModel = UserViewModel(userRepo)
        goalViewModel = GoalViewModel(goalRepo, prefs)
        trackingViewModel = TrackingViewModel(trackingRepo, timeProvider, focusBlocker)
        statisticsViewModel = StatisticsViewModel(statisticsRepo)

        lifecycleScope.launch {
            // Only insert if userId 1 doesn't exist
            if (userDao.getUserById(1) == null) {
                val exampleUser = User(
                    userId = 1, // fixed userId 1 for the test user
                    username = "testuser",
                    balance = 100,
                    friends = listOf("alice", "bob"),
                    collectedCatsIds = listOf("cat1", "cat2")
                )
                dao.insertUser(exampleUser)
            }
        }

        setContent {
            PurrsistenceTheme {
                // pass created ViewModels to MainScreen (scaffold)
                MainScreen(
                    userViewModel = userViewModel,
                    goalViewModel = goalViewModel,
                    trackingViewModel = trackingViewModel,
                    statisticsViewModel = statisticsViewModel,
                )
            }
        }
    }
}