package com.mcqautomation.app

import android.content.Context
import androidx.room.*

@Entity(tableName = "answers")
data class CachedAnswer(
    @PrimaryKey val questionHash: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE questionHash = :questionHash")
    suspend fun getAnswer(questionHash: String): CachedAnswer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: CachedAnswer)
}

@Database(entities = [CachedAnswer::class], version = 1, exportSchema = false)
abstract class AnswerDatabase : RoomDatabase() {
    abstract fun answerDao(): AnswerDao
}

class AnswerCache private constructor(private val dao: AnswerDao) {

    companion object {
        @Volatile
        private var INSTANCE: AnswerCache? = null

        fun getInstance(context: Context): AnswerCache {
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    AnswerDatabase::class.java,
                    "answer_cache"
                ).build()
                val instance = AnswerCache(database.answerDao())
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun getAnswer(question: String): String? {
        val hash = question.hashCode().toString()
        return dao.getAnswer(hash)?.answer
    }

    suspend fun saveAnswer(question: String, answer: String) {
        val hash = question.hashCode().toString()
        dao.insertAnswer(CachedAnswer(hash, answer))
    }
}