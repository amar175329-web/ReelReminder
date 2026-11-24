package com.reelreminder.app.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(
    private val reelDao: ReelDao,
    private val categoryDao: CategoryDao
) {
    val reels: Flow<List<Reel>> = reelDao.observeAll()
    val categories: Flow<List<Category>> = categoryDao.observeAll()

    suspend fun get(id: Long): Reel? = reelDao.getById(id)

    suspend fun getActive(): List<Reel> = reelDao.getActive()

    suspend fun getReelsByCategory(categoryId: Long): List<Reel> = reelDao.getByCategory(categoryId)

    fun observeReelsByCategory(categoryId: Long): Flow<List<Reel>> = reelDao.observeByCategory(categoryId)

    suspend fun insert(reel: Reel): Long = reelDao.insert(reel)

    suspend fun update(reel: Reel) = reelDao.update(reel)

    suspend fun delete(reel: Reel) = reelDao.delete(reel)

    suspend fun setPaused(id: Long, paused: Boolean) = reelDao.setPaused(id, paused)

    suspend fun markDone(id: Long) = reelDao.setDone(id, done = true, nextTriggerAt = 0L)

    suspend fun toggleDone(id: Long) {
        val reel = reelDao.getById(id) ?: return
        val newDone = !reel.done
        reelDao.setDone(id, done = newDone, nextTriggerAt = if (newDone) 0L else reel.nextTriggerAt)
    }

    suspend fun updateNextTrigger(id: Long, nextTriggerAt: Long) =
        reelDao.updateNextTrigger(id, nextTriggerAt)

    suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)

    suspend fun getAllCategories(): List<Category> = categoryDao.getAll()

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) {
        // Reassign any reels in this category to default category 1
        reelDao.reassignCategory(category.id, 1L)
        categoryDao.delete(category)
    }
}
