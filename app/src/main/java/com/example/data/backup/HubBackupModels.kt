package com.example.data.backup

import com.example.data.local.ExamTopic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HubBackupPayload(
    val schemaVersion: Int = 1,
    val app: String = "My Hub",
    val exportedAt: String,
    val preferences: BackupPreferences,
    val school: SchoolBackup,
    val money: MoneyBackup
)

@Serializable
data class BackupPreferences(
    val themeMode: String,
    val activeSection: String,
    val currencySymbol: String
)

@Serializable
data class SchoolBackup(
    val classes: List<ClassBackup>,
    val homework: List<HomeworkBackup>,
    val studyTopics: List<StudyTopicBackup>,
    val bagItems: List<BagItemBackup>,
    val exams: List<ExamBackup>
)

@Serializable
data class ClassBackup(
    val id: Long,
    val dayOfWeek: Int,
    val dayName: String,
    val time: String,
    val name: String
)

@Serializable
data class HomeworkBackup(
    val id: Long,
    val text: String,
    val dueDate: String,
    val done: Boolean
)

@Serializable
data class StudyTopicBackup(
    val id: Long,
    val text: String,
    val testDate: String,
    val done: Boolean
)

@Serializable
data class BagItemBackup(
    val id: Long,
    val text: String,
    val doneDate: String
)

@Serializable
data class ExamBackup(
    val id: Long,
    val subject: String,
    val date: String,
    val method: String,
    val topics: List<ExamTopic>
)

@Serializable
data class MoneyBackup(
    val currencySymbol: String,
    val monthlyBudget: Double,
    val savingsGoal: SavingsGoalBackup?,
    val transactions: List<TransactionBackup>
)

@Serializable
data class SavingsGoalBackup(
    val name: String,
    val target: Double,
    val saved: Double
)

@Serializable
data class TransactionBackup(
    val id: Long,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String,
    val date: String,
    val createdAt: Long
)

object BackupJsonCodec {
    val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(payload: HubBackupPayload): String = json.encodeToString(payload)

    fun decodeTopics(topicsJson: String): List<ExamTopic> {
        return try {
            json.decodeFromString<List<ExamTopic>>(topicsJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encodeTopics(topics: List<ExamTopic>): String = json.encodeToString(topics)
}
