package com.reelreminder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {
    @Query(
        """
        SELECT * FROM reels
        ORDER BY done ASC, paused ASC, nextTriggerAt ASC, createdAt DESC
        """
    )
    fun observeAll(): Flow<List<Reel>>

    @Query(
        """
        SELECT * FROM reels
        WHERE categoryId = :categoryId
        ORDER BY done ASC, paused ASC, nextTriggerAt ASC, createdAt DESC
        """
    )
    fun observeByCategory(categoryId: Long): Flow<List<Reel>>

    @Query("SELECT * FROM reels WHERE categoryId = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<Reel>

    @Query("SELECT * FROM reels WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Reel?

    @Query("SELECT * FROM reels WHERE done = 0 AND paused = 0")
    suspend fun getActive(): List<Reel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reel: Reel): Long

    @Update
    suspend fun update(reel: Reel)

    @Delete
    suspend fun delete(reel: Reel)

    @Query("UPDATE reels SET paused = :paused WHERE id = :id")
    suspend fun setPaused(id: Long, paused: Boolean)

    @Query("UPDATE reels SET done = :done, nextTriggerAt = :nextTriggerAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, nextTriggerAt: Long)

    @Query("UPDATE reels SET nextTriggerAt = :nextTriggerAt WHERE id = :id")
    suspend fun updateNextTrigger(id: Long, nextTriggerAt: Long)

    @Query("UPDATE reels SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Query("DELETE FROM reels WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)

    @Query("DELETE FROM reels")
    suspend fun deleteAll()

    @Query("SELECT * FROM reels")
    suspend fun getAll(): List<Reel>
}
