package com.reelreminder.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_logs",
    indices = [Index(value = ["reelId"]), Index(value = ["watchedAt"])]
)
data class WatchLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val reelId: Long,
    val watchedAt: Long = System.currentTimeMillis()
)
