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
    @Query("SELECT * FROM school_classes ORDER BY dayOfWeek ASC, time ASC")
    fun observeClasses(): Flow<List<SchoolClassEntity>>

    @Query("SELECT * FROM school_classes ORDER BY dayOfWeek ASC, time ASC")
    suspend fun getAllClassesOnce(): List<SchoolClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(item: SchoolClassEntity)

    @Query("DELETE FROM school_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    // Homework
    @Query("SELECT * FROM homework_items ORDER BY done ASC, CASE WHEN dueDate = '' THEN '9999-99-99' ELSE dueDate END ASC, id DESC")
    fun observeHomework(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework_items")
    suspend fun getAllHomeworkOnce(): List<HomeworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(item: HomeworkEntity)

    @Update
    suspend fun updateHomework(item: HomeworkEntity)

    @Query("DELETE FROM homework_items WHERE id = :id")
    suspend fun deleteHomeworkById(id: Long)

    // Study Topics
    @Query("SELECT * FROM study_topics ORDER BY done ASC, CASE WHEN testDate = '' THEN '9999-99-99' ELSE testDate END ASC, id DESC")
    fun observeStudyTopics(): Flow<List<StudyTopicEntity>>

    @Query("SELECT * FROM study_topics")
    suspend fun getAllStudyTopicsOnce(): List<StudyTopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTopic(item: StudyTopicEntity)

    @Update
    suspend fun updateStudyTopic(item: StudyTopicEntity)

    @Query("DELETE FROM study_topics WHERE id = :id")
    suspend fun deleteStudyTopicById(id: Long)

    // Bag Items
    @Query("SELECT * FROM bag_items ORDER BY id ASC")
    fun observeBagItems(): Flow<List<BagItemEntity>>

    @Query("SELECT * FROM bag_items")
    suspend fun getAllBagItemsOnce(): List<BagItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBagItem(item: BagItemEntity)

    @Update
    suspend fun updateBagItem(item: BagItemEntity)

    @Query("DELETE FROM bag_items WHERE id = :id")
    suspend fun deleteBagItemById(id: Long)

    // Exams
    @Query("SELECT * FROM exams ORDER BY date ASC, id ASC")
    fun observeExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY date ASC")
    suspend fun getAllExamsOnce(): List<ExamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(item: ExamEntity)

    @Update
    suspend fun updateExam(item: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
