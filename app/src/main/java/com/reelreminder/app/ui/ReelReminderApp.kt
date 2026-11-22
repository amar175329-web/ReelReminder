package com.reelreminder.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reelreminder.app.R
import com.reelreminder.app.ReelReminderApplication
import com.reelreminder.app.alarm.AlarmScheduler
import com.reelreminder.app.ui.home.HomeScreen
import com.reelreminder.app.ui.schedule.ScheduleScreen
import com.reelreminder.app.ui.schedule.ScheduleViewModel

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
    val sharedUrl by viewModel.pendingShareUrl.collectAsStateWithLifecycle()
    val scheduleState by scheduleViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSchedule by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var prefillUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var session by rememberSaveable { mutableIntStateOf(0) }
    var snackbarMessage by rememberSaveable { mutableStateOf<Int?>(null) }

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
        showSchedule = true
        viewModel.consumeSharedUrl()
    }

    // Make existing alarms exact when the user grants access, and repair any
    // alarms lost while the app was force-stopped. The scheduler falls back to
    // an inexact alarm when exact-alarm access is unavailable.
    LaunchedEffect(exactAlarmsEnabled) {
        viewModel.rescheduleAll()
    }

    LaunchedEffect(showSchedule, session) {
        if (showSchedule) {
            scheduleViewModel.start(editingId = editingId, sharedUrl = prefillUrl)
        }
    }

    LaunchedEffect(scheduleState.saved) {
        if (!scheduleState.saved) return@LaunchedEffect
        val needsExactAlarm = scheduleState.needsExactAlarm
        showSchedule = false
        snackbarMessage = if (needsExactAlarm) {
            R.string.exact_alarm_saved_warning
        } else {
            R.string.reminder_saved
        }
        scheduleViewModel.consumeSaved()
    }

    LaunchedEffect(snackbarMessage) {
        val messageRes = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = context.getString(messageRes),
            duration = if (messageRes == R.string.exact_alarm_saved_warning) {
                SnackbarDuration.Long
            } else {
                SnackbarDuration.Short
            }
        )
        snackbarMessage = null
    }

    BackHandler(enabled = showSchedule) {
        showSchedule = false
    }

    if (showSchedule) {
        ScheduleScreen(
            state = scheduleState,
            exactAlarmsEnabled = exactAlarmsEnabled,
            notificationsEnabled = notificationsEnabled,
            snackbarHostState = snackbarHostState,
            onBack = { showSchedule = false },
            onUrlChange = scheduleViewModel::updateUrl,
            onNoteChange = scheduleViewModel::updateNote,
            onTypeChange = scheduleViewModel::updateType,
            onDateSelected = scheduleViewModel::updateDate,
            onTimeSelected = scheduleViewModel::updateTime,
            onToggleDay = scheduleViewModel::toggleDay,
            onSave = scheduleViewModel::save,
            onRequestExactAlarms = onRequestExactAlarms,
            onOpenNotificationSettings = onRequestNotificationSettings
        )
    } else {
        HomeScreen(
            reels = reels,
            exactAlarmsEnabled = exactAlarmsEnabled,
            notificationsEnabled = notificationsEnabled,
            snackbarHostState = snackbarHostState,
            onAdd = {
                editingId = null
                prefillUrl = null
                session++
                showSchedule = true
            },
            onEdit = { reel ->
                editingId = reel.id
                prefillUrl = null
                session++
                showSchedule = true
            },
            onDelete = viewModel::delete,
            onTogglePause = viewModel::togglePause,
            onRequestExactAlarms = onRequestExactAlarms,
            onOpenNotificationSettings = onRequestNotificationSettings
        )
    }
}
