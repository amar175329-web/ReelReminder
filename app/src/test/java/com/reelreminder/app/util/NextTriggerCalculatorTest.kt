package com.reelreminder.app.util

import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ScheduleType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class NextTriggerCalculatorTest {

    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-01-01T08:00:00Z") // Thursday

    @Test
    fun dailyUsesTodayWhenTimeIsStillAhead() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.DAILY,
            time = LocalTime.of(9, 0),
            days = emptySet(),
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-01-01T09:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun dailyMovesToTomorrowWhenTimeHasPassed() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.DAILY,
            time = LocalTime.of(8, 0),
            days = emptySet(),
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-01-02T08:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun weeklyFindsTheNextSelectedWeekday() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.WEEKLY,
            time = LocalTime.of(9, 0),
            days = setOf(1), // Monday
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-01-05T09:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun weeklyCanUseTodayWhenTheTimeIsStillAhead() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.WEEKLY,
            time = LocalTime.of(9, 0),
            days = setOf(4), // Thursday
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-01-01T09:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun weeklySkipsToNextWeekWhenTodaysTimeHasPassed() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.WEEKLY,
            time = LocalTime.of(8, 0),
            days = setOf(4), // Thursday
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-01-08T08:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun weeklyReturnsNullWithoutDays() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.WEEKLY,
            time = LocalTime.of(9, 0),
            days = emptySet(),
            onceDate = null,
            now = now,
            zone = zone
        )
        assertEquals(null, result)
    }

    @Test
    fun onceUsesTheChosenDateAndTime() {
        val result = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.ONCE,
            time = LocalTime.of(12, 30),
            days = emptySet(),
            onceDate = LocalDate.of(2026, 2, 1),
            now = now,
            zone = zone
        )
        assertEquals(Instant.parse("2026-02-01T12:30:00Z").toEpochMilli(), result)
    }

    @Test
    fun nextAfterKeepsFutureOnceTrigger() {
        val trigger = Instant.parse("2026-01-01T10:00:00Z").toEpochMilli()
        val reel = Reel(
            id = 1,
            url = "https://example.com",
            scheduleType = ScheduleType.ONCE,
            time = "10:00",
            nextTriggerAt = trigger
        )
        assertEquals(trigger, NextTriggerCalculator.nextAfter(reel, now, zone))
    }

    @Test
    fun nextAfterFiresMissedOnceReminderSoon() {
        val reel = Reel(
            id = 1,
            url = "https://example.com",
            scheduleType = ScheduleType.ONCE,
            time = "07:00",
            nextTriggerAt = Instant.parse("2026-01-01T07:00:00Z").toEpochMilli()
        )
        assertEquals(
            now.plusSeconds(5).toEpochMilli(),
            NextTriggerCalculator.nextAfter(reel, now, zone)
        )
    }
}
