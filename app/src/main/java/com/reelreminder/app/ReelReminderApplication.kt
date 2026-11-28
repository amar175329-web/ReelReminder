package com.reelreminder.app

import android.app.Application
import com.reelreminder.app.data.AppDatabase
import com.reelreminder.app.data.ReelRepository
import com.reelreminder.app.notifications.NotificationHelper

class ReelReminderApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        ReelRepository(
            reelDao = database.reelDao(),
            categoryDao = database.categoryDao(),
            watchLogDao = database.watchLogDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
