package com.speak2wake.core.data.repository

import com.speak2wake.core.database.VocabularyDao
import com.speak2wake.core.database.toModel
import com.speak2wake.core.model.VocabularyLevel
import com.speak2wake.core.model.VocabularyWord
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineFirstVocabularyRepository
@Inject
constructor(
        private val vocabularyDao: VocabularyDao,
) : VocabularyRepository {

    override fun getAllWords(): Flow<List<VocabularyWord>> =
            vocabularyDao.getAllWords().map { list -> list.map { it.toModel() } }

    override fun getMasteredCount(): Flow<Int> = vocabularyDao.countMastered()

    override suspend fun getWordForChallenge(level: VocabularyLevel, language: String): VocabularyWord? {
        val levels =
                when (level) {
                    VocabularyLevel.A1 -> listOf("A1")
                    VocabularyLevel.A2 -> listOf("A2")
                    VocabularyLevel.B1 -> listOf("B1")
                    VocabularyLevel.B2 -> listOf("B2")
                    VocabularyLevel.MIXED -> listOf("A1", "A2", "B1", "B2")
                }
        return (vocabularyDao.getLeastSeenUnmasteredByLanguage(levels, language)
                        ?: vocabularyDao.getRandomWordByLevelAndLanguage(levels, language))?.toModel()
    }

    override suspend fun recordShown(wordId: Long) = vocabularyDao.incrementShown(wordId)
    override suspend fun recordPassed(wordId: Long) = vocabularyDao.incrementPassed(wordId)
}
