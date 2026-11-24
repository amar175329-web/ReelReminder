package com.reelreminder.app.ui.schedule

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.draw.shadow
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
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.ui.components.ReelPoster
import com.reelreminder.app.ui.components.formatPosterTime
import com.reelreminder.app.ui.components.formatTimeOfDay
import com.reelreminder.app.ui.theme.DarkB1
import com.reelreminder.app.ui.theme.DarkChip
import com.reelreminder.app.ui.theme.DarkEdge
import com.reelreminder.app.ui.theme.DarkGlass
import com.reelreminder.app.ui.theme.DarkInk
import com.reelreminder.app.ui.theme.DarkMute
import com.reelreminder.app.ui.theme.LightB1
import com.reelreminder.app.ui.theme.LightChip
import com.reelreminder.app.ui.theme.LightEdge
import com.reelreminder.app.ui.theme.LightGlass
import com.reelreminder.app.ui.theme.LightInk
import com.reelreminder.app.ui.theme.LightMute
import com.reelreminder.app.ui.theme.RedDot
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.ui.theme.TealPrimary
import com.reelreminder.app.ui.theme.parseColorPair
import com.reelreminder.app.util.UrlExtractor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReelSheet(
    state: ScheduleUiState,
    categories: List<Category>,
    selectedCategoryId: Long,
    onCategoryChange: (Long) -> Unit,
    onAddNewCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onUrlChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (ScheduleType) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onToggleDay: (Int) -> Unit,
    onSave: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) DarkB1 else LightB1
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute
    val chipBg = if (isDark) DarkChip else LightChip
    val edgeColor = if (isDark) DarkEdge else LightEdge

    val currentCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()

    var showTimePicker by remember { mutableStateOf(false) }
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val quickTimes = listOf(
        "Morning" to Pair(7, 0),
        "Midday" to Pair(12, 30),
        "Evening" to Pair(20, 0)
    )

    val previewTimeStr = String.format("%02d:%02d", state.hour, state.minute)
    val previewReel = remember(state.url, state.note, state.type, previewTimeStr, state.selectedDays, selectedCategoryId) {
        Reel(
            id = state.reelId ?: 0L,
            url = state.url,
            note = state.note,
            categoryId = selectedCategoryId,
            scheduleType = state.type,
            time = previewTimeStr,
            daysOfWeek = state.selectedDays.sorted().joinToString(",")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 42.dp, height = 5.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(chipBg)
            )
        },
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 32.dp)
        ) {
            // Live Preview Card (Poster + Inputs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Live mini poster
                Box(modifier = Modifier.width(110.dp)) {
                    ReelPoster(
                        reel = previewReel,
                        category = currentCategory,
                        aspectRatio = 3f / 4f,
                        cornerRadius = 22.dp,
                        headlineSize = 13.sp,
                        playButtonSize = 0.dp,
                        onPlay = null
                    )
                }

                // URL & Note Fields
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.url,
                        onValueChange = onUrlChange,
                        label = { Text(stringResource(R.string.link_label)) },
                        placeholder = { Text(stringResource(R.string.link_placeholder)) },
                        isError = state.urlError != null,
                        supportingText = state.urlError?.let {
                            {
                                Text(
                                    stringResource(
                                        if (it == UrlError.EMPTY) R.string.link_error_empty
                                        else R.string.link_error_invalid
                                    ),
                                    color = RedDot
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = onNoteChange,
                        label = { Text(stringResource(R.string.note_label)) },
                        placeholder = { Text(stringResource(R.string.note_placeholder)) },
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category Section
            Text(
                text = stringResource(R.string.category_label),
                color = inkColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val colors = parseColorPair(cat.gradientStart, cat.gradientEnd)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) inkColor else chipBg)
                            .clickable { onCategoryChange(cat.id) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors))
                            )
                            Text(
                                text = cat.name,
                                color = if (isSelected) sheetBg else inkColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // "+ New" category chip
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { showNewCategoryInput = true }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "+ New",
                        color = muteColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (showNewCategoryInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("Name it, then save") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = newCategoryName.trim()
                            if (trimmed.isNotEmpty()) {
                                onAddNewCategory(trimmed)
                                newCategoryName = ""
                                showNewCategoryInput = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // When Section
            Text(
                text = stringResource(R.string.when_label),
                color = inkColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickTimes.forEach { (label, time) ->
                    val isSelected = state.hour == time.first && state.minute == time.second
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) inkColor else chipBg)
                            .clickable { onTimeSelected(time.first, time.second) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "$label (${formatTimeOfDay(time.first, time.second)})",
                            color = if (isSelected) sheetBg else inkColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Custom Time button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { showTimePicker = true }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = muteColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatTimeOfDay(state.hour, state.minute),
                            color = inkColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Repeat Section
            Text(
                text = stringResource(R.string.repeat_label),
                color = inkColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ScheduleType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { onTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ScheduleType.entries.size
                        ),
                        label = {
                            Text(
                                text = when (type) {
                                    ScheduleType.ONCE -> "Once"
                                    ScheduleType.DAILY -> "Daily"
                                    ScheduleType.WEEKLY -> "Weekly"
                                }
                            )
                        }
                    )
                }
            }

            // If Weekly: On these days
            if (state.type == ScheduleType.WEEKLY) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.weekday_label),
                    color = inkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..7).forEach { day ->
                        val isSelected = day in state.selectedDays
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) inkColor else chipBg)
                                .clickable { onToggleDay(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabels[day - 1],
                                color = if (isSelected) sheetBg else inkColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // Save Button (Big Gradient Teal Button with glow)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x6614B8A6), spotColor = Color(0x6614B8A6))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF14B8A6), Color(0xFF0A7F86))
                        )
                    )
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.save_new),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
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
