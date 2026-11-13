package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HubDao {
    // Classes
    @Query("SELECT * FROM school_classes ORDER BY time ASC")
    fun observeClasses(): Flow<List<SchoolClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(item: SchoolClassEntity)

    @Query("DELETE FROM school_classes WHERE id = :id")
    suspend fun deleteClass(id: Long)

    // Homework
    @Query("SELECT * FROM homework_tasks")
    fun observeHomework(): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(item: HomeworkEntity)

    @Update
    suspend fun updateHomework(item: HomeworkEntity)

    @Query("DELETE FROM homework_tasks WHERE id = :id")
    suspend fun deleteHomework(id: Long)

    // Study Topics
    @Query("SELECT * FROM study_topics")
    fun observeStudyTopics(): Flow<List<StudyTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTopic(item: StudyTopicEntity)

    @Update
    suspend fun updateStudyTopic(item: StudyTopicEntity)

    @Query("DELETE FROM study_topics WHERE id = :id")
    suspend fun deleteStudyTopic(id: Long)

    // Pack Items
    @Query("SELECT * FROM pack_items ORDER BY id ASC")
    fun observePackItems(): Flow<List<PackItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackItem(item: PackItemEntity)

    @Update
    suspend fun updatePackItem(item: PackItemEntity)

    @Query("DELETE FROM pack_items WHERE id = :id")
    suspend fun deletePackItem(id: Long)

    // Exams
    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun observeExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(item: ExamEntity)

    @Update
    suspend fun updateExam(item: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: Long)

    // Money Transactions
    @Query("SELECT * FROM money_transactions ORDER BY date DESC, createdAt DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity)

    @Query("DELETE FROM money_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM money_transactions")
    suspend fun deleteAllTransactions()
}
