package com.reelreminder.app.util

import com.reelreminder.app.data.EndType
import com.reelreminder.app.data.RepeatUnit
import com.reelreminder.app.data.ScheduleType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NextTriggerCalculatorTest {

    private val utc = ZoneId.of("UTC")

    @Test
    fun monthlyClamps31stToFebruaryEnd() {
        val anchor = LocalDate.of(2026, 1, 31)
        val now = anchor.atTime(10, 0).atZone(utc).toInstant()
        val next = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.MONTHLY,
            time = LocalTime.of(9, 0),
            days = emptySet(),
            onceDate = anchor,
            now = now,
            zone = utc
        )
        assertNotNull(next)
        val nextDate = Instant.ofEpochMilli(next!!).atZone(utc).toLocalDate()
        assertEquals(LocalDate.of(2026, 2, 28), nextDate)
    }

    @Test
    fun yearlyClampsLeapDay29FebTo28FebInNonLeapYear() {
        val anchor = LocalDate.of(2024, 2, 29)
        val now = anchor.atTime(10, 0).atZone(utc).toInstant()
        val next = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.YEARLY,
            time = LocalTime.of(9, 0),
            days = emptySet(),
            onceDate = anchor,
            now = now,
            zone = utc
        )
        assertNotNull(next)
        val nextDate = Instant.ofEpochMilli(next!!).atZone(utc).toLocalDate()
        assertEquals(LocalDate.of(2025, 2, 28), nextDate)
    }

    @Test
    fun customFiveYearsAdvancesCorrectly() {
        val anchor = LocalDate.of(2026, 11, 27)
        val now = anchor.atTime(21, 0).atZone(utc).toInstant()
        val next = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.CUSTOM,
            time = LocalTime.of(20, 0),
            days = emptySet(),
            onceDate = anchor,
            now = now,
            zone = utc,
            repeatInterval = 5,
            repeatUnit = RepeatUnit.YEARS
        )
        assertNotNull(next)
        val nextDate = Instant.ofEpochMilli(next!!).atZone(utc).toLocalDate()
        assertEquals(LocalDate.of(2031, 11, 27), nextDate)
    }

    @Test
    fun endsAfterCountStopsSchedulingOnceReached() {
        val anchor = LocalDate.of(2026, 11, 27)
        val now = anchor.atTime(10, 0).atZone(utc).toInstant()
        val next = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.DAILY,
            time = LocalTime.of(9, 0),
            days = emptySet(),
            onceDate = anchor,
            now = now,
            zone = utc,
            endType = EndType.AFTER_COUNT,
            endAfterCount = 3,
            firedCount = 3
        )
        assertNull(next)
    }

    @Test
    fun endsOnDateStopsSchedulingAfterEndDate() {
        val anchor = LocalDate.of(2026, 11, 27)
        val now = anchor.atTime(21, 0).atZone(utc).toInstant()
        val next = NextTriggerCalculator.nextTriggerFor(
            type = ScheduleType.DAILY,
            time = LocalTime.of(20, 0),
            days = emptySet(),
            onceDate = anchor,
            now = now,
            zone = utc,
            endType = EndType.ON_DATE,
            endDate = LocalDate.of(2026, 11, 27)
        )
        assertNull(next)
    }

    @Test
    fun summaryStringMatchesExpectedFormat() {
        val summary = NextTriggerCalculator.formatSummary(
            startDate = LocalDate.of(2026, 11, 27),
            time = LocalTime.of(20, 0),
            type = ScheduleType.CUSTOM,
            days = emptySet(),
            repeatInterval = 5,
            repeatUnit = RepeatUnit.YEARS,
            endType = EndType.NEVER,
            endDate = null,
            endAfterCount = null,
            locale = Locale.ENGLISH
        )
        assertTrue(summary.contains("Fri, 27 Nov 2026"))
        assertTrue(summary.contains("8:00 pm"))
        assertTrue(summary.contains("repeats every 5 years"))
        assertTrue(summary.contains("Never ends."))
    }
}
