package com.reelreminder.app.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(private val dao: ReelDao) {
    val reels: Flow<List<Reel>> = dao.observeAll()

    suspend fun get(id: Long): Reel? = dao.getById(id)

    suspend fun getActive(): List<Reel> = dao.getActive()

    suspend fun insert(reel: Reel): Long = dao.insert(reel)

    suspend fun update(reel: Reel) = dao.update(reel)

    suspend fun delete(reel: Reel) = dao.delete(reel)

    suspend fun setPaused(id: Long, paused: Boolean) = dao.setPaused(id, paused)

    suspend fun markDone(id: Long) = dao.setDone(id, done = true, nextTriggerAt = 0L)

    suspend fun updateNextTrigger(id: Long, nextTriggerAt: Long) =
        dao.updateNextTrigger(id, nextTriggerAt)
}
