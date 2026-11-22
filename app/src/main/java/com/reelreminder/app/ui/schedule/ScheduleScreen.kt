package com.reelreminder.app.ui.schedule

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.reelreminder.app.R
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.ui.components.PermissionBanner
import com.reelreminder.app.ui.components.formatDateLabel
import com.reelreminder.app.ui.components.formatTimeOfDay
import com.reelreminder.app.ui.components.formatTriggerText
import com.reelreminder.app.util.TimeUtils
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    exactAlarmsEnabled: Boolean,
    notificationsEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onUrlChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (ScheduleType) -> Unit,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onToggleDay: (Int) -> Unit,
    onSave: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.reelId == null) {
                                R.string.schedule_new_title
                            } else {
                                R.string.schedule_edit_title
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!notificationsEnabled) {
                PermissionBanner(
                    title = stringResource(R.string.notifications_off_title),
                    body = stringResource(R.string.notifications_off_body),
                    actionLabel = stringResource(R.string.enable),
                    onAction = onOpenNotificationSettings
                )
            }
            if (!exactAlarmsEnabled) {
                PermissionBanner(
                    title = stringResource(R.string.exact_alarms_off_title),
                    body = stringResource(R.string.exact_alarms_off_body),
                    actionLabel = stringResource(R.string.allow),
                    onAction = onRequestExactAlarms
                )
            }

            OutlinedTextField(
                value = state.url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.link_label)) },
                placeholder = { Text(stringResource(R.string.link_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Link,
                        contentDescription = stringResource(R.string.cd_link)
                    )
                },
                isError = state.urlError != null,
                supportingText = state.urlError?.let { error ->
                    {
                        Text(
                            stringResource(
                                if (error == UrlError.EMPTY) {
                                    R.string.link_error_empty
                                } else {
                                    R.string.link_error_invalid
                                }
                            )
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.note_label)) },
                placeholder = { Text(stringResource(R.string.note_placeholder)) },
                minLines = 1,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.repeat_label),
                    style = MaterialTheme.typography.titleSmall
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ScheduleType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = state.type == type,
                            onClick = { onTypeChange(type) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ScheduleType.entries.size
                            ),
                            label = { Text(stringResource(type.labelRes())) }
                        )
                    }
                }
            }

            when (state.type) {
                ScheduleType.ONCE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PickerCard(
                            icon = Icons.Outlined.CalendarMonth,
                            label = stringResource(R.string.date_label),
                            value = formatDateLabel(
                                TimeUtils.utcMillisToLocalDate(state.dateUtcMillis)
                            ),
                            contentDescription = stringResource(R.string.cd_date),
                            onClick = { showDatePicker = true }
                        )
                        PickerCard(
                            icon = Icons.Outlined.AccessTime,
                            label = stringResource(R.string.time_label),
                            value = formatTimeOfDay(state.hour, state.minute),
                            contentDescription = stringResource(R.string.cd_time),
                            onClick = { showTimePicker = true }
                        )
                    }
                }

                ScheduleType.DAILY -> {
                    PickerCard(
                        icon = Icons.Outlined.AccessTime,
                        label = stringResource(R.string.time_label),
                        value = formatTimeOfDay(state.hour, state.minute),
                        contentDescription = stringResource(R.string.cd_time),
                        onClick = { showTimePicker = true }
                    )
                }

                ScheduleType.WEEKLY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.weekday_label),
                                style = MaterialTheme.typography.titleSmall
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (1..7).forEach { day ->
                                    FilterChip(
                                        selected = day in state.selectedDays,
                                        onClick = { onToggleDay(day) },
                                        label = {
                                            Text(
                                                DayOfWeek.of(day).getDisplayName(
                                                    TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                            if (state.noDaysSelected) {
                                Text(
                                    text = stringResource(R.string.days_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        PickerCard(
                            icon = Icons.Outlined.AccessTime,
                            label = stringResource(R.string.time_label),
                            value = formatTimeOfDay(state.hour, state.minute),
                            contentDescription = stringResource(R.string.cd_time),
                            onClick = { showTimePicker = true }
                        )
                    }
                }
            }

            NextReminderCard(state)

            if (state.paused) {
                Text(
                    text = stringResource(R.string.edit_paused_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.saving))
                } else {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(
                            if (state.reelId == null) R.string.save_new else R.string.save_edit
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.hour,
            initialMinute = state.minute,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Composable
private fun PickerCard(
    icon: ImageVector,
    label: String,
    value: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NextReminderCard(state: ScheduleUiState) {
    val previewAt = state.previewAt
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Alarm, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.next_reminder_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = when {
                        state.onceInPast -> stringResource(R.string.once_past_error)
                        previewAt != null -> formatTriggerText(previewAt)
                        else -> stringResource(R.string.next_reminder_none)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.onceInPast) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@StringRes
private fun ScheduleType.labelRes(): Int = when (this) {
    ScheduleType.ONCE -> R.string.repeat_once
    ScheduleType.DAILY -> R.string.repeat_daily
    ScheduleType.WEEKLY -> R.string.repeat_weekly
}
