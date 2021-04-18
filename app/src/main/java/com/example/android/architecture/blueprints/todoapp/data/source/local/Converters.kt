package com.example.android.architecture.blueprints.todoapp.data

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.*

object Converters {
    //https://developer.android.com/training/data-storage/room/referencing-data
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}