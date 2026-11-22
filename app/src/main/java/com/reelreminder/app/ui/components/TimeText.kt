package com.reelreminder.app.ui.components

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.reelreminder.app.R
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun formatTriggerText(triggerAt: Long): String {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val dateTime = remember(triggerAt, zone) {
        Instant.ofEpochMilli(triggerAt).atZone(zone)
    }
    val today = LocalDate.now(zone)
    val timeText = remember(triggerAt, context) {
        AndroidDateFormat.getTimeFormat(context).format(Date(triggerAt))
    }

    return when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.today_at, timeText)
        today.plusDays(1) -> stringResource(R.string.tomorrow_at, timeText)
        else -> {
            val pattern = if (dateTime.year == today.year) "EEE, d MMM" else "d MMM yyyy"
            val dateText = remember(triggerAt, pattern) {
                DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(dateTime)
            }
            "$dateText · $timeText"
        }
    }
}

@Composable
fun formatDateLabel(date: LocalDate): String = remember(date) {
    DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()).format(date)
}

@Composable
fun formatTimeOfDay(hour: Int, minute: Int): String {
    val context = LocalContext.current
    return remember(hour, minute, context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        AndroidDateFormat.getTimeFormat(context).format(calendar.time)
    }
}

@Composable
fun repeatLabel(reel: Reel): String = when (reel.scheduleType) {
    ScheduleType.ONCE -> stringResource(R.string.repeat_once)
    ScheduleType.DAILY -> stringResource(R.string.repeat_daily)
    ScheduleType.WEEKLY -> {
        val days = reel.daySet()
            .sorted()
            .joinToString(", ") {
                DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        if (days.isBlank()) {
            stringResource(R.string.repeat_weekly)
        } else {
            stringResource(R.string.weekly_days, days)
        }
    }
}
