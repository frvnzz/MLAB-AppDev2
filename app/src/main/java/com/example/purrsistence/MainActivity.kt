package com.example.purrsistence

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.purrsistence.data.local.AppDatabase
import com.example.purrsistence.data.local.entity.User
import com.example.purrsistence.data.local.repository.DataRepository
import com.example.purrsistence.ui.DataViewModel
import com.example.purrsistence.ui.screens.MainScreen
import com.example.purrsistence.ui.theme.PurrsistenceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: DataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // goal database and repository
        val db = AppDatabase.getInstance(this)
        val dao = db.dao()
        val repo = DataRepository(dao)

        // shared preferences (for storing last selected goal from GoalBottomDrawer)
        val prefs = getSharedPreferences("purrsistence_prefs", Context.MODE_PRIVATE)

        // create ViewModel instance for this activity
        viewModel = DataViewModel(repo, prefs)

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
                // pass created ViewModel to MainScreen (scaffold)
                MainScreen(viewModel = viewModel)
            }
        }
    }
}