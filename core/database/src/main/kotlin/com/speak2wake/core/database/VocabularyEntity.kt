package com.speak2wake.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.speak2wake.core.model.VocabularyWord

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val german: String,
    val english: String,
    val vietnamese: String = "",
    val phonetic: String = "",
    val level: String = "A1",
    val category: String = "",
    val timesShown: Int = 0,
    val timesPassed: Int = 0,
    val mastered: Boolean = false,
)

fun VocabularyEntity.toModel() = VocabularyWord(
    id = id, german = german, english = english,
    vietnamese = vietnamese, phonetic = phonetic, level = level, category = category,
)
