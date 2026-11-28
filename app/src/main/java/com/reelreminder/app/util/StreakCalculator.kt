package com.reelreminder.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StreakStats(
    val currentStreakDays: Int,
    /** Oldest-to-newest 7-day activity flags ending with [today]. */
    val last7Days: List<Boolean>
)

/**
 * Computes consecutive local-day watch streak from real WatchLog timestamps.
 * New installs with 0 logs always return 0 (no fake/demo numbers).
 */
object StreakCalculator {
    fun compute(
        watchedTimestampsMillis: List<Long>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): StreakStats {
        val activeDays = watchedTimestampsMillis
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .toSet()

        val last7 = (6 downTo 0).map { offset ->
            today.minusDays(offset.toLong()) in activeDays
        }

        val startDay = when {
            today in activeDays -> today
            today.minusDays(1) in activeDays -> today.minusDays(1)
            else -> return StreakStats(currentStreakDays = 0, last7Days = last7)
        }

        var streak = 0
        var cursor = startDay
        while (cursor in activeDays) {
            streak++
            cursor = cursor.minusDays(1)
        }

        return StreakStats(currentStreakDays = streak, last7Days = last7)
    }
}
