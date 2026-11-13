package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExamTopic(
    val title: String,
    val done: Boolean = false
)

@Entity(tableName = "classes")
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 0 = Sunday .. 6 = Saturday (matching JS Date.getDay())
    val time: String,   // "HH:mm"
    val name: String
)

@Entity(tableName = "homework")
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

@Entity(tableName = "pack_items")
data class PackItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val doneDate: String = "" // "YYYY-MM-DD" (resets daily when != tomorrow's date)
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val date: String, // "YYYY-MM-DD"
    val topics: List<ExamTopic>,
    val method: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,     // "s" (spent) or "e" (earned)
    val category: String,
    val note: String,
    val date: String,     // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goal")
data class SavingsGoalEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val target: Double,
    val saved: Double = 0.0
)

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTopicList(topics: List<ExamTopic>): String = json.encodeToString(topics)

    @TypeConverter
    fun toTopicList(value: String): List<ExamTopic> =
        if (value.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<ExamTopic>>(value) }.getOrDefault(emptyList())
}
