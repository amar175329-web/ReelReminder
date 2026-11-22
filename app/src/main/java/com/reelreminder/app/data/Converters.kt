package com.reelreminder.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromScheduleType(value: ScheduleType): String = value.name

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType =
        runCatching { ScheduleType.valueOf(value) }.getOrDefault(ScheduleType.ONCE)
}
