package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "school_classes")
data class SchoolClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Int, // 0=Sunday, 1=Monday .. 6=Saturday
    val time: String, // "HH:mm"
    val name: String
)

@Entity(tableName = "homework_tasks")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val due: String, // "yyyy-MM-dd" or ""
    val done: Boolean = false
)

@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val date: String, // "yyyy-MM-dd" or ""
    val done: Boolean = false
)

@Entity(tableName = "pack_items")
data class PackItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val doneDate: String = "" // "yyyy-MM-dd" when packed for that day
)

@Serializable
data class ExamTopic(
    val t: String,
    val done: Boolean = false
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val date: String, // "yyyy-MM-dd"
    val method: String,
    val topicsJson: String
)

@Entity(tableName = "money_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amt: Double,
    val type: String, // "s" (Spent) or "e" (Earned)
    val cat: String,
    val note: String,
    val date: String, // "yyyy-MM-dd"
    val createdAt: Long = System.currentTimeMillis()
)
