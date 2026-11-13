package com.example.ui.components

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object HubDateUtils {
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayKey(): String = LocalDate.now().format(isoDate)

    fun tomorrowKey(): String = LocalDate.now().plusDays(1).format(isoDate)

    fun monthKey(): String = todayKey().take(7)

    fun weekAgoKey(): String = LocalDate.now().minusDays(6).format(isoDate)

    fun parseDate(dateKey: String): LocalDate? =
        if (dateKey.isBlank()) null
        else runCatching { LocalDate.parse(dateKey, isoDate) }.getOrNull()

    fun daysUntil(dateKey: String): Long? {
        val target = parseDate(dateKey) ?: return null
        return ChronoUnit.DAYS.between(LocalDate.now(), target)
    }

    /**
     * Converts Java DayOfWeek to JS Date.getDay() numbering: 0=Sunday, 1=Monday .. 6=Saturday
     */
    fun jsDayOfWeek(date: LocalDate): Int = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    fun dayName(jsDay: Int): String = when (jsDay) {
        0 -> "Sunday"
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        else -> ""
    }

    fun formatTime12h(time24: String): String {
        val parts = time24.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val min = parts.getOrNull(1)?.padStart(2, '0') ?: "00"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val suffix = if (hour < 12) "am" else "pm"
        return "$displayHour:$min $suffix"
    }

    fun formatMonthDay(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))

    fun formatShortDate(dateKey: String): String {
        val date = parseDate(dateKey) ?: return dateKey
        return date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }

    fun formatFullDate(dateKey: String): String {
        val date = parseDate(dateKey) ?: return dateKey
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    fun formatMoney(currencySymbol: String, amount: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currencySymbol${nf.format(amount)}"
    }
}
