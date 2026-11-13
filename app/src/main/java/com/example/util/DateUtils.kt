package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object DateUtils {
    val DAY_NAMES = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val WEEK_ORDER = listOf(1, 2, 3, 4, 5, 6, 0) // Mon..Sun

    private fun keyFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun dateKey(cal: Calendar): String = keyFormat().format(cal.time)

    fun todayCalendar(): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun tomorrowCalendar(): Calendar = todayCalendar().apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }

    fun todayKey(): String = dateKey(todayCalendar())

    fun tomorrowKey(): String = dateKey(tomorrowCalendar())

    fun tomorrowDayIndex(): Int = tomorrowCalendar().get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun..6=Sat

    fun tomorrowDayName(): String = DAY_NAMES[tomorrowDayIndex()]

    fun tomorrowSubtitle(): String {
        val fmt = SimpleDateFormat("MMMM d", Locale.getDefault())
        return "${fmt.format(tomorrowCalendar().time)}. Here is what to get ready tonight."
    }

    fun parseKey(key: String): Calendar? {
        if (key.isBlank()) return null
        return try {
            val date = keyFormat().parse(key) ?: return null
            Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun daysUntil(key: String): Int? {
        val target = parseKey(key) ?: return null
        val today = todayCalendar()
        val diffMillis = target.timeInMillis - today.timeInMillis
        return (diffMillis.toDouble() / 86_400_000.0).roundToInt()
    }

    fun dueLabel(key: String): Pair<String, DueUrgency> {
        if (key.isBlank()) return "No date" to DueUrgency.NORMAL
        val n = daysUntil(key) ?: return key to DueUrgency.NORMAL
        return when {
            n < 0 -> "Overdue" to DueUrgency.LATE
            n == 0 -> "Today" to DueUrgency.SOON
            n == 1 -> "Tomorrow" to DueUrgency.SOON
            n < 7 -> {
                val cal = parseKey(key)!!
                val idx = cal.get(Calendar.DAY_OF_WEEK) - 1
                DAY_NAMES[idx].take(3) to DueUrgency.NORMAL
            }
            else -> {
                val cal = parseKey(key)!!
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                fmt.format(cal.time) to DueUrgency.NORMAL
            }
        }
    }

    fun formatLongDate(key: String): String {
        val cal = parseKey(key) ?: return key
        val fmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        return fmt.format(cal.time)
    }

    fun formatShortDate(key: String): String {
        val cal = parseKey(key) ?: return key
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        return fmt.format(cal.time)
    }

    fun format12Hour(time24: String): String {
        val parts = time24.split(":")
        if (parts.size != 2) return time24
        val h = parts[0].toIntOrNull() ?: return time24
        val m = parts[1]
        val h12 = if (h % 12 == 0) 12 else h % 12
        val suffix = if (h < 12) "am" else "pm"
        return "$h12:$m $suffix"
    }

    fun weekAheadDays(): List<WeekDaySlot> {
        val base = todayCalendar()
        return (1..7).map { offset ->
            val cal = (base.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val idx = cal.get(Calendar.DAY_OF_WEEK) - 1
            WeekDaySlot(
                dateKey = dateKey(cal),
                dayOfWeek = idx,
                shortLabel = "${DAY_NAMES[idx].take(3)} ${cal.get(Calendar.DAY_OF_MONTH)}"
            )
        }
    }

    fun weekAgoKey(): String {
        val cal = todayCalendar().apply { add(Calendar.DAY_OF_YEAR, -6) }
        return dateKey(cal)
    }

    fun currentMonthKey(): String = todayKey().take(7)

    fun daysRemainingInMonth(): Int {
        val cal = todayCalendar()
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        return (maxDay - currentDay + 1).coerceAtLeast(1)
    }

    fun formatMoney(currency: String, amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currency${fmt.format(amount)}"
    }

    fun dateKeyFromMillis(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        // DatePicker returns UTC midnight; convert using UTC calendar to avoid off-by-one day
        val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH) + 1,
            utc.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentTimeFormatted(): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }
}

enum class DueUrgency {
    NORMAL, SOON, LATE
}

data class WeekDaySlot(
    val dateKey: String,
    val dayOfWeek: Int,
    val shortLabel: String
)
