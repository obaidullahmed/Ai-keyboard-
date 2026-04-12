package com.yourapp.aikeyboard.keyboard

object TypingAssistant {

    private val englishWords = listOf(
        "the", "and", "for", "you", "your", "with", "from", "that", "this", "have",
        "there", "not", "but", "what", "when", "which", "their", "about", "would", "could",
        "should", "hello", "thanks", "please", "thanks", "good", "better", "today", "message",
        "typing", "keyboard", "really", "please", "support", "learn", "smart", "premium", "grammar"
    )

    private val englishNextWordMap = mapOf(
        "i" to listOf("am", "will", "have"),
        "you" to listOf("are", "can", "have"),
        "we" to listOf("can", "are", "will"),
        "this" to listOf("is", "will", "can"),
        "thank" to listOf("you", "for", "you."),
        "good" to listOf("morning", "luck", "job"),
        "please" to listOf("select", "write", "check"),
        "can" to listOf("you", "we", "I"),
        "help" to listOf("me", "with", "please")
    )

    private val banglaWords = listOf(
        "আমি", "তুমি", "সে", "আমরা", "তারা", "এই", "ওই", "কেন", "কখন", "কিভাবে",
        "ভালো", "খারাপ", "ধন্যবাদ", "সুন্দর", "শুভ", "দিন", "রাত", "খাবার", "ভাষা", "বাংলা"
    )

    private val banglaNextWordMap = mapOf(
        "আমি" to listOf("আছি", "চাই", "গেলাম"),
        "তুমি" to listOf("করা", "হলে", "আছ"),
        "এই" to listOf("জন্য", "খুব", "দিয়ে"),
        "ধন্যবাদ" to listOf("ভাই", "আপনাকে", "অনেক"),
        "ভালো" to listOf("লাগে", "থাকে", "হয়")
    )

    fun generateWordSuggestions(fragment: String, language: String): List<String> {
        if (fragment.isBlank()) return emptyList()
        val source = if (language.equals("Bangla", ignoreCase = true)) banglaWords else englishWords
        val lowerFragment = fragment.lowercase().trim()

        val startsWith = source
            .filter { it.lowercase().startsWith(lowerFragment) && it.lowercase() != lowerFragment }
            .take(3)

        if (startsWith.isNotEmpty()) return startsWith

        val fuzzyMatch = source
            .map { it to levenshteinDistance(it.lowercase(), lowerFragment) }
            .filter { it.second <= 2 }
            .sortedBy { it.second }
            .map { it.first }
            .take(3)

        return fuzzyMatch
    }

    fun generateNextWordSuggestions(contextBefore: String, language: String): List<String> {
        val lastWord = contextBefore.trimEnd().split(" ").lastOrNull()?.lowercase().orEmpty()
        val nextMap = if (language.equals("Bangla", ignoreCase = true)) banglaNextWordMap else englishNextWordMap
        return nextMap[lastWord]?.take(3) ?: listOf("the", "and", "for").take(3)
    }

    fun suggestCorrection(word: String, language: String): String? {
        if (word.isBlank()) return null
        val candidates = if (language.equals("Bangla", ignoreCase = true)) banglaWords else englishWords
        val normalizedInput = word.lowercase().trim()
        if (candidates.contains(normalizedInput)) return null

        val bestMatch = candidates
            .map { it to levenshteinDistance(it.lowercase(), normalizedInput) }
            .filter { it.second <= 2 }
            .sortedBy { it.second }
            .map { it.first }
            .firstOrNull()

        return bestMatch
    }

    private fun levenshteinDistance(first: String, second: String): Int {
        val firstLength = first.length
        val secondLength = second.length
        val distance = Array(firstLength + 1) { IntArray(secondLength + 1) }

        for (i in 0..firstLength) distance[i][0] = i
        for (j in 0..secondLength) distance[0][j] = j

        for (i in 1..firstLength) {
            for (j in 1..secondLength) {
                val cost = if (first[i - 1] == second[j - 1]) 0 else 1
                distance[i][j] = minOf(
                    distance[i - 1][j] + 1,
                    distance[i][j - 1] + 1,
                    distance[i - 1][j - 1] + cost
                )
            }
        }
        return distance[firstLength][secondLength]
    }
}
