package com.example.android.architecture.blueprints.todoapp.data

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.*

object Converters {

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?) = timestamp?.let { Date(timestamp) }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: Date?) = date?.time

}