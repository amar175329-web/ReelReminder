package com.reelreminder.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The three supported repeat modes.
 *
 * Stored as its name through [Converters] so the database stays readable.
 */
enum class ScheduleType {
    ONCE,
    DAILY,
    WEEKLY
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
    val createdAt: Long = System.currentTimeMillis()
)

fun Reel.daySet(): Set<Int> =
    daysOfWeek.split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..7 }
        .toSet()
