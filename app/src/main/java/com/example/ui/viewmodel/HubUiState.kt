package com.example.ui.viewmodel

import com.example.data.local.ExamEntity
import com.example.data.local.ExamTopic
import com.example.data.local.HomeworkEntity
import com.example.data.local.PackItemEntity
import com.example.data.local.SchoolClassEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.TransactionEntity
import com.example.ui.theme.ThemeMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class ExamDisplayItem(
    val entity: ExamEntity,
    val topics: List<ExamTopic>,
    val daysUntil: Long,
    val formattedDate: String,
    val tagText: String,
    val isSoon: Boolean,
    val studyTip: String,
    val planText: String
)

data class WeekDaySummary(
    val dayLabel: String,
    val classCount: Int,
    val dueTasks: List<String>,
    val tests: List<String>
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val ratio: Float
)

data class HubUiState(
    val isUnlocked: Boolean = false,
    val lockError: String = "",
    val lockShakeCount: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val activeSection: String = "school",
    val schoolTab: String = "tomorrow",
    val moneyTab: String = "log",
    // School
    val todayDate: LocalDate = LocalDate.now(),
    val tomorrowDate: LocalDate = LocalDate.now().plusDays(1),
    val tomorrowDayOfWeek: Int = (LocalDate.now().plusDays(1).dayOfWeek.value % 7),
    val tomorrowKey: String = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val tomorrowClasses: List<SchoolClassEntity> = emptyList(),
    val classesByDay: Map<Int, List<SchoolClassEntity>> = emptyMap(),
    val homework: List<HomeworkEntity> = emptyList(),
    val dueTomorrowCount: Int = 0,
    val studyTopics: List<StudyTopicEntity> = emptyList(),
    val toStudyCount: Int = 0,
    val packItems: List<PackItemEntity> = emptyList(),
    val exams: List<ExamDisplayItem> = emptyList(),
    val weekAhead: List<WeekDaySummary> = emptyList(),
    // Timer
    val timerMode: String = "Focus",
    val timerStatusLabel: String = "Focus",
    val timerSeconds: Int = 1500,
    val timerRunning: Boolean = false,
    // Money
    val currency: String = "$",
    val transactions: List<TransactionEntity> = emptyList(),
    val showAllTransactions: Boolean = false,
    val spentThisMonth: Double = 0.0,
    val spentToday: Double = 0.0,
    val spentLast7Days: Double = 0.0,
    val earnedThisMonth: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val categorySpends: List<CategorySpend> = emptyList(),
    val goalName: String = "",
    val goalTarget: Double = 0.0,
    val goalSaved: Double = 0.0
)

object HubFormatters {
    val DAY_NAMES = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val DAY_ORDER = listOf(1, 2, 3, 4, 5, 6, 0)

    val STUDY_METHODS = listOf(
        "Flashcards",
        "Practice questions",
        "Summary notes",
        "Past papers",
        "Teach it out loud"
    )

    val STUDY_TIPS = mapOf(
        "Flashcards" to "Write a question on one side and the answer on the other. Say answers out loud, and put the ones you miss in a pile to repeat.",
        "Practice questions" to "Try questions before looking at your notes. Mark the ones you got wrong and redo only those.",
        "Summary notes" to "Shrink each topic to one page from memory, then check your notes to see what you missed.",
        "Past papers" to "Do one paper with a timer and no notes, mark it, then study only the gaps.",
        "Teach it out loud" to "Explain each topic as if teaching a friend. Wherever you get stuck is what to study next."
    )

    val MONEY_CATEGORIES = listOf("Food", "Transport", "Shopping", "Fun", "Bills", "School", "Other")
    val CURRENCY_SYMBOLS = listOf("$", "£", "€", "¥", "₹", "R", "kr")

    private val fullDateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    private val monthDayFmt = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())
    private val shortDateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    fun formatTime12h(time24: String): String {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.padStart(2, '0') ?: "00"
        val h12 = if (h % 12 == 0) 12 else h % 12
        val suffix = if (h < 12) "am" else "pm"
        return "$h12:$m $suffix"
    }

    fun parseDateOrNull(key: String): LocalDate? {
        if (key.isBlank()) return null
        return runCatching { LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
    }

    fun daysUntil(key: String, today: LocalDate): Long? {
        val d = parseDateOrNull(key) ?: return null
        return ChronoUnit.DAYS.between(today, d)
    }

    fun formatMonthDay(date: LocalDate): String = date.format(monthDayFmt)

    fun formatFullDate(key: String): String {
        val d = parseDateOrNull(key) ?: return key
        return d.format(fullDateFmt)
    }

    fun formatShortDate(key: String): String {
        val d = parseDateOrNull(key) ?: return key
        return d.format(shortDateFmt)
    }

    fun formatDueLabel(key: String, today: LocalDate): Pair<String, String> {
        if (key.isBlank()) return "No date" to ""
        val n = daysUntil(key, today) ?: return "No date" to ""
        return when {
            n < 0 -> "Overdue" to "late"
            n == 0L -> "Today" to "soon"
            n == 1L -> "Tomorrow" to "soon"
            n < 7L -> {
                val d = parseDateOrNull(key)!!
                DAY_NAMES[d.dayOfWeek.value % 7].take(3) to ""
            }
            else -> formatShortDate(key) to ""
        }
    }

    fun formatMoney(currency: String, amount: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currency${nf.format(amount)}"
    }

    fun formatTimer(seconds: Int): String {
        val mins = (seconds / 60).toString().padStart(2, '0')
        val secs = (seconds % 60).toString().padStart(2, '0')
        return "$mins:$secs"
    }

    fun remainingDaysInMonth(today: LocalDate): Int {
        val lengthOfMonth = YearMonth.from(today).lengthOfMonth()
        return maxOf(1, lengthOfMonth - today.dayOfMonth + 1)
    }
}
