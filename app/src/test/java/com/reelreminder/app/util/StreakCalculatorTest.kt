package com.reelreminder.app.util

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    private val utc = ZoneId.of("UTC")

    @Test
    fun emptyLogsReturnZeroStreak() {
        val stats = StreakCalculator.compute(emptyList(), LocalDate.of(2026, 11, 28), utc)
        assertEquals(0, stats.currentStreakDays)
        assertEquals(7, stats.last7Days.size)
        assertTrue(stats.last7Days.all { !it })
    }

    @Test
    fun consecutiveDaysEndingTodayCountedAccurately() {
        val today = LocalDate.of(2026, 11, 28)
        val logs = listOf(
            today.atTime(9, 0).atZone(utc).toInstant().toEpochMilli(),
            today.minusDays(1).atTime(23, 59).atZone(utc).toInstant().toEpochMilli(),
            today.minusDays(2).atTime(0, 1).atZone(utc).toInstant().toEpochMilli(),
            // gap on day -3
            today.minusDays(4).atTime(12, 0).atZone(utc).toInstant().toEpochMilli()
        )
        val stats = StreakCalculator.compute(logs, today, utc)
        assertEquals(3, stats.currentStreakDays)
        assertTrue(stats.last7Days[6]) // today
        assertTrue(stats.last7Days[5]) // yesterday
        assertTrue(stats.last7Days[4]) // -2
        assertFalse(stats.last7Days[3]) // -3 gap
        assertTrue(stats.last7Days[2]) // -4
    }

    @Test
    fun streakContinuesIfLastWatchedWasYesterday() {
        val today = LocalDate.of(2026, 11, 28)
        val logs = listOf(
            today.minusDays(1).atTime(20, 0).atZone(utc).toInstant().toEpochMilli(),
            today.minusDays(2).atTime(20, 0).atZone(utc).toInstant().toEpochMilli()
        )
        val stats = StreakCalculator.compute(logs, today, utc)
        assertEquals(2, stats.currentStreakDays)
    }
}
