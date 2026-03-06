package com.speak2wake.core.common

import kotlin.math.max
import kotlin.math.min

/**
 * Composite pronunciation scoring engine.
 * Score = 40% Levenshtein + 30% Phonetic + 30% STT Confidence
 */
object ScoringEngine {

    data class ScoreResult(
        val levenshteinScore: Float,
        val phoneticScore: Float,
        val confidenceScore: Float,
        val totalScore: Float,
        val passed: Boolean,
        val threshold: Float,
    )

    fun score(
        expected: String,
        transcript: String,
        sttConfidence: Float = 0.9f,
    ): ScoreResult {
        val normalizedExpected = expected.lowercase().trim()
        val normalizedTranscript = transcript.lowercase().trim()

        val levenshtein = levenshteinSimilarity(normalizedExpected, normalizedTranscript)
        val phonetic = phoneticSimilarity(normalizedExpected, normalizedTranscript)
        val total = levenshtein * 0.4f + phonetic * 0.3f + sttConfidence * 0.3f

        val threshold = dynamicThreshold(normalizedExpected)

        return ScoreResult(
            levenshteinScore = levenshtein,
            phoneticScore = phonetic,
            confidenceScore = sttConfidence,
            totalScore = total,
            passed = total >= threshold,
            threshold = threshold,
        )
    }

    private fun dynamicThreshold(word: String): Float = 0.90f

    /** Normalized Levenshtein similarity (0..1) */
    private fun levenshteinSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1f
        if (s1.isEmpty() || s2.isEmpty()) return 0f
        val distance = levenshteinDistance(s1, s2)
        val maxLen = max(s1.length, s2.length)
        return 1f - distance.toFloat() / maxLen
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) dp[i - 1][j - 1]
                else 1 + min(dp[i - 1][j], min(dp[i][j - 1], dp[i - 1][j - 1]))
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Simplified Cologne phonetics for German.
     * Maps German phoneme groups to digits, then compares.
     */
    private fun phoneticSimilarity(s1: String, s2: String): Float {
        val code1 = cologneCode(s1)
        val code2 = cologneCode(s2)
        return levenshteinSimilarity(code1, code2)
    }

    private fun cologneCode(s: String): String {
        if (s.isEmpty()) return ""
        val sb = StringBuilder()
        val lower = s.lowercase()
            .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
        for (i in lower.indices) {
            val c = lower[i]
            val prev = if (i > 0) lower[i - 1] else '\u0000'
            val next = if (i < lower.length - 1) lower[i + 1] else '\u0000'
            val code = when (c) {
                'a', 'e', 'i', 'o', 'u' -> if (i == 0) "0" else ""
                'h' -> ""
                'b', 'p' -> "1"
                'd', 't' -> if (next in "sz") "8" else "2"
                'f', 'v', 'w' -> "3"
                'g', 'k', 'q' -> "4"
                'c' -> when {
                    i == 0 && next in "ahkloqrux" -> "4"
                    prev in "sz" -> "8"
                    next in "sz" -> "8"
                    else -> "4"
                }
                'x' -> if (prev in "ckq") "8" else "48"
                'l' -> "5"
                'm', 'n' -> "6"
                'r' -> "7"
                's', 'z' -> "8"
                else -> ""
            }
            sb.append(code)
        }
        // Remove consecutive duplicates
        return sb.toString().replace(Regex("(.)\\1+"), "$1")
    }
}
