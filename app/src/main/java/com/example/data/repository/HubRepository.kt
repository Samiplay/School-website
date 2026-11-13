package com.example.data.repository

import com.example.data.local.ExamEntity
import com.example.data.local.ExamTopic
import com.example.data.local.HomeworkEntity
import com.example.data.local.HubDao
import com.example.data.local.HubPreferences
import com.example.data.local.HubPrefsState
import com.example.data.local.PackItemEntity
import com.example.data.local.SchoolClassEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.TransactionEntity
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HubRepository(
    private val dao: HubDao,
    private val prefs: HubPreferences
) {
    private val json = Json { ignoreUnknownKeys = true }

    val prefsFlow: Flow<HubPrefsState> = prefs.prefsFlow
    val classesFlow: Flow<List<SchoolClassEntity>> = dao.observeClasses()
    val homeworkFlow: Flow<List<HomeworkEntity>> = dao.observeHomework()
    val studyTopicsFlow: Flow<List<StudyTopicEntity>> = dao.observeStudyTopics()
    val packItemsFlow: Flow<List<PackItemEntity>> = dao.observePackItems()
    val examsFlow: Flow<List<ExamEntity>> = dao.observeExams()
    val transactionsFlow: Flow<List<TransactionEntity>> = dao.observeTransactions()

    suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)
    suspend fun setActiveSection(section: String) = prefs.setActiveSection(section)
    suspend fun setSchoolTab(tab: String) = prefs.setSchoolTab(tab)
    suspend fun setMoneyTab(tab: String) = prefs.setMoneyTab(tab)

    // Classes
    suspend fun addClass(day: Int, time: String, name: String) {
        dao.insertClass(SchoolClassEntity(day = day, time = time, name = name))
    }

    suspend fun deleteClass(id: Long) = dao.deleteClass(id)

    // Homework
    suspend fun addHomework(text: String, due: String) {
        dao.insertHomework(HomeworkEntity(text = text, due = due, done = false))
    }

    suspend fun toggleHomework(item: HomeworkEntity) {
        dao.updateHomework(item.copy(done = !item.done))
    }

    suspend fun deleteHomework(id: Long) = dao.deleteHomework(id)

    // Study topics
    suspend fun addStudyTopic(text: String, date: String) {
        dao.insertStudyTopic(StudyTopicEntity(text = text, date = date, done = false))
    }

    suspend fun toggleStudyTopic(item: StudyTopicEntity) {
        dao.updateStudyTopic(item.copy(done = !item.done))
    }

    suspend fun deleteStudyTopic(id: Long) = dao.deleteStudyTopic(id)

    // Bag pack items
    suspend fun addPackItem(text: String) {
        dao.insertPackItem(PackItemEntity(text = text, doneDate = ""))
    }

    suspend fun togglePackItem(item: PackItemEntity, tomorrowKey: String) {
        val nextDone = if (item.doneDate == tomorrowKey) "" else tomorrowKey
        dao.updatePackItem(item.copy(doneDate = nextDone))
    }

    suspend fun deletePackItem(id: Long) = dao.deletePackItem(id)

    // Exams
    fun parseTopics(topicsJson: String): List<ExamTopic> {
        if (topicsJson.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ExamTopic>>(topicsJson) }.getOrDefault(emptyList())
    }

    suspend fun addExam(subject: String, date: String, rawTopics: String, method: String) {
        val topicList = rawTopics.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { ExamTopic(t = it, done = false) }
        dao.insertExam(
            ExamEntity(
                subject = subject,
                date = date,
                method = method,
                topicsJson = json.encodeToString(topicList)
            )
        )
    }

    suspend fun toggleExamTopic(exam: ExamEntity, topicIndex: Int) {
        val current = parseTopics(exam.topicsJson).toMutableList()
        if (topicIndex in current.indices) {
            current[topicIndex] = current[topicIndex].copy(done = !current[topicIndex].done)
            dao.updateExam(exam.copy(topicsJson = json.encodeToString(current)))
        }
    }

    suspend fun deleteExam(id: Long) = dao.deleteExam(id)

    // Money
    suspend fun addTransaction(amt: Double, type: String, cat: String, note: String, date: String) {
        dao.insertTransaction(
            TransactionEntity(
                amt = kotlin.math.round(amt * 100.0) / 100.0,
                type = type,
                cat = cat,
                note = note,
                date = date
            )
        )
    }

    suspend fun deleteTransaction(id: Long) = dao.deleteTransaction(id)
    suspend fun setMonthlyBudget(amount: Double) = prefs.setMonthlyBudget(amount)
    suspend fun setCurrency(symbol: String) = prefs.setCurrency(symbol)
    suspend fun setSavingsGoal(name: String, target: Double) = prefs.setSavingsGoal(name, target, 0.0)
    suspend fun addSavings(newSaved: Double) = prefs.updateGoalSaved(kotlin.math.round(newSaved * 100.0) / 100.0)
    suspend fun removeSavingsGoal() = prefs.clearGoal()

    suspend fun eraseAllMoneyData() {
        dao.deleteAllTransactions()
        prefs.clearMoneySettings()
    }
}
