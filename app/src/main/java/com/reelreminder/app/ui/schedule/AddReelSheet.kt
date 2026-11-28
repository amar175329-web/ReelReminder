package com.reelreminder.app.ui.schedule

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.R
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.EndType
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.RepeatUnit
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.ui.components.ReelPoster
import com.reelreminder.app.ui.theme.DarkB1
import com.reelreminder.app.ui.theme.DarkChip
import com.reelreminder.app.ui.theme.DarkEdge
import com.reelreminder.app.ui.theme.DarkInk
import com.reelreminder.app.ui.theme.DarkMute
import com.reelreminder.app.ui.theme.LightB1
import com.reelreminder.app.ui.theme.LightChip
import com.reelreminder.app.ui.theme.LightEdge
import com.reelreminder.app.ui.theme.LightInk
import com.reelreminder.app.ui.theme.LightMute
import com.reelreminder.app.ui.theme.LocalAppDarkTheme
import com.reelreminder.app.ui.theme.RedDot
import com.reelreminder.app.ui.theme.ReelReminderTheme
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.ui.theme.TealPrimary
import com.reelreminder.app.ui.theme.ThemeMode
import com.reelreminder.app.ui.theme.parseColorPair
import com.reelreminder.app.util.CoverStorage
import com.reelreminder.app.util.TimeUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReelSheet(
    state: ScheduleUiState,
    categories: List<Category>,
    selectedCategoryId: Long,
    onCategoryChange: (Long) -> Unit,
    onAddNewCategory: (String) -> Unit,
    onCoverChanged: (String?) -> Unit,
    onDismiss: () -> Unit,
    onUrlChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (ScheduleType) -> Unit,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onQuickInOneHour: () -> Unit,
    onQuickTonight8Pm: () -> Unit,
    onQuickTomorrow7Am: () -> Unit,
    onRepeatIntervalChange: (Int) -> Unit,
    onRepeatUnitChange: (RepeatUnit) -> Unit,
    onCustomPreset: (Int, RepeatUnit) -> Unit,
    onEndTypeChange: (EndType) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    onEndAfterCountChange: (Int) -> Unit,
    onToggleDay: (Int) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalAppDarkTheme.current
    val sheetBg = if (isDark) DarkB1 else LightB1
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute
    val chipBg = if (isDark) DarkChip else LightChip
    val edgeColor = if (isDark) DarkEdge else LightEdge

    val currentCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()

    var showDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val copied = CoverStorage.copyFromUri(context, uri)
            if (copied != null) {
                onCoverChanged(copied)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val previewTimeStr = String.format(Locale.US, "%02d:%02d", state.hour, state.minute)
    val dateText = remember(state.dateUtcMillis) {
        TimeUtils.utcMillisToLocalDate(state.dateUtcMillis)
            .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    }
    val endDateText = remember(state.endDateUtcMillis) {
        TimeUtils.utcMillisToLocalDate(state.endDateUtcMillis)
            .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    }
    val timeText = remember(state.hour, state.minute) {
        LocalTime.of(state.hour, state.minute)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
            .lowercase(Locale.getDefault())
    }

    val previewReel = remember(state, previewTimeStr) {
        Reel(
            id = state.reelId ?: 0L,
            url = state.url,
            note = state.note.ifBlank { "Your note shows here" },
            categoryId = state.categoryId,
            scheduleType = state.type,
            time = previewTimeStr,
            daysOfWeek = state.selectedDays.sorted().joinToString(","),
            repeatInterval = state.repeatInterval,
            repeatUnit = state.repeatUnit,
            coverUri = state.coverUri
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Preview + Link + Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                ReelPoster(
                    reel = previewReel,
                    category = currentCategory,
                    aspectRatio = 3f / 4f,
                    cornerRadius = 20.dp,
                    headlineSize = 13.sp,
                    playButtonSize = 0.dp,
                    modifier = Modifier.width(104.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.url,
                        onValueChange = onUrlChange,
                        placeholder = { Text("Instagram reel link", fontSize = 13.sp, color = muteColor) },
                        singleLine = true,
                        isError = state.urlError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = edgeColor,
                            focusedTextColor = inkColor,
                            unfocusedTextColor = inkColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.urlError != null) {
                        Text(
                            text = if (state.urlError == UrlError.EMPTY) {
                                "Paste an Instagram reel link"
                            } else {
                                "Only Instagram reel links work"
                            },
                            color = RedDot,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = onNoteChange,
                        placeholder = { Text("Why did you save it?", fontSize = 13.sp, color = muteColor) },
                        maxLines = 2,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = edgeColor,
                            focusedTextColor = inkColor,
                            unfocusedTextColor = inkColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Issue 12: Photo Picker cover image (no storage permission)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.coverUri == null) "Add cover" else "Change cover", fontSize = 12.sp)
                        }
                        if (state.coverUri != null) {
                            TextButton(
                                onClick = {
                                    CoverStorage.deleteFile(state.coverUri)
                                    onCoverChanged(null)
                                }
                            ) {
                                Text("Remove", fontSize = 12.sp, color = RedDot)
                            }
                        }
                    }
                }
            }

            // Category Chips
            Text("Category", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = inkColor)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat.id == selectedCategoryId
                    val colors = parseColorPair(cat.gradientStart, cat.gradientEnd)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) inkColor else chipBg)
                            .clickable { onCategoryChange(cat.id) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors))
                        )
                        Text(
                            text = cat.name,
                            color = if (selected) sheetBg else inkColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { showNewCategoryInput = !showNewCategoryInput }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+ New", color = inkColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            if (showNewCategoryInput) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("Collection name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onAddNewCategory(newCategoryName.trim())
                                newCategoryName = ""
                                showNewCategoryInput = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Add")
                    }
                }
            }

            // Issue 9: Always-filled Date + Time + Quick Chips
            Text("When", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = inkColor)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChip("In 1 hour", chipBg, inkColor, onQuickInOneHour)
                QuickChip("Tonight 8 pm", chipBg, inkColor, onQuickTonight8Pm)
                QuickChip("Tomorrow 7 am", chipBg, inkColor, onQuickTomorrow7Am)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(1.dp, edgeColor, RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Pick date", tint = TealDark, modifier = Modifier.size(18.dp))
                    Text(dateText, color = inkColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(1.dp, edgeColor, RoundedCornerShape(16.dp))
                        .clickable { showTimePicker = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.AccessTime, contentDescription = "Pick time", tint = TealDark, modifier = Modifier.size(18.dp))
                    Text(timeText, color = inkColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            if (state.onceInPast) {
                Text(
                    text = "Pick a future date and time for a one-time reminder.",
                    color = RedDot,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Issue 10: Repeat UI (Once / Daily / Weekly / Monthly / Yearly / Custom)
            Text("Repeat", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = inkColor)
            val repeatTypes = listOf(
                ScheduleType.ONCE to "Once",
                ScheduleType.DAILY to "Daily",
                ScheduleType.WEEKLY to "Weekly",
                ScheduleType.MONTHLY to "Monthly",
                ScheduleType.YEARLY to "Yearly",
                ScheduleType.CUSTOM to "Custom"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeatTypes.forEach { (type, label) ->
                    val on = state.type == type
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (on) TealDark else chipBg)
                            .clickable { onTypeChange(type) }
                            .padding(horizontal = 15.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (on) Color.White else inkColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (state.type == ScheduleType.WEEKLY) {
                val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { (dayNum, label) ->
                        val on = dayNum in state.selectedDays
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (on) TealDark else chipBg)
                                .clickable { onToggleDay(dayNum) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (on) Color.White else inkColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                if (state.noDaysSelected) {
                    Text("Pick at least one day", color = RedDot, fontSize = 12.sp)
                }
            }

            if (state.type == ScheduleType.CUSTOM) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipBg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Repeat every", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = inkColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onRepeatIntervalChange((state.repeatInterval - 1).coerceAtLeast(1)) }
                            ) {
                                Icon(Icons.Outlined.Remove, contentDescription = "Decrease interval", tint = inkColor)
                            }
                            Text(
                                text = "${state.repeatInterval}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = inkColor,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onRepeatIntervalChange(state.repeatInterval + 1) }
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Increase interval", tint = inkColor)
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RepeatUnit.entries.forEach { unit ->
                            val on = state.repeatUnit == unit
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (on) inkColor else sheetBg)
                                    .clickable { onRepeatUnitChange(unit) }
                                    .padding(horizontal = 13.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = unit.label,
                                    color = if (on) sheetBg else inkColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text("Quick picks", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = muteColor)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickChip("Every 2 weeks", sheetBg, inkColor) { onCustomPreset(2, RepeatUnit.WEEKS) }
                        QuickChip("3 months", sheetBg, inkColor) { onCustomPreset(3, RepeatUnit.MONTHS) }
                        QuickChip("5 years", sheetBg, inkColor) { onCustomPreset(5, RepeatUnit.YEARS) }
                        QuickChip("10 years", sheetBg, inkColor) { onCustomPreset(10, RepeatUnit.YEARS) }
                    }
                }
            }

            if (state.type != ScheduleType.ONCE) {
                Text("Ends", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = inkColor)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EndType.entries.forEach { endType ->
                        val on = state.endType == endType
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (on) TealDark else chipBg)
                                .clickable { onEndTypeChange(endType) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = endType.label,
                                color = if (on) Color.White else inkColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (state.endType == EndType.ON_DATE) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(chipBg)
                            .clickable { showEndDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TealDark, modifier = Modifier.size(16.dp))
                        Text("End date: $endDateText", color = inkColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                } else if (state.endType == EndType.AFTER_COUNT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Stop after", color = inkColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { onEndAfterCountChange((state.endAfterCount - 1).coerceAtLeast(1)) }) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Decrease count", tint = inkColor)
                        }
                        Text("${state.endAfterCount} times", color = inkColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        IconButton(onClick = { onEndAfterCountChange(state.endAfterCount + 1) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Increase count", tint = inkColor)
                        }
                    }
                }
            }

            // Live summary line (Issue 10)
            if (state.summaryText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.summaryText,
                        color = inkColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Issue 13: Explicit Save + Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.38f)
                        .height(54.dp),
                    shape = CircleShape
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = inkColor)
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .weight(0.62f)
                        .height(54.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                ) {
                    Text(
                        text = if (state.reelId == null) stringResource(R.string.save_new) else stringResource(R.string.save_edit),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Issue 9: Light-themed DatePickerDialog (Cancel keeps old value)
    if (showDatePicker) {
        ReelReminderTheme(themeMode = ThemeMode.LIGHT) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateUtcMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let(onDateSelected)
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }

    if (showEndDatePicker) {
        ReelReminderTheme(themeMode = ThemeMode.LIGHT) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.endDateUtcMillis)
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let(onEndDateSelected)
                            showEndDatePicker = false
                        }
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }

    // Issue 9: Light-themed TimePicker dialog (Cancel keeps old value)
    if (showTimePicker) {
        ReelReminderTheme(themeMode = ThemeMode.LIGHT) {
            val timeState = rememberTimePickerState(
                initialHour = state.hour,
                initialMinute = state.minute,
                is24Hour = DateFormat.is24HourFormat(context)
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = Color.White,
                title = { Text("Pick time", fontWeight = FontWeight.ExtraBold, color = LightInk) },
                text = { TimePicker(state = timeState) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onTimeSelected(timeState.hour, timeState.minute)
                            showTimePicker = false
                        }
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    bg: Color,
    ink: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
