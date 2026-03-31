package com.example.purrsistence.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.data.local.entity.User
import androidx.room.TypeConverters
import com.example.purrsistence.data.local.converter.StringListConverter

@Database(
    entities = [
        User::class,
        Goal::class,
        TrackingSession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): Dao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                ).build().also { INSTANCE = it }
            }
    }
}
