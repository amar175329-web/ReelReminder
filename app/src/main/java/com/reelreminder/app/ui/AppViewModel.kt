package com.reelreminder.app.ui

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.Reel
import com.reelreminder.app.notifications.NotificationHelper
import com.reelreminder.app.ui.components.AppTab
import com.reelreminder.app.util.BackupCodec
import com.reelreminder.app.util.CoverStorage
import com.reelreminder.app.util.NextTriggerCalculator
import com.reelreminder.app.util.ShortcodeParser
import com.reelreminder.app.util.StreakCalculator
import com.reelreminder.app.util.StreakStats
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ReelReminderApplication
    private val repository = app.repository
    private val scheduler = AlarmScheduler(app)

    val reels: StateFlow<List<Reel>> = repository.reels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<Category>> = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Category.DEFAULT_CATEGORIES
    )

    val streakStats: StateFlow<StreakStats> = repository.watchLogs
        .map { logs -> StreakCalculator.compute(logs.map { it.watchedAt }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreakStats(0, List(7) { false })
        )

    private val _currentTab = MutableStateFlow(AppTab.TODAY)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _pendingShareUrl = MutableStateFlow<String?>(null)
    val pendingShareUrl: StateFlow<String?> = _pendingShareUrl.asStateFlow()

    private val _clipboardUrl = MutableStateFlow<String?>(null)
    val clipboardUrl: StateFlow<String?> = _clipboardUrl.asStateFlow()

    private var lastDismissedClipboardUrl: String? = null

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.TODAY) {
            _selectedCategory.value = null
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun onSharedUrl(url: String) {
        _pendingShareUrl.value = url
    }

    fun consumeSharedUrl() {
        _pendingShareUrl.value = null
    }

    fun checkClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        if (!clipboard.hasPrimaryClip()) return
        val clip = clipboard.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0)?.text?.toString() ?: return
        val parsed = ShortcodeParser.extractFromText(text) ?: return
        val alreadySaved = reels.value.any { existing ->
            ShortcodeParser.parse(existing.url)?.shortcode == parsed.shortcode
        }
        if (!alreadySaved && parsed.canonicalUrl != lastDismissedClipboardUrl && _pendingShareUrl.value == null) {
            _clipboardUrl.value = parsed.canonicalUrl
        }
    }

    fun dismissClipboardUrl() {
        lastDismissedClipboardUrl = _clipboardUrl.value
        _clipboardUrl.value = null
    }

    fun toggleDone(reel: Reel) {
        viewModelScope.launch {
            repository.toggleDone(reel.id)
            if (!reel.done) {
                scheduler.cancel(reel.id)
                NotificationHelper.cancel(app, reel.id)
            }
        }
    }

    fun togglePause(reel: Reel) {
        val shouldPause = !reel.paused
        viewModelScope.launch {
            repository.setPaused(reel.id, shouldPause)
            if (shouldPause) {
                scheduler.cancel(reel.id)
            } else {
                val updated = repository.get(reel.id) ?: return@launch
                if (updated.done) return@launch
                val next = NextTriggerCalculator.nextAfter(
                    reel = updated,
                    from = Instant.now().plusSeconds(1)
                )
                if (next > 0L) {
                    repository.updateNextTrigger(updated.id, next)
                    scheduler.schedule(updated.copy(nextTriggerAt = next), next)
                }
            }
        }
    }

    fun delete(reel: Reel) {
        viewModelScope.launch {
            scheduler.cancel(reel.id)
            NotificationHelper.cancel(app, reel.id)
            repository.delete(reel)
        }
    }

    /** Issue 13: Restore deleted reel on Undo and reschedule its alarm. */
    fun restoreReel(reel: Reel) {
        viewModelScope.launch {
            val id = repository.insert(reel)
            val restored = repository.get(id) ?: return@launch
            if (!restored.done && !restored.paused && restored.nextTriggerAt > System.currentTimeMillis()) {
                scheduler.schedule(restored, restored.nextTriggerAt)
            }
        }
    }

    fun createDebugReelWithoutLink() {
        viewModelScope.launch {
            val nextTrigger = Instant.now().plusSeconds(3600).toEpochMilli()
            repository.insert(
                Reel(
                    url = "",
                    note = "Debug reel (no link)",
                    categoryId = 1L,
                    nextTriggerAt = nextTrigger
                )
            )
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return@launch
            val existing = repository.getAllCategories()
            val (start, end) = Category.nextGradientPair(existing.size)
            val newCat = Category(
                name = trimmed,
                gradientStart = start,
                gradientEnd = end,
                isDefault = false
            )
            repository.insertCategory(newCat)
        }
    }

    fun renameCategory(category: Category, newName: String) {
        viewModelScope.launch {
            if (repository.renameCategory(category.id, newName)) {
                if (_selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = category.copy(name = newName.trim())
                }
            }
        }
    }

    fun deleteCategory(category: Category, moveToCategoryId: Long? = null) {
        viewModelScope.launch {
            val deleted = repository.deleteCategory(category, moveToCategoryId)
            if (deleted && _selectedCategory.value?.id == category.id) {
                _selectedCategory.value = null
            }
        }
    }

    /** Issue 17: Wipe all local data (Room, covers, alarms). */
    fun deleteAllData(context: Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.getAllReels().forEach { reel ->
                scheduler.cancel(reel.id)
                NotificationHelper.cancel(app, reel.id)
            }
            CoverStorage.deleteAll(context)
            repository.deleteAllData()
            _selectedCategory.value = null
            onComplete()
        }
    }

    suspend fun exportBackupJson(): String {
        return BackupCodec.encode(
            categories = repository.getAllCategories(),
            reels = repository.getAllReels()
        )
    }

    suspend fun importBackupJson(json: String): Int {
        val payload = BackupCodec.decode(json) ?: return -1
        payload.categories.forEach { cat ->
            if (repository.getCategory(cat.id) == null) {
                repository.insertCategory(cat)
            }
        }
        var count = 0
        payload.reels.forEach { reel ->
            val id = repository.insert(reel.copy(id = 0L))
            val saved = repository.get(id)
            if (saved != null && !saved.done && !saved.paused && saved.nextTriggerAt > System.currentTimeMillis()) {
                scheduler.schedule(saved, saved.nextTriggerAt)
            }
            count++
        }
        return count
    }

    fun rescheduleAll() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val from = Instant.ofEpochMilli(now).plusSeconds(1)
            repository.getActive().forEach { reel ->
                val next = if (reel.nextTriggerAt > now) {
                    reel.nextTriggerAt
                } else {
                    NextTriggerCalculator.nextAfter(reel = reel, from = from)
                }
                if (next > 0L) {
                    if (next != reel.nextTriggerAt) {
                        repository.updateNextTrigger(reel.id, next)
                    }
                    scheduler.schedule(reel.copy(nextTriggerAt = next), next)
                }
            }
        }
    }
}
