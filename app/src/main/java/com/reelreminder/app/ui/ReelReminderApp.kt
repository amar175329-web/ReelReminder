package com.reelreminder.app.ui

import android.webkit.WebStorage
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.reelreminder.app.ui.components.AppTab
import com.reelreminder.app.ui.components.FloatingGlassNav
import com.reelreminder.app.ui.home.TodayView
import com.reelreminder.app.ui.player.PlayerScreen
import com.reelreminder.app.ui.schedule.AddReelSheet
import com.reelreminder.app.ui.schedule.ScheduleViewModel
import com.reelreminder.app.ui.settings.SettingsDialog
import com.reelreminder.app.ui.stats.StatsDialog
import com.reelreminder.app.ui.theme.DarkB1
import com.reelreminder.app.ui.theme.DarkB2
import com.reelreminder.app.ui.theme.LightB1
import com.reelreminder.app.ui.theme.LightB2
import com.reelreminder.app.ui.theme.LocalAppDarkTheme
import com.reelreminder.app.ui.theme.ThemeMode
import com.reelreminder.app.ui.vault.VaultView
import com.reelreminder.app.util.AndroidReelLaunchDelegate
import com.reelreminder.app.util.DebugPrefs
import com.reelreminder.app.util.OpenMethod
import com.reelreminder.app.util.OpenResult
import com.reelreminder.app.util.ReelOpener
import com.reelreminder.app.util.ShareDiagnosticsStore
import com.reelreminder.app.util.TodayGrouping
import kotlinx.coroutines.launch

