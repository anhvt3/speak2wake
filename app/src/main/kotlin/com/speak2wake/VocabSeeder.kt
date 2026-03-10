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

@Serializable
private data class VocabViJson(
    val vietnamese: String,
    val english: String,
    val phonetic: String = "",
    val level: String = "A1",
    val category: String = "",
)

/**
 * Seeds vocabulary database from assets on first launch.
 * Loads German words from vocabulary.json and Vietnamese words from vocabulary_vi.json.
 * Supports incremental migration: adds new words from JSON that don't exist in DB.
 */
@Singleton
class VocabSeeder @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfEmpty() {
        val deWords = loadDeWordsFromAssets()
        val viWords = loadViWordsFromAssets()
        val existing = vocabularyDao.getAllWords().first()

        if (existing.isEmpty()) {
            // First launch: insert all words
            val allEntities = deWords.map { it.toEntity() } + viWords.map { it.toEntity() }
            vocabularyDao.upsertWords(allEntities)
            return
        }

        // Incremental migration: find words not in DB (by challengeWord + language pair)
        val existingKeys = existing.map { "${it.language}:${it.german.lowercase()}" }.toSet()

        val newDeWords = deWords.filter { "de:${it.german.lowercase()}" !in existingKeys }
        val newViWords = viWords.filter { "vi:${it.vietnamese.lowercase()}" !in existingKeys }

        val newEntities = newDeWords.map { it.toEntity() } + newViWords.map { it.toEntity() }
        if (newEntities.isNotEmpty()) {
            vocabularyDao.upsertWords(newEntities)
        }
    }

    private fun loadDeWordsFromAssets(): List<VocabJson> {
        val jsonString = context.assets.open("vocabulary.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<List<VocabJson>>(jsonString)
    }

    private fun loadViWordsFromAssets(): List<VocabViJson> {
        return try {
            val jsonString = context.assets.open("vocabulary_vi.json")
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString<List<VocabViJson>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun VocabJson.toEntity() = VocabularyEntity(
        german = german,
        english = english,
        vietnamese = vietnamese,
        phonetic = phonetic,
        language = "de",
        level = level,
        category = category,
    )

    private fun VocabViJson.toEntity() = VocabularyEntity(
        german = vietnamese, // challenge word field
        english = english,
        vietnamese = vietnamese,
        phonetic = phonetic,
        language = "vi",
        level = level,
        category = category,
    )
}
