package com.example.data.repository

import com.example.data.local.ClassSessionEntity
import com.example.data.local.ExamEntity
import com.example.data.local.ExamTopic
import com.example.data.local.HomeworkEntity
import com.example.data.local.HubPreferences
import com.example.data.local.MainSection
import com.example.data.local.MoneyDao
import com.example.data.local.PackItemEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.SchoolDao
import com.example.data.local.StudyTopicEntity
import com.example.data.local.ThemePreference
import com.example.data.local.TransactionEntity
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HubRepository(
    private val schoolDao: SchoolDao,
    private val moneyDao: MoneyDao,
    private val preferencesManager: PreferencesManager
) {
    private val exportJson = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    val preferences: Flow<HubPreferences> = preferencesManager.preferencesFlow

    // School streams
    val classes: Flow<List<ClassSessionEntity>> = schoolDao.observeClasses()
    val homework: Flow<List<HomeworkEntity>> = schoolDao.observeHomework()
    val studyTopics: Flow<List<StudyTopicEntity>> = schoolDao.observeStudyTopics()
    val packItems: Flow<List<PackItemEntity>> = schoolDao.observePackItems()
    val exams: Flow<List<ExamEntity>> = schoolDao.observeExams()

    // Money streams
    val transactions: Flow<List<TransactionEntity>> = moneyDao.observeTransactions()
    val savingsGoal: Flow<SavingsGoalEntity?> = moneyDao.observeSavingsGoal()

    // Preferences updates
    suspend fun setTheme(theme: ThemePreference) = preferencesManager.setTheme(theme)
    suspend fun setActiveSection(section: MainSection) = preferencesManager.setActiveSection(section)
    suspend fun setSchoolTab(tab: String) = preferencesManager.setSchoolTab(tab)
    suspend fun setMoneyTab(tab: String) = preferencesManager.setMoneyTab(tab)
    suspend fun setMonthlyBudget(budget: Double) = preferencesManager.setMonthlyBudget(budget)
    suspend fun setCurrencySymbol(symbol: String) = preferencesManager.setCurrencySymbol(symbol)

    // School mutations
    suspend fun addClass(dayOfWeek: Int, time: String, name: String) {
        schoolDao.insertClass(ClassSessionEntity(dayOfWeek = dayOfWeek, time = time, name = name))
    }

    suspend fun deleteClass(id: Long) = schoolDao.deleteClass(id)

    suspend fun addHomework(text: String, dueDate: String) {
        schoolDao.insertHomework(HomeworkEntity(text = text, dueDate = dueDate, done = false))
    }

    suspend fun toggleHomework(item: HomeworkEntity) {
        schoolDao.updateHomework(item.copy(done = !item.done))
    }

    suspend fun deleteHomework(id: Long) = schoolDao.deleteHomework(id)

    suspend fun addStudyTopic(text: String, testDate: String) {
        schoolDao.insertStudyTopic(StudyTopicEntity(text = text, testDate = testDate, done = false))
    }

    suspend fun toggleStudyTopic(item: StudyTopicEntity) {
        schoolDao.updateStudyTopic(item.copy(done = !item.done))
    }

    suspend fun deleteStudyTopic(id: Long) = schoolDao.deleteStudyTopic(id)

    suspend fun addPackItem(text: String) {
        schoolDao.insertPackItem(PackItemEntity(text = text, doneDate = ""))
    }

    suspend fun togglePackItem(item: PackItemEntity, tomorrowKey: String) {
        val nextDoneDate = if (item.doneDate == tomorrowKey) "" else tomorrowKey
        schoolDao.updatePackItem(item.copy(doneDate = nextDoneDate))
    }

    suspend fun deletePackItem(id: Long) = schoolDao.deletePackItem(id)

    suspend fun addExam(subject: String, date: String, topicTitles: List<String>, method: String) {
        val topics = topicTitles.map { ExamTopic(title = it, done = false) }
        schoolDao.insertExam(ExamEntity(subject = subject, date = date, topics = topics, method = method))
    }

    suspend fun toggleExamTopic(exam: ExamEntity, topicIndex: Int) {
        if (topicIndex !in exam.topics.indices) return
        val updatedTopics = exam.topics.mapIndexed { idx, topic ->
            if (idx == topicIndex) topic.copy(done = !topic.done) else topic
        }
        schoolDao.updateExam(exam.copy(topics = updatedTopics))
    }

    suspend fun deleteExam(id: Long) = schoolDao.deleteExam(id)

    // Money mutations
    suspend fun addTransaction(amount: Double, type: String, category: String, note: String, date: String) {
        moneyDao.insertTransaction(
            TransactionEntity(
                amount = kotlin.math.round(amount * 100.0) / 100.0,
                type = type,
                category = category,
                note = note,
                date = date
            )
        )
    }

    suspend fun deleteTransaction(id: Long) = moneyDao.deleteTransaction(id)

    suspend fun setSavingsGoal(name: String, target: Double) {
        moneyDao.upsertSavingsGoal(SavingsGoalEntity(id = 1, name = name, target = target, saved = 0.0))
    }

    suspend fun addToSavingsGoal(amount: Double) {
        val current = moneyDao.getSavingsGoalOnce() ?: return
        val updatedSaved = kotlin.math.round((current.saved + amount) * 100.0) / 100.0
        moneyDao.upsertSavingsGoal(current.copy(saved = updatedSaved))
    }

    suspend fun deleteSavingsGoal() = moneyDao.deleteSavingsGoal()

    suspend fun eraseAllMoneyData() {
        moneyDao.deleteAllTransactions()
        moneyDao.deleteSavingsGoal()
        preferencesManager.resetMoneyPreferences()
    }

    // Full JSON backup export (School + Money + Preferences)
    suspend fun buildBackupJson(): BackupExportResult = withContext(Dispatchers.IO) {
        val prefs = preferencesManager.preferencesFlow.first()
        val classList = schoolDao.getAllClassesOnce()
        val hwList = schoolDao.getAllHomeworkOnce()
        val studyList = schoolDao.getAllStudyTopicsOnce()
        val packList = schoolDao.getAllPackItemsOnce()
        val examList = schoolDao.getAllExamsOnce()
        val txList = moneyDao.getAllTransactionsOnce()
        val goal = moneyDao.getSavingsGoalOnce()

        val payload = HubBackupPayload(
            schemaVersion = 1,
            appName = "My Hub",
            exportedAt = Instant.now().toString(),
            school = SchoolBackupData(
                classes = classList.map {
                    ClassSessionBackup(id = it.id, dayOfWeek = it.dayOfWeek, time = it.time, name = it.name)
                },
                homework = hwList.map {
                    HomeworkBackup(id = it.id, text = it.text, dueDate = it.dueDate, done = it.done)
                },
                studyTopics = studyList.map {
                    StudyTopicBackup(id = it.id, text = it.text, testDate = it.testDate, done = it.done)
                },
                packItems = packList.map {
                    PackItemBackup(id = it.id, text = it.text, doneDate = it.doneDate)
                },
                exams = examList.map {
                    ExamBackup(id = it.id, subject = it.subject, date = it.date, topics = it.topics, method = it.method)
                }
            ),
            money = MoneyBackupData(
                transactions = txList.map {
                    TransactionBackup(
                        id = it.id,
                        amount = it.amount,
                        type = it.type,
                        category = it.category,
                        note = it.note,
                        date = it.date,
                        createdAt = it.createdAt
                    )
                },
                monthlyBudget = prefs.monthlyBudget,
                currencySymbol = prefs.currencySymbol,
                savingsGoal = goal?.let {
                    SavingsGoalBackup(name = it.name, target = it.target, saved = it.saved)
                }
            ),
            preferences = PreferencesBackupData(
                theme = prefs.theme.key,
                activeSection = prefs.activeSection.key,
                schoolTab = prefs.schoolTab,
                moneyTab = prefs.moneyTab
            )
        )

        val schoolCount = classList.size + hwList.size + studyList.size + packList.size + examList.size
        val moneyCount = txList.size + (if (goal != null) 1 else 0) + (if (prefs.monthlyBudget > 0) 1 else 0)

        BackupExportResult(
            jsonContent = exportJson.encodeToString(payload),
            schoolItemCount = schoolCount,
            moneyItemCount = moneyCount
        )
    }
}
