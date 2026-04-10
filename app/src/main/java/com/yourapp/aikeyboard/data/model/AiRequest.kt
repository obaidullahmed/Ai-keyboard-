package com.yourapp.aikeyboard.data.model

/**
 * Request to the AI API
 */
data class AiRequest(
    val feature: String,  // reply, tone, grammar, rewrite, continue, summarize, translate
    val text: String,
    val context: String = "",
    val mode: String = "",  // for tone: casual, professional, etc.
    val targetLanguage: String = ""  // for translation
) {
    fun toJson(): String {
        return """
            {
                "feature": "$feature",
                "text": "${ escapeJson(text)}",
                "context": "${escapeJson(context)}",
                "mode": "$mode",
                "targetLanguage": "$targetLanguage"
            }
        """.trimIndent()
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

/**
 * Represents a single result from AI processing
 */
data class AiResult(
    val original: String,
    val result: String,
    val feature: String
)
