package com.speak2wake.core.data.repository

import com.speak2wake.core.model.VocabularyLevel
import com.speak2wake.core.model.VocabularyWord
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    fun getAllWords(): Flow<List<VocabularyWord>>
    fun getMasteredCount(): Flow<Int>
    suspend fun getWordForChallenge(level: VocabularyLevel): VocabularyWord?
    suspend fun recordShown(wordId: Long)
    suspend fun recordPassed(wordId: Long)
}
