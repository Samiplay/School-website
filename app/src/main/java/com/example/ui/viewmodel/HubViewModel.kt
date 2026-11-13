package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExamEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.HubDatabase
import com.example.data.local.HubPreferences
import com.example.data.local.HubPrefsState
import com.example.data.local.PackItemEntity
import com.example.data.local.SchoolClassEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.HubRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class HubViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HubRepository = HubRepository(
        dao = HubDatabase.getInstance(application).hubDao(),
        prefs = HubPreferences(application)
    )

    private val isUnlocked = MutableStateFlow(false)
    private val lockError = MutableStateFlow("")
    private val lockShakeCount = MutableStateFlow(0)
    private val showAllTx = MutableStateFlow(false)

    private val timerMode = MutableStateFlow("Focus")
    private val timerStatusLabel = MutableStateFlow("Focus")
    private val timerSeconds = MutableStateFlow(1500)
    private val timerRunning = MutableStateFlow(false)
    private var timerJob: Job? = null

    private data class SchoolRawData(
        val classes: List<SchoolClassEntity>,
        val homework: List<HomeworkEntity>,
        val study: List<StudyTopicEntity>,
        val pack: List<PackItemEntity>,
        val exams: List<ExamEntity>
    )

    private data class TimerState(
        val mode: String,
        val statusLabel: String,
        val seconds: Int,
        val running: Boolean
    )

    private val schoolFlow = combine(
        repository.classesFlow,
        repository.homeworkFlow,
        repository.studyTopicsFlow,
        repository.packItemsFlow,
        repository.examsFlow
    ) { classes, hw, study, pack, exams ->
        SchoolRawData(classes, hw, study, pack, exams)
    }

    private val timerFlow = combine(
        timerMode,
        timerStatusLabel,
        timerSeconds,
        timerRunning
    ) { mode, status, secs, running ->
        TimerState(mode, status, secs, running)
    }

    private val lockFlow = combine(
        isUnlocked,
        lockError,
        lockShakeCount
    ) { unlocked, err, shake ->
        Triple(unlocked, err, shake)
    }

    val uiState: StateFlow<HubUiState> = combine(
        lockFlow,
        repository.prefsFlow,
        schoolFlow,
        repository.transactionsFlow,
        combine(timerFlow, showAllTx) { t, showAll -> t to showAll }
    ) { lock, prefs, school, transactions, timerAndShowAll ->
        val (timer, showAll) = timerAndShowAll
        buildUiState(
            unlocked = lock.first,
            lockError = lock.second,
            lockShakeCount = lock.third,
            prefs = prefs,
            school = school,
            transactions = transactions,
            timer = timer,
            showAll = showAll
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HubUiState()
    )

    private fun buildUiState(
        unlocked: Boolean,
        lockError: String,
        lockShakeCount: Int,
        prefs: HubPrefsState,
        school: SchoolRawData,
        transactions: List<TransactionEntity>,
        timer: TimerState,
        showAll: Boolean
    ): HubUiState {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val todayKey = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tomorrowKey = tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tomorrowDow = tomorrow.dayOfWeek.value % 7
        val monthKey = todayKey.take(7)
        val weekStartKey = today.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val tomorrowClasses = school.classes
            .filter { it.day == tomorrowDow }
            .sortedBy { it.time }

        val classesByDay = HubFormatters.DAY_ORDER
            .associateWith { d -> school.classes.filter { it.day == d }.sortedBy { it.time } }
            .filterValues { it.isNotEmpty() }

        val sortedHomework = school.homework.sortedWith(
            compareBy<HomeworkEntity> { it.done }
                .thenBy { it.due.ifBlank { "9999-99-99" } }
                .thenBy { it.id }
        )

        val dueTomorrowCount = school.homework.count {
            !it.done && it.due.isNotBlank() && it.due <= tomorrowKey
        }

        val sortedStudy = school.study.sortedWith(
            compareBy<StudyTopicEntity> { it.done }
                .thenBy { it.date.ifBlank { "9999-99-99" } }
                .thenBy { it.id }
        )

        val toStudyCount = school.study.count { !it.done }

        val examItems = school.exams
            .sortedBy { it.date }
            .map { exam ->
                val topics = repository.parseTopics(exam.topicsJson)
                val n = HubFormatters.daysUntil(exam.date, today) ?: 0L
                val total = topics.size
                val done = topics.count { it.done }
                val rem = total - done
                val tag = when {
                    n < 0L -> "Passed"
                    n == 0L -> "Today"
                    n == 1L -> "Tomorrow"
                    else -> "In $n days"
                }
                val per = if (n >= 2L && rem > 0) {
                    ceil(rem.toDouble() / (n - 1).toDouble()).toInt()
                } else 0
                val plan = when {
                    n < 0L -> "This exam has passed. You can delete it."
                    n == 0L -> "Exam today. Trust your preparation."
                    n == 1L -> "Exam is tomorrow. Review your weakest topics, pack your bag, and sleep early."
                    total == 0 -> "Add topics next time to get a daily plan."
                    rem == 0 -> "Every topic is covered. Use the days left for practice."
                    else -> "Plan: cover about $per topic${if (per == 1) "" else "s"} a day to finish the day before."
                }
                ExamDisplayItem(
                    entity = exam,
                    topics = topics,
                    daysUntil = n,
                    formattedDate = HubFormatters.formatFullDate(exam.date),
                    tagText = tag,
                    isSoon = n in 0L..3L,
                    studyTip = HubFormatters.STUDY_TIPS[exam.method].orEmpty(),
                    planText = plan
                )
            }

        val weekAhead = (1L..7L).map { offset ->
            val d = today.plusDays(offset)
            val key = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dow = d.dayOfWeek.value % 7
            val classCount = school.classes.count { it.day == dow }
            val dueTasks = school.homework.filter { !it.done && it.due == key }.map { it.text }
            val tests = school.study.filter { !it.done && it.date == key }.map { it.text } +
                school.exams.filter { it.date == key }.map { "${it.subject} exam" }
            WeekDaySummary(
                dayLabel = "${HubFormatters.DAY_NAMES[dow].take(3)} ${d.dayOfMonth}",
                classCount = classCount,
                dueTasks = dueTasks,
                tests = tests
            )
        }

        // Money calculations
        val spentMonth = transactions
            .filter { it.type == "s" && it.date.take(7) == monthKey }
            .sumOf { it.amt }
        val spentToday = transactions
            .filter { it.type == "s" && it.date == todayKey }
            .sumOf { it.amt }
        val spentWeek = transactions
            .filter { it.type == "s" && it.date in weekStartKey..todayKey }
            .sumOf { it.amt }
        val earnedMonth = transactions
            .filter { it.type == "e" && it.date.take(7) == monthKey }
            .sumOf { it.amt }

        val catMap = linkedMapOf<String, Double>()
        transactions
            .filter { it.type == "s" && it.date.take(7) == monthKey }
            .forEach { t ->
                catMap[t.cat] = (catMap[t.cat] ?: 0.0) + t.amt
            }
        val categorySpends = catMap.entries
            .sortedByDescending { it.value }
            .map { (cat, amt) ->
                val ratio = if (spentMonth > 0.0) (amt / spentMonth).toFloat().coerceIn(0f, 1f) else 0f
                CategorySpend(cat, amt, ratio)
            }

        return HubUiState(
            isUnlocked = unlocked,
            lockError = lockError,
            lockShakeCount = lockShakeCount,
            themeMode = prefs.themeMode,
            activeSection = prefs.activeSection,
            schoolTab = prefs.schoolTab,
            moneyTab = prefs.moneyTab,
            todayDate = today,
            tomorrowDate = tomorrow,
            tomorrowDayOfWeek = tomorrowDow,
            tomorrowKey = tomorrowKey,
            tomorrowClasses = tomorrowClasses,
            classesByDay = classesByDay,
            homework = sortedHomework,
            dueTomorrowCount = dueTomorrowCount,
            studyTopics = sortedStudy,
            toStudyCount = toStudyCount,
            packItems = school.pack,
            exams = examItems,
            weekAhead = weekAhead,
            timerMode = timer.mode,
            timerStatusLabel = timer.statusLabel,
            timerSeconds = timer.seconds,
            timerRunning = timer.running,
            currency = prefs.currency,
            transactions = transactions,
            showAllTransactions = showAll,
            spentThisMonth = spentMonth,
            spentToday = spentToday,
            spentLast7Days = spentWeek,
            earnedThisMonth = earnedMonth,
            monthlyBudget = prefs.monthlyBudget,
            categorySpends = categorySpends,
            goalName = prefs.goalName,
            goalTarget = prefs.goalTarget,
            goalSaved = prefs.goalSaved
        )
    }

    // Lock screen
    fun submitPassword(input: String) {
        if (verifyPassword(input)) {
            lockError.value = ""
            isUnlocked.value = true
        } else {
            lockError.value = "Incorrect password. Try again."
            lockShakeCount.value += 1
        }
    }

    fun lockApp() {
        isUnlocked.value = false
        lockError.value = ""
    }

    private fun verifyPassword(value: String): Boolean {
        val codes = intArrayOf(49, 114, 50, 114, 51, 114, 52, 82)
        if (value.length != codes.size) return false
        for (i in codes.indices) {
            if (value[i].code != codes[i]) return false
        }
        return true
    }

    // Theme & Navigation
    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            repository.setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
        }
    }

    fun selectSection(section: String) {
        viewModelScope.launch { repository.setActiveSection(section) }
    }

    fun selectSchoolTab(tab: String) {
        viewModelScope.launch { repository.setSchoolTab(tab) }
    }

    fun selectMoneyTab(tab: String) {
        viewModelScope.launch { repository.setMoneyTab(tab) }
    }

    // School actions
    fun addClass(day: Int, time: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addClass(day, time.ifBlank { "08:00" }, trimmed)
        }
    }

    fun deleteClass(id: Long) {
        viewModelScope.launch { repository.deleteClass(id) }
    }

    fun addHomework(text: String, due: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addHomework(trimmed, due) }
    }

    fun toggleHomework(item: HomeworkEntity) {
        viewModelScope.launch { repository.toggleHomework(item) }
    }

    fun deleteHomework(id: Long) {
        viewModelScope.launch { repository.deleteHomework(id) }
    }

    fun addStudyTopic(text: String, date: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addStudyTopic(trimmed, date) }
    }

    fun toggleStudyTopic(item: StudyTopicEntity) {
        viewModelScope.launch { repository.toggleStudyTopic(item) }
    }

    fun deleteStudyTopic(id: Long) {
        viewModelScope.launch { repository.deleteStudyTopic(id) }
    }

    fun addPackItem(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addPackItem(trimmed) }
    }

    fun togglePackItem(item: PackItemEntity) {
        val tomorrowKey = uiState.value.tomorrowKey
        viewModelScope.launch { repository.togglePackItem(item, tomorrowKey) }
    }

    fun deletePackItem(id: Long) {
        viewModelScope.launch { repository.deletePackItem(id) }
    }

    fun addExam(subject: String, date: String, rawTopics: String, method: String) {
        val s = subject.trim()
        if (s.isEmpty() || date.isBlank()) return
        viewModelScope.launch { repository.addExam(s, date, rawTopics, method) }
    }

    fun toggleExamTopic(exam: ExamEntity, topicIndex: Int) {
        viewModelScope.launch { repository.toggleExamTopic(exam, topicIndex) }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch { repository.deleteExam(id) }
    }

    // Study Timer
    fun toggleTimer() {
        if (timerRunning.value) {
            stopTimer()
        } else {
            timerRunning.value = true
            timerStatusLabel.value = timerMode.value
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (timerRunning.value) {
                    delay(1000L)
                    val next = timerSeconds.value - 1
                    if (next <= 0) {
                        stopTimer()
                        val nextMode = if (timerMode.value == "Focus") "Break" else "Focus"
                        setTimerMode(nextMode)
                        timerStatusLabel.value = if (nextMode == "Break") "Time for a break" else "Back to focus"
                    } else {
                        timerSeconds.value = next
                    }
                }
            }
        }
    }

    fun resetTimer() {
        stopTimer()
        setTimerMode(timerMode.value)
    }

    fun swapTimerMode() {
        stopTimer()
        val nextMode = if (timerMode.value == "Focus") "Break" else "Focus"
        setTimerMode(nextMode)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        timerRunning.value = false
    }

    private fun setTimerMode(mode: String) {
        timerMode.value = mode
        timerStatusLabel.value = mode
        timerSeconds.value = if (mode == "Focus") 1500 else 300
    }

    // Money actions
    fun addTransaction(amountText: String, type: String, category: String, note: String, date: String) {
        val amount = amountText.toDoubleOrNull() ?: return
        if (amount <= 0.0) return
        val fallbackDate = uiState.value.todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.addTransaction(
                amt = amount,
                type = type,
                cat = category,
                note = note.trim(),
                date = date.ifBlank { fallbackDate }
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { repository.deleteTransaction(id) }
    }

    fun toggleShowAllTransactions() {
        showAllTx.value = !showAllTx.value
    }

    fun setMonthlyBudget(amountText: String) {
        val v = amountText.toDoubleOrNull() ?: return
        if (v < 0.0) return
        viewModelScope.launch { repository.setMonthlyBudget(v) }
    }

    fun setSavingsGoal(name: String, targetText: String) {
        val trimmed = name.trim()
        val target = targetText.toDoubleOrNull() ?: return
        if (trimmed.isEmpty() || target <= 0.0) return
        viewModelScope.launch { repository.setSavingsGoal(trimmed, target) }
    }

    fun addSavings(amountText: String) {
        val v = amountText.toDoubleOrNull() ?: return
        if (v <= 0.0) return
        val current = uiState.value.goalSaved
        viewModelScope.launch { repository.addSavings(current + v) }
    }

    fun removeSavingsGoal() {
        viewModelScope.launch { repository.removeSavingsGoal() }
    }

    fun setCurrency(symbol: String) {
        viewModelScope.launch { repository.setCurrency(symbol) }
    }

    fun eraseAllMoneyData() {
        viewModelScope.launch { repository.eraseAllMoneyData() }
    }
}
