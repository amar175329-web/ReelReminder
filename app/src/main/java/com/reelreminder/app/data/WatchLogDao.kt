package com.reelreminder.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLogDao {
    @Query("SELECT * FROM watch_logs ORDER BY watchedAt DESC")
    fun observeAll(): Flow<List<WatchLog>>

    @Query("SELECT * FROM watch_logs ORDER BY watchedAt DESC")
    suspend fun getAll(): List<WatchLog>

    @Insert
    suspend fun insert(log: WatchLog): Long

    @Query("DELETE FROM watch_logs")
    suspend fun deleteAll()
}
