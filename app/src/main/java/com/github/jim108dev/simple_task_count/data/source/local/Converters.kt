package com.github.jim108dev.simple_task_count.data

import androidx.room.TypeConverter
import java.util.*

object Converters {

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?) = timestamp?.let { Date(timestamp) }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: Date?) = date?.time

}