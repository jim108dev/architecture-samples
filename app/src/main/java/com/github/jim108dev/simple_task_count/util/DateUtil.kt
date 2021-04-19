package com.github.jim108dev.simple_task_count.util

import java.text.SimpleDateFormat
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