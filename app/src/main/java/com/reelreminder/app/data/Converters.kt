package com.reelreminder.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromScheduleType(type: ScheduleType): String = type.name

    @TypeConverter
    fun toScheduleType(name: String): ScheduleType =
        ScheduleType.entries.firstOrNull { it.name == name } ?: ScheduleType.ONCE

    @TypeConverter
    fun fromRepeatUnit(unit: RepeatUnit): String = unit.name

    @TypeConverter
    fun toRepeatUnit(name: String): RepeatUnit =
        RepeatUnit.entries.firstOrNull { it.name == name } ?: RepeatUnit.DAYS

    @TypeConverter
    fun fromEndType(type: EndType): String = type.name

    @TypeConverter
    fun toEndType(name: String): EndType =
        EndType.entries.firstOrNull { it.name == name } ?: EndType.NEVER
}
