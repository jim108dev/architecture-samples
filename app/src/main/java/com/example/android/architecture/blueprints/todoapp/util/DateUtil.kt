package com.example.android.architecture.blueprints.todoapp.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class DateUtil {
    companion object {
        private const val DATE_PATTERN = "MM/dd/yy"
        private val formatter = SimpleDateFormat(DATE_PATTERN, Locale.US)
        fun convertDateToString(date: Date): String {
            return formatter.format(date)
        }
        fun convertStringToDate(str: String): Date {
            return  formatter.parse(str)
        }
    }
}