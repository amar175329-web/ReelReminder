package com.reelreminder.app.util

import com.reelreminder.app.data.Reel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayGroupingTest {

    private val utc = ZoneId.of("UTC")

    @Test
    fun splitsCarouselTodaysPlanAndComingUpExcludingToday() {
        val today = LocalDate.of(2026, 11, 28)
        val todayMs = today.atTime(18, 0).atZone(utc).toInstant().toEpochMilli()
        val tomorrowMs = today.plusDays(1).atTime(9, 0).atZone(utc).toInstant().toEpochMilli()
        val laterMs = today.plusDays(3).atTime(20, 30).atZone(utc).toInstant().toEpochMilli()

        val rToday = Reel(id = 1, url = "https://www.instagram.com/reel/Code11111/", note = "Today", time = "18:00", nextTriggerAt = todayMs)
        val rTomorrow = Reel(id = 2, url = "https://www.instagram.com/reel/Code22222/", note = "Tomorrow", time = "09:00", nextTriggerAt = tomorrowMs)
        val rLater = Reel(id = 3, url = "https://www.instagram.com/reel/Code33333/", note = "Later", time = "20:30", nextTriggerAt = laterMs)
        val rDone = Reel(id = 4, url = "https://www.instagram.com/reel/Code44444/", note = "Done", time = "08:00", done = true, nextTriggerAt = 0L)

        val grouped = TodayGrouping.group(listOf(rLater, rToday, rDone, rTomorrow), today, utc)

        assertEquals(listOf(1L, 2L, 3L), grouped.carousel.map { it.id })
        assertEquals(listOf(4L, 1L), grouped.todaysPlan.map { it.id })
        assertEquals(listOf(2L, 3L), grouped.comingUp.map { it.id })

        val labelTomorrow = TodayGrouping.formatComingUpLabel(tomorrowMs, today, utc, Locale.ENGLISH)
        assertTrue(labelTomorrow.startsWith("Tomorrow"))
        val labelLater = TodayGrouping.formatComingUpLabel(laterMs, today, utc, Locale.ENGLISH)
        assertTrue(labelLater.contains("1 Dec"))
    }
}
