package me.dkim19375.dailycoinstax.data

import java.time.LocalDate

data class DateData(
    var year: Int,
    var month: Int,
    var day: Int,
) {
    operator fun compareTo(other: DateData): Int = when {
        year > other.year -> 1
        year < other.year -> -1
        month > other.month -> 1
        month < other.month -> -1
        day > other.day -> 1
        day < other.day -> -1
        else -> 0
    }

    companion object {
        fun getToday(): DateData {
            val date = LocalDate.now()
            return DateData(date.year, date.monthValue, date.dayOfMonth)
        }
    }
}