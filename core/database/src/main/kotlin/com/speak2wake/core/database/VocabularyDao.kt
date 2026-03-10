package com.speak2wake.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary") fun getAllWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE level = :level ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByLevel(level: String): VocabularyEntity?

    @Query(
            "SELECT * FROM vocabulary WHERE mastered = 0 AND level IN (:levels) ORDER BY timesShown ASC LIMIT 1"
    )
    suspend fun getLeastSeenUnmastered(levels: List<String>): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE level IN (:levels) AND language = :language ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByLevelAndLanguage(levels: List<String>, language: String): VocabularyEntity?

    @Query(
            "SELECT * FROM vocabulary WHERE mastered = 0 AND level IN (:levels) AND language = :language ORDER BY timesShown ASC LIMIT 1"
    )
    suspend fun getLeastSeenUnmasteredByLanguage(levels: List<String>, language: String): VocabularyEntity?

    @Upsert suspend fun upsertWords(words: List<VocabularyEntity>)

    @Query("UPDATE vocabulary SET timesShown = timesShown + 1 WHERE id = :id")
    suspend fun incrementShown(id: Long)

    @Query(
            "UPDATE vocabulary SET timesPassed = timesPassed + 1, mastered = CASE WHEN timesShown > 0 THEN (CAST(timesPassed + 1 AS REAL) / timesShown) >= 0.8 ELSE 0 END WHERE id = :id"
    )
    suspend fun incrementPassed(id: Long)

    @Query("SELECT COUNT(*) FROM vocabulary WHERE mastered = 1") fun countMastered(): Flow<Int>
}
