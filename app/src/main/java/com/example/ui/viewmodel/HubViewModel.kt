package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ClassSessionEntity
import com.example.data.local.ExamEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.HubPreferences
import com.example.data.local.MainSection
import com.example.data.local.PackItemEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.ThemePreference
import com.example.data.local.TransactionEntity
import com.example.data.repository.HubRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StudyTimerState(
    val mode: String = "Focus",
    val statusLabel: String = "Focus",
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false
)

sealed interface BackupExportEvent {
    data class Success(val schoolCount: Int, val moneyCount: Int) : BackupExportEvent
    data class Failure(val message: String) : BackupExportEvent
}

data class HubUiState(
    val isUnlocked: Boolean = false,
    val lockError: String? = null,
    val lockShakeCounter: Int = 0,
    val preferences: HubPreferences = HubPreferences(),
    val classes: List<ClassSessionEntity> = emptyList(),
    val homework: List<HomeworkEntity> = emptyList(),
    val studyTopics: List<StudyTopicEntity> = emptyList(),
    val packItems: List<PackItemEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val savingsGoal: SavingsGoalEntity? = null,
    val timer: StudyTimerState = StudyTimerState(),
    val showAllHistory: Boolean = false,
    val isExportingBackup: Boolean = false,
    val lastBackupSummary: String? = null
)

class HubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HubRepository

    init {
        val db = AppDatabase.getInstance(application)
        val prefs = PreferencesManager(application)
        repository = HubRepository(
            schoolDao = db.schoolDao(),
            moneyDao = db.moneyDao(),
            preferencesManager = prefs
        )
    }

    private val _lockState = MutableStateFlow(LockSessionState())
    private val _timerState = MutableStateFlow(StudyTimerState())
    private val _showAllHistory = MutableStateFlow(false)
    private val _exportState = MutableStateFlow(ExportUiState())
    private val _backupEvents = MutableSharedFlow<BackupExportEvent>()
    val backupEvents: SharedFlow<BackupExportEvent> = _backupEvents.asSharedFlow()

    private var timerJob: Job? = null

    private data class LockSessionState(
        val isUnlocked: Boolean = false,
        val error: String? = null,
        val shakeCounter: Int = 0
    )

    private data class ExportUiState(
        val isExporting: Boolean = false,
        val lastSummary: String? = null
    )

    private data class SchoolDataBundle(
        val classes: List<ClassSessionEntity>,
        val homework: List<HomeworkEntity>,
        val studyTopics: List<StudyTopicEntity>,
        val packItems: List<PackItemEntity>,
        val exams: List<ExamEntity>
    )

    private val schoolBundleFlow = combine(
        repository.classes,
        repository.homework,
        repository.studyTopics,
        repository.packItems,
        repository.exams
    ) { classes, hw, study, pack, exams ->
        SchoolDataBundle(classes, hw, study, pack, exams)
    }

    private val moneyBundleFlow = combine(
        repository.transactions,
        repository.savingsGoal,
        _showAllHistory
    ) { tx, goal, showAll ->
        Triple(tx, goal, showAll)
    }

    val uiState: StateFlow<HubUiState> = combine(
        _lockState,
        repository.preferences,
        schoolBundleFlow,
        moneyBundleFlow,
        combine(_timerState, _exportState) { timer, export -> timer to export }
    ) { lock, prefs, school, money, (timer, export) ->
        HubUiState(
            isUnlocked = lock.isUnlocked,
            lockError = lock.error,
            lockShakeCounter = lock.shakeCounter,
            preferences = prefs,
            classes = school.classes,
            homework = school.homework,
            studyTopics = school.studyTopics,
            packItems = school.packItems,
            exams = school.exams,
            transactions = money.first,
            savingsGoal = money.second,
            timer = timer,
            showAllHistory = money.third,
            isExportingBackup = export.isExporting,
            lastBackupSummary = export.lastSummary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HubUiState()
    )

    // Matching password check from index.html: [49, 114, 50, 114, 51, 114, 52, 82]
    private val expectedCharCodes = intArrayOf(49, 114, 50, 114, 51, 114, 52, 82)

    fun attemptUnlock(password: String): Boolean {
        val matches = password.length == expectedCharCodes.size &&
            password.indices.all { i -> password[i].code == expectedCharCodes[i] }
        return if (matches) {
            _lockState.value = LockSessionState(isUnlocked = true, error = null, shakeCounter = 0)
            true
        } else {
            val errorText = getApplication<Application>().getString(R.string.lock_error)
            _lockState.update {
                it.copy(
                    isUnlocked = false,
                    error = errorText,
                    shakeCounter = it.shakeCounter + 1
                )
            }
            false
        }
    }

    fun clearLockError() {
        _lockState.update { it.copy(error = null) }
    }

    fun lockApp() {
        _lockState.update { it.copy(isUnlocked = false, error = null) }
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            repository.setTheme(if (currentlyDark) ThemePreference.LIGHT else ThemePreference.DARK)
        }
    }

    fun selectMainSection(section: MainSection) {
        viewModelScope.launch { repository.setActiveSection(section) }
    }

    fun selectSchoolTab(tab: String) {
        viewModelScope.launch { repository.setSchoolTab(tab) }
    }

    fun selectMoneyTab(tab: String) {
        viewModelScope.launch { repository.setMoneyTab(tab) }
    }

    // School actions
    fun addClass(dayOfWeek: Int, time: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addClass(dayOfWeek, time.ifBlank { "08:00" }, trimmed)
        }
    }

    fun deleteClass(id: Long) {
        viewModelScope.launch { repository.deleteClass(id) }
    }

    fun addHomework(text: String, dueDate: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addHomework(trimmed, dueDate) }
    }

    fun toggleHomework(item: HomeworkEntity) {
        viewModelScope.launch { repository.toggleHomework(item) }
    }

    fun deleteHomework(id: Long) {
        viewModelScope.launch { repository.deleteHomework(id) }
    }

    fun addStudyTopic(text: String, testDate: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addStudyTopic(trimmed, testDate) }
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

    fun togglePackItem(item: PackItemEntity, tomorrowKey: String) {
        viewModelScope.launch { repository.togglePackItem(item, tomorrowKey) }
    }

    fun deletePackItem(id: Long) {
        viewModelScope.launch { repository.deletePackItem(id) }
    }

    fun addExam(subject: String, date: String, rawTopics: String, method: String) {
        val cleanSubject = subject.trim()
        val cleanDate = date.trim()
        if (cleanSubject.isEmpty() || cleanDate.isEmpty()) return
        val topics = rawTopics.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        viewModelScope.launch { repository.addExam(cleanSubject, cleanDate, topics, method) }
    }

    fun toggleExamTopic(exam: ExamEntity, topicIndex: Int) {
        viewModelScope.launch { repository.toggleExamTopic(exam, topicIndex) }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch { repository.deleteExam(id) }
    }

    // Study Timer
    fun toggleTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val current = _timerState.value
                if (!current.isRunning) break
                val nextSeconds = current.secondsRemaining - 1
                if (nextSeconds <= 0) {
                    val nextMode = if (current.mode == "Focus") "Break" else "Focus"
                    val nextDuration = if (nextMode == "Focus") 25 * 60 else 5 * 60
                    val nextLabel = if (nextMode == "Break") "Time for a break" else "Back to focus"
                    _timerState.value = StudyTimerState(
                        mode = nextMode,
                        statusLabel = nextLabel,
                        secondsRemaining = nextDuration,
                        isRunning = false
                    )
                    break
                } else {
                    _timerState.update { it.copy(secondsRemaining = nextSeconds) }
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        val mode = _timerState.value.mode
        _timerState.value = StudyTimerState(
            mode = mode,
            statusLabel = mode,
            secondsRemaining = if (mode == "Focus") 25 * 60 else 5 * 60,
            isRunning = false
        )
    }

    fun swapTimerMode() {
        pauseTimer()
        val nextMode = if (_timerState.value.mode == "Focus") "Break" else "Focus"
        _timerState.value = StudyTimerState(
            mode = nextMode,
            statusLabel = nextMode,
            secondsRemaining = if (nextMode == "Focus") 25 * 60 else 5 * 60,
            isRunning = false
        )
    }

    // Money actions
    fun addTransaction(amount: Double, type: String, category: String, note: String, date: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addTransaction(amount, type, category, note.trim(), date)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { repository.deleteTransaction(id) }
    }

    fun toggleShowAllHistory() {
        _showAllHistory.update { !it }
    }

    fun setMonthlyBudget(budget: Double) {
        if (budget < 0.0) return
        viewModelScope.launch { repository.setMonthlyBudget(budget) }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch { repository.setCurrencySymbol(symbol) }
    }

    fun setSavingsGoal(name: String, target: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || target <= 0.0) return
        viewModelScope.launch { repository.setSavingsGoal(trimmed, target) }
    }

    fun addToSavingsGoal(amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch { repository.addToSavingsGoal(amount) }
    }

    fun deleteSavingsGoal() {
        viewModelScope.launch { repository.deleteSavingsGoal() }
    }

    fun eraseAllMoneyData() {
        viewModelScope.launch { repository.eraseAllMoneyData() }
    }

    // JSON Backup Export (School + Money)
    fun defaultBackupFilename(): String {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "myhub_backup_$today.json"
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            _exportState.update { it.copy(isExporting = true) }
            try {
                val result = repository.buildBackupJson()
                withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(result.jsonContent.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    } ?: throw IllegalStateException("Unable to open output stream")
                }
                val summary = getApplication<Application>().getString(
                    R.string.settings_export_success,
                    result.schoolItemCount,
                    result.moneyItemCount
                )
                _exportState.value = ExportUiState(isExporting = false, lastSummary = summary)
                _backupEvents.emit(BackupExportEvent.Success(result.schoolItemCount, result.moneyItemCount))
            } catch (e: Exception) {
                val errMsg = e.message?.takeIf { it.isNotBlank() }
                    ?: getApplication<Application>().getString(R.string.settings_export_error)
                _exportState.update { it.copy(isExporting = false) }
                _backupEvents.emit(BackupExportEvent.Failure(errMsg))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
