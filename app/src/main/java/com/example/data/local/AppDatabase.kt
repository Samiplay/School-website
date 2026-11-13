package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, time ASC")
    fun observeClasses(): Flow<List<ClassSessionEntity>>

    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, time ASC")
    suspend fun getAllClassesOnce(): List<ClassSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(item: ClassSessionEntity)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClass(id: Long)

    @Query("SELECT * FROM homework")
    fun observeHomework(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework")
    suspend fun getAllHomeworkOnce(): List<HomeworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(item: HomeworkEntity)

    @Update
    suspend fun updateHomework(item: HomeworkEntity)

    @Query("DELETE FROM homework WHERE id = :id")
    suspend fun deleteHomework(id: Long)

    @Query("SELECT * FROM study_topics")
    fun observeStudyTopics(): Flow<List<StudyTopicEntity>>

    @Query("SELECT * FROM study_topics")
    suspend fun getAllStudyTopicsOnce(): List<StudyTopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTopic(item: StudyTopicEntity)

    @Update
    suspend fun updateStudyTopic(item: StudyTopicEntity)

    @Query("DELETE FROM study_topics WHERE id = :id")
    suspend fun deleteStudyTopic(id: Long)

    @Query("SELECT * FROM pack_items")
    fun observePackItems(): Flow<List<PackItemEntity>>

    @Query("SELECT * FROM pack_items")
    suspend fun getAllPackItemsOnce(): List<PackItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackItem(item: PackItemEntity)

    @Update
    suspend fun updatePackItem(item: PackItemEntity)

    @Query("DELETE FROM pack_items WHERE id = :id")
    suspend fun deletePackItem(id: Long)

    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun observeExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY date ASC")
    suspend fun getAllExamsOnce(): List<ExamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(item: ExamEntity)

    @Update
    suspend fun updateExam(item: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: Long)
}

@Dao
interface MoneyDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC, id DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM savings_goal WHERE id = 1 LIMIT 1")
    fun observeSavingsGoal(): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goal WHERE id = 1 LIMIT 1")
    suspend fun getSavingsGoalOnce(): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavingsGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goal")
    suspend fun deleteSavingsGoal()
}

@Database(
    entities = [
        ClassSessionEntity::class,
        HomeworkEntity::class,
        StudyTopicEntity::class,
        PackItemEntity::class,
        ExamEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao
    abstract fun moneyDao(): MoneyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_hub_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
