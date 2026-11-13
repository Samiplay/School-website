package com.example.data.repository

import com.example.data.local.ExamTopic
import kotlinx.serialization.Serializable

@Serializable
data class HubBackupPayload(
    val schemaVersion: Int = 1,
    val appName: String = "My Hub",
    val exportedAt: String,
    val school: SchoolBackupData,
    val money: MoneyBackupData,
    val preferences: PreferencesBackupData
)

@Serializable
data class SchoolBackupData(
    val classes: List<ClassSessionBackup>,
    val homework: List<HomeworkBackup>,
    val studyTopics: List<StudyTopicBackup>,
    val packItems: List<PackItemBackup>,
    val exams: List<ExamBackup>
)

@Serializable
data class ClassSessionBackup(
    val id: Long,
    val dayOfWeek: Int,
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
data class PackItemBackup(
    val id: Long,
    val text: String,
    val doneDate: String
)

@Serializable
data class ExamBackup(
    val id: Long,
    val subject: String,
    val date: String,
    val topics: List<ExamTopic>,
    val method: String
)

@Serializable
data class MoneyBackupData(
    val transactions: List<TransactionBackup>,
    val monthlyBudget: Double,
    val currencySymbol: String,
    val savingsGoal: SavingsGoalBackup?
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

@Serializable
data class SavingsGoalBackup(
    val name: String,
    val target: Double,
    val saved: Double
)

@Serializable
data class PreferencesBackupData(
    val theme: String,
    val activeSection: String,
    val schoolTab: String,
    val moneyTab: String
)

data class BackupExportResult(
    val jsonContent: String,
    val schoolItemCount: Int,
    val moneyItemCount: Int
)
