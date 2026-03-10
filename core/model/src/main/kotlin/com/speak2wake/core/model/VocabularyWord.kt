package com.speak2wake.core.model

data class VocabularyWord(
    val id: Long = 0,
    val german: String,
    val english: String,
    val vietnamese: String = "",
    val phonetic: String = "",
    val language: String = "de",
    val level: String = "A1",
    val category: String = "",
)
