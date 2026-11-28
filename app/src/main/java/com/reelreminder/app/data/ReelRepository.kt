package com.reelreminder.app.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(
    private val reelDao: ReelDao,
    private val categoryDao: CategoryDao,
    private val watchLogDao: WatchLogDao
) {
    val reels: Flow<List<Reel>> = reelDao.observeAll()
    val categories: Flow<List<Category>> = categoryDao.observeAll()
    val watchLogs: Flow<List<WatchLog>> = watchLogDao.observeAll()

    suspend fun get(id: Long): Reel? = reelDao.getById(id)

    suspend fun getAllReels(): List<Reel> = reelDao.getAll()

    suspend fun getActive(): List<Reel> = reelDao.getActive()

    suspend fun getReelsByCategory(categoryId: Long): List<Reel> = reelDao.getByCategory(categoryId)

    fun observeReelsByCategory(categoryId: Long): Flow<List<Reel>> = reelDao.observeByCategory(categoryId)

    suspend fun insert(reel: Reel): Long = reelDao.insert(reel)

    suspend fun update(reel: Reel) = reelDao.update(reel)

    suspend fun delete(reel: Reel) = reelDao.delete(reel)

    suspend fun setPaused(id: Long, paused: Boolean) = reelDao.setPaused(id, paused)

    suspend fun markDone(id: Long) {
        reelDao.setDone(id, done = true, nextTriggerAt = 0L)
        watchLogDao.insert(WatchLog(reelId = id))
    }

    suspend fun toggleDone(id: Long) {
        val reel = reelDao.getById(id) ?: return
        val newDone = !reel.done
        reelDao.setDone(id, done = newDone, nextTriggerAt = if (newDone) 0L else reel.nextTriggerAt)
        if (newDone) {
            watchLogDao.insert(WatchLog(reelId = id))
        }
    }

    suspend fun updateNextTrigger(id: Long, nextTriggerAt: Long) =
        reelDao.updateNextTrigger(id, nextTriggerAt)

    suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)

    suspend fun getAllCategories(): List<Category> = categoryDao.getAll()

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun renameCategory(categoryId: Long, newName: String): Boolean {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return false
        val existing = categoryDao.getById(categoryId) ?: return false
        categoryDao.update(existing.copy(name = trimmed))
        return true
    }

    /**
     * Deletes a category while protecting the last remaining collection.
     * If [moveToCategoryId] is non-null, reassigns reels there; otherwise deletes reels in that category.
     */
    suspend fun deleteCategory(category: Category, moveToCategoryId: Long? = null): Boolean {
        val all = categoryDao.getAll()
        if (all.size <= 1) return false
        if (moveToCategoryId != null && moveToCategoryId != category.id) {
            reelDao.reassignCategory(category.id, moveToCategoryId)
        } else {
            reelDao.deleteByCategory(category.id)
        }
        categoryDao.delete(category)
        return true
    }

    suspend fun deleteAllData() {
        reelDao.deleteAll()
        watchLogDao.deleteAll()
    }
}
