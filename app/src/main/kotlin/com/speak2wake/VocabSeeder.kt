package com.speak2wake

import android.content.Context
import com.speak2wake.core.database.VocabularyDao
import com.speak2wake.core.database.VocabularyEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class VocabJson(
    val german: String,
    val english: String,
    val vietnamese: String = "",
    val phonetic: String = "",
    val level: String = "A1",
    val category: String = "",
)

/**
 * Seeds vocabulary database from assets/vocabulary.json on first launch.
 * Supports incremental migration: adds new words from JSON that don't exist in DB.
 */
@Singleton
class VocabSeeder @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfEmpty() {
        val allJsonWords = loadWordsFromAssets()
        val existing = vocabularyDao.getAllWords().first()

        if (existing.isEmpty()) {
            // First launch: insert all words
            vocabularyDao.upsertWords(allJsonWords.map { it.toEntity() })
            return
        }

        // Incremental migration: find words in JSON that don't exist in DB (by german text)
        val existingGerman = existing.map { it.german.lowercase() }.toSet()
        val newWords = allJsonWords.filter { it.german.lowercase() !in existingGerman }
        if (newWords.isNotEmpty()) {
            vocabularyDao.upsertWords(newWords.map { it.toEntity() })
        }
    }

    private fun loadWordsFromAssets(): List<VocabJson> {
        val jsonString = context.assets.open("vocabulary.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<List<VocabJson>>(jsonString)
    }

    private fun VocabJson.toEntity() = VocabularyEntity(
        german = german,
        english = english,
        vietnamese = vietnamese,
        phonetic = phonetic,
        level = level,
        category = category,
    )
}
