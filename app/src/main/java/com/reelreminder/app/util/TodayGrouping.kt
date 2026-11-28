package com.reelreminder.app.util

import com.reelreminder.app.data.Reel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TodaySections(
    /** Next 6 pending reels sorted by nearest trigger. */
    val carousel: List<Reel>,
    /** Reels whose next trigger is today (plus reels watched today). */
    val todaysPlan: List<Reel>,
    /** Reels whose next trigger is tomorrow or later (today excluded), nearest first. */
    val comingUp: List<Reel>
)

object TodayGrouping {
    fun group(
        reels: List<Reel>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): TodaySections {
        val pendingSorted = reels
            .filter { !it.done && !it.paused && it.nextTriggerAt > 0L }
            .sortedBy { it.nextTriggerAt }

        val carousel = pendingSorted.take(6)

        val todaysPlan = reels
            .filter { reel ->
                if (reel.done) {
                    true
                } else if (reel.nextTriggerAt <= 0L) {
                    false
                } else {
                    val triggerDate = Instant.ofEpochMilli(reel.nextTriggerAt).atZone(zone).toLocalDate()
                    !triggerDate.isAfter(today)
                }
            }
            .sortedBy { it.time }

        val comingUp = reels
            .filter { reel ->
                !reel.done && !reel.paused && reel.nextTriggerAt > 0L &&
                    Instant.ofEpochMilli(reel.nextTriggerAt).atZone(zone).toLocalDate().isAfter(today)
            }
            .sortedBy { it.nextTriggerAt }

        return TodaySections(
            carousel = carousel,
            todaysPlan = todaysPlan,
            comingUp = comingUp
        )
    }

    fun formatComingUpLabel(
        triggerMillis: Long,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.ENGLISH
    ): String {
        val zdt = Instant.ofEpochMilli(triggerMillis).atZone(zone)
        val date = zdt.toLocalDate()
        val timeText = zdt.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a", locale)).lowercase(locale)
        val dayLabel = if (date == today.plusDays(1)) {
            "Tomorrow"
        } else {
            date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))
        }
        return "$dayLabel · $timeText"
    }
}
