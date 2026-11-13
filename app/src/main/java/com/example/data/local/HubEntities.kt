package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class ExamTopic(
    val title: String,
    val done: Boolean = false
)

@Entity(tableName = "school_classes")
data class SchoolClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 0 = Sunday .. 6 = Saturday
    val time: String,   // "HH:mm"
    val name: String
)

@Entity(tableName = "homework_items")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val dueDate: String, // "YYYY-MM-DD" or ""
    val done: Boolean = false
)

@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val testDate: String, // "YYYY-MM-DD" or ""
    val done: Boolean = false
)

@Entity(tableName = "bag_items")
data class BagItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val doneDate: String = "" // "YYYY-MM-DD" for daily reset
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val date: String, // "YYYY-MM-DD"
    val method: String,
    val topicsJson: String = "[]"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String, // "s" = Spent, "e" = Earned
    val category: String,
    val note: String,
    val date: String, // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)
