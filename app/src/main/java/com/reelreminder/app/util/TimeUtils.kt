package com.reelreminder.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseTime(value: String): LocalTime? =
        runCatching { LocalTime.parse(value, timeFormatter) }.getOrNull()

    fun formatTimeValue(hour: Int, minute: Int): String =
        "%02d:%02d".format(hour, minute)

    fun formatTimeValue(time: LocalTime): String = time.format(timeFormatter)

    /**
     * Material 3 date pickers work with UTC midnight millis. Our stored date is
     * always a UTC midnight so there is no timezone drift while picking.
     */
    fun localDateToUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun utcMillisToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun localDateFromEpoch(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
}
