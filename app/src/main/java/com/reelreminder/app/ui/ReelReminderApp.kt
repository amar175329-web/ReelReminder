package com.reelreminder.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reelreminder.app.R
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.data.Reel
import com.reelreminder.app.notifications.NotificationHelper
import com.reelreminder.app.ui.components.AppTab
import com.reelreminder.app.ui.components.FloatingGlassNav
import com.reelreminder.app.ui.home.TodayView
import com.reelreminder.app.ui.schedule.AddReelSheet
import com.reelreminder.app.ui.schedule.ScheduleViewModel
import com.reelreminder.app.ui.vault.VaultView

@Composable
fun ReelReminderApp(
    viewModel: AppViewModel,
    onRequestNotificationSettings: () -> Unit,
    onRequestExactAlarms: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ReelReminderApplication
    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(app)
    )

    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sharedUrl by viewModel.pendingShareUrl.collectAsStateWithLifecycle()
    val clipboardUrl by viewModel.clipboardUrl.collectAsStateWithLifecycle()
    val scheduleState by scheduleViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var prefillUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var sheetCategoryId by rememberSaveable { mutableStateOf(1L) }
    var session by rememberSaveable { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<Reel?>(null) }

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var exactAlarmsEnabled by remember {
        mutableStateOf(AlarmScheduler(context).canScheduleExact())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                exactAlarmsEnabled = AlarmScheduler(context).canScheduleExact()
                viewModel.checkClipboard(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // When a URL is shared from Instagram, immediately open the Add Sheet
    LaunchedEffect(sharedUrl) {
        val url = sharedUrl ?: return@LaunchedEffect
        editingId = null
        prefillUrl = url
        session++
        showSheet = true
        viewModel.consumeSharedUrl()
    }

    LaunchedEffect(exactAlarmsEnabled) {
        viewModel.rescheduleAll()
    }

    LaunchedEffect(showSheet, session) {
        if (showSheet) {
            scheduleViewModel.start(
                editingId = editingId,
                sharedUrl = prefillUrl,
                defaultCategory = sheetCategoryId
            )
        }
    }

    LaunchedEffect(scheduleState.saved) {
        if (!scheduleState.saved) return@LaunchedEffect
        showSheet = false
        val message = if (scheduleState.needsExactAlarm) {
            context.getString(R.string.exact_alarm_saved_warning)
        } else {
            context.getString(R.string.reminder_saved)
        }
        snackbarHostState.showSnackbar(
            message = message,
            duration = if (scheduleState.needsExactAlarm) SnackbarDuration.Long else SnackbarDuration.Short
        )
        scheduleViewModel.consumeSaved()
    }

    val onPlayReel: (Reel) -> Unit = { reel ->
        runCatching {
            val intent = NotificationHelper.createOpenReelIntent(context, reel.url)
            context.startActivity(intent)
        }
    }

    BackHandler(enabled = selectedCategory != null || showSheet) {
        when {
            showSheet -> showSheet = false
            selectedCategory != null -> viewModel.selectCategory(null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            AppTab.TODAY -> {
                TodayView(
                    reels = reels,
                    categories = categories,
                    clipboardUrl = clipboardUrl,
                    onDismissClipboard = viewModel::dismissClipboardUrl,
                    onSaveClipboard = { url ->
                        viewModel.dismissClipboardUrl()
                        editingId = null
                        prefillUrl = url
                        session++
                        showSheet = true
                    },
                    onPlayReel = onPlayReel,
                    onToggleDone = viewModel::toggleDone,
                    onEditReel = { reel ->
                        editingId = reel.id
                        prefillUrl = null
                        sheetCategoryId = reel.categoryId
                        session++
                        showSheet = true
                    },
                    onOpenAdd = {
                        editingId = null
                        prefillUrl = null
                        sheetCategoryId = selectedCategory?.id ?: 1L
                        session++
                        showSheet = true
                    },
                    exactAlarmsEnabled = exactAlarmsEnabled,
                    notificationsEnabled = notificationsEnabled,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onOpenNotificationSettings = onRequestNotificationSettings
                )
            }

            AppTab.VAULT -> {
                VaultView(
                    categories = categories,
                    reels = reels,
                    selectedCategory = selectedCategory,
                    onSelectCategory = viewModel::selectCategory,
                    onAddCategory = viewModel::addCategory,
                    onPlayReel = onPlayReel,
                    onDeleteReel = { reel -> deleteTarget = reel },
                    onEditReel = { reel ->
                        editingId = reel.id
                        prefillUrl = null
                        sheetCategoryId = reel.categoryId
                        session++
                        showSheet = true
                    }
                )
            }
        }

        // Floating Glass Navigation
        FloatingGlassNav(
            currentTab = currentTab,
            onTabSelected = viewModel::selectTab,
            onAddClick = {
                editingId = null
                prefillUrl = null
                sheetCategoryId = selectedCategory?.id ?: 1L
                session++
                showSheet = true
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }

    // Modal Add / Edit Sheet
    if (showSheet) {
        AddReelSheet(
            state = scheduleState,
            categories = categories,
            selectedCategoryId = scheduleState.categoryId,
            onCategoryChange = scheduleViewModel::updateCategory,
            onAddNewCategory = { name ->
                viewModel.addCategory(name)
            },
            onDismiss = { showSheet = false },
            onUrlChange = scheduleViewModel::updateUrl,
            onNoteChange = scheduleViewModel::updateNote,
            onTypeChange = scheduleViewModel::updateType,
            onTimeSelected = scheduleViewModel::updateTime,
            onToggleDay = scheduleViewModel::toggleDay,
            onSave = scheduleViewModel::save
        )
    }

    // Delete confirmation dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(target)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