@Composable
fun ReelReminderApp(
    viewModel: AppViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
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
    val streakStats by viewModel.streakStats.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sharedUrl by viewModel.pendingShareUrl.collectAsStateWithLifecycle()
    val clipboardUrl by viewModel.clipboardUrl.collectAsStateWithLifecycle()
    val scheduleState by scheduleViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showSheet by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var prefillUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var sheetCategoryId by rememberSaveable { mutableLongStateOf(1L) }
    var session by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }
    var selectedOpenMethod by remember { mutableStateOf(DebugPrefs.getOpenMethod(context)) }
    var activePlayerReelId by rememberSaveable { mutableStateOf<Long?>(null) }
    var activePlayerReel by remember { mutableStateOf<Reel?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val ok = runCatching {
                    val json = viewModel.exportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }.isSuccess
                snackbarHostState.showSnackbar(if (ok) "Backup exported" else "Export failed")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val count = runCatching {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) viewModel.importBackupJson(json) else -1
                }.getOrDefault(-1)
                snackbarHostState.showSnackbar(
                    if (count >= 0) "Imported $count reels" else "Invalid backup file"
                )
            }
        }
    }

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

    LaunchedEffect(activePlayerReelId) {
        activePlayerReel = activePlayerReelId?.let { app.repository.get(it) }
    }

    val reelOpener = remember(app, context) {
        ReelOpener(
            loadReelById = { id -> app.repository.get(id) },
            getOpenMethod = { DebugPrefs.getOpenMethod(context) },
            launcher = AndroidReelLaunchDelegate(
                context = context,
                onOpenInAppPlayer = { reelId -> activePlayerReelId = reelId }
            )
        )
    }

    val onPlayReel: (Reel) -> Unit = { reel ->
        coroutineScope.launch {
            when (reelOpener.open(reel.id)) {
                is OpenResult.MissingLink -> {
                    snackbarHostState.showSnackbar("Not saved: this reel has no link")
                }
                is OpenResult.LaunchFailed -> {
                    snackbarHostState.showSnackbar("Could not open reel")
                }
                else -> Unit
            }
        }
    }

    // Issue 13: Delete with 4.5s Undo snackbar
    val onDeleteWithUndo: (Reel) -> Unit = { reel ->
        viewModel.delete(reel)
        coroutineScope.launch {
            val res = snackbarHostState.showSnackbar(
                message = "Reel deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) {
                viewModel.restoreReel(reel)
            }
        }
    }

    BackHandler(enabled = activePlayerReelId != null || showSettings || showStats || selectedCategory != null || showSheet) {
        when {
            showSettings -> showSettings = false
            showStats -> showStats = false
            activePlayerReelId != null -> activePlayerReelId = null
            showSheet -> showSheet = false
            selectedCategory != null -> viewModel.selectCategory(null)
        }
    }

    val isDark = LocalAppDarkTheme.current
    val bgBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) listOf(DarkB1, DarkB2) else listOf(LightB1, LightB2)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Issue 6: Soft radial glow drawn BEHIND content (no hard edge)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val aquaCenter = Offset(size.width * 0.92f, size.height * 0.04f)
            val aquaRadius = size.width * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4D5EEAD4), Color.Transparent),
                    center = aquaCenter,
                    radius = aquaRadius
                ),
                radius = aquaRadius,
                center = aquaCenter
            )
            val skyCenter = Offset(size.width * 0.05f, size.height * 0.78f)
            val skyRadius = size.width * 0.60f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x427DD3FC), Color.Transparent),
                    center = skyCenter,
                    radius = skyRadius
                ),
                radius = skyRadius,
                center = skyCenter
            )
        }

        when (currentTab) {
            AppTab.TODAY -> {
                TodayView(
                    reels = reels,
                    categories = categories,
                    streakStats = streakStats,
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
                    onTogglePause = viewModel::togglePause,
                    onDeleteReel = onDeleteWithUndo,
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
                    onOpenStats = { showStats = true },
                    exactAlarmsEnabled = exactAlarmsEnabled,
                    notificationsEnabled = notificationsEnabled,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onOpenNotificationSettings = onRequestNotificationSettings,
                    onOpenSettings = { showSettings = true }
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
                    onDeleteReel = onDeleteWithUndo,
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (activePlayerReelId != null) {
            val playerId = activePlayerReelId!!
            PlayerScreen(
                reel = activePlayerReel,
                onBack = { activePlayerReelId = null },
                onOpenInInstagram = {
                    coroutineScope.launch {
                        reelOpener.open(playerId, overrideMethod = OpenMethod.M2_INSTAGRAM_HTTPS)
                    }
                },
                onOpenInBrowser = {
                    coroutineScope.launch {
                        reelOpener.open(playerId, overrideMethod = OpenMethod.M3_BROWSER)
                    }
                }
            )
        }
    }

    if (showSheet) {
        AddReelSheet(
            state = scheduleState,
            categories = categories,
            selectedCategoryId = scheduleState.categoryId,
            onCategoryChange = scheduleViewModel::updateCategory,
            onAddNewCategory = { name -> viewModel.addCategory(name) },
            onCoverChanged = scheduleViewModel::updateCoverUri,
            onDismiss = { showSheet = false },
            onUrlChange = scheduleViewModel::updateUrl,
            onNoteChange = scheduleViewModel::updateNote,
            onTypeChange = scheduleViewModel::updateType,
            onDateSelected = scheduleViewModel::updateDate,
            onTimeSelected = scheduleViewModel::updateTime,
            onQuickInOneHour = scheduleViewModel::applyQuickInOneHour,
            onQuickTonight8Pm = scheduleViewModel::applyQuickTonight8Pm,
            onQuickTomorrow7Am = scheduleViewModel::applyQuickTomorrow7Am,
            onRepeatIntervalChange = scheduleViewModel::updateRepeatInterval,
            onRepeatUnitChange = scheduleViewModel::updateRepeatUnit,
            onCustomPreset = scheduleViewModel::applyCustomPreset,
            onEndTypeChange = scheduleViewModel::updateEndType,
            onEndDateSelected = scheduleViewModel::updateEndDate,
            onEndAfterCountChange = scheduleViewModel::updateEndAfterCount,
            onToggleDay = scheduleViewModel::toggleDay,
            onSave = scheduleViewModel::save
        )
    }

    if (showStats) {
        val todaysPlan = remember(reels) { TodayGrouping.group(reels).todaysPlan }
        StatsDialog(
            streakStats = streakStats,
            watchedToday = todaysPlan.count { it.done },
            totalToday = todaysPlan.size,
            categories = categories,
            reels = reels,
            onDismiss = { showStats = false }
        )
    }

    if (showSettings) {
        val lastShared = remember(showSettings) { ShareDiagnosticsStore.load(context) }
        SettingsDialog(
            themeMode = themeMode,
            onSelectThemeMode = onThemeModeChange,
            selectedMethod = selectedOpenMethod,
            lastShared = lastShared,
            onSelectMethod = { method ->
                selectedOpenMethod = method
                DebugPrefs.setOpenMethod(context, method)
            },
            onCreateReelWithoutLink = {
                viewModel.createDebugReelWithoutLink()
                showSettings = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Created debug reel without link")
                }
            },
            onExportBackup = {
                showSettings = false
                exportLauncher.launch("reelreminder-backup.json")
            },
            onImportBackup = {
                showSettings = false
                importLauncher.launch(arrayOf("application/json"))
            },
            onDeleteAllData = {
                showSettings = false
                confirmDeleteAll = true
            },
            onDismiss = { showSettings = false }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all my data?") },
            text = { Text("This removes all saved reels, covers, watch history, and scheduled alarms from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteAll = false
                        runCatching { WebStorage.getInstance().deleteAllData() }
                        viewModel.deleteAllData(context) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All local data deleted")
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete all")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
