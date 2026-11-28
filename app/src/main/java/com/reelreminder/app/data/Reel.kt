package com.reelreminder.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleType {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}

enum class RepeatUnit(val label: String, val singular: String) {
    DAYS("Days", "day"),
    WEEKS("Weeks", "week"),
    MONTHS("Months", "month"),
    YEARS("Years", "year")
}

enum class EndType(val label: String) {
    NEVER("Never"),
    ON_DATE("On date"),
    AFTER_COUNT("After N times")
}

@Entity(
    tableName = "reels",
    indices = [Index(value = ["categoryId"])]
)
data class Reel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val note: String = "",
    val categoryId: Long = 1L,
    val scheduleType: ScheduleType = ScheduleType.ONCE,
    /** Local wall-clock time in HH:mm form. */
    val time: String = "09:00",
    /** Comma separated ISO weekday numbers, Monday = 1 ... Sunday = 7. Empty for non-weekly. */
    val daysOfWeek: String = "",
    /** Epoch millis of the next alarm. Zero once the reminder is done. */
    val nextTriggerAt: Long = 0L,
    val done: Boolean = false,
    /** Paused reminders keep their data but have no alarm scheduled. */
    val paused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val repeatInterval: Int = 1,
    val repeatUnit: RepeatUnit = RepeatUnit.DAYS,
    val endType: EndType = EndType.NEVER,
    /** ISO-8601 local date (yyyy-MM-dd) when endType == ON_DATE. */
    val endDateIso: String? = null,
    val endAfterCount: Int? = null,
    val firedCount: Int = 0,
    /** Optional app-internal file path for owner-picked cover image (Issue 12). */
    val coverUri: String? = null
)

fun Reel.daySet(): Set<Int> =
    daysOfWeek.split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..7 }
        .toSet()
