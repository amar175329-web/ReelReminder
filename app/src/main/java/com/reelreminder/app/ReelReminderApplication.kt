package com.reelreminder.app

import android.app.Application
import com.reelreminder.app.data.AppDatabase
import com.reelreminder.app.data.ReelRepository
import com.reelreminder.app.notifications.NotificationHelper

class ReelReminderApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: ReelRepository by lazy { ReelRepository(database.reelDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
