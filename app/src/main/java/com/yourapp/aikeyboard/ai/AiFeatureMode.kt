package com.yourapp.aikeyboard.ai

/**
 * Represents the AI feature being used
 */
enum class AiFeatureMode(
    val displayName: String,
    val icon: String,
    val description: String
) {
    REPLY("Reply", "💬", "Generate reply suggestions"),
    TONE("Tone", "🎨", "Change writing tone"),
    GRAMMAR("Grammar", "✓", "Fix grammar and spelling"),
    REWRITE("Rewrite", "✍️", "Generate variations"),
    CONTINUE("Continue", "→", "Complete the thought"),
    SUMMARIZE("Summarize", "📋", "Make it shorter"),
    TRANSLATE("Translate", "🌐", "Translate to another language");

    companion object {
        fun fromDisplayName(name: String): AiFeatureMode? =
            values().find { it.displayName.equals(name, ignoreCase = true) }
    }
}

/**
 * Tone variations for text transformation
 */
enum class ToneMode(val displayName: String, val description: String) {
    CASUAL("Casual", "Relaxed and conversational"),
    PROFESSIONAL("Professional", "Formal and businesslike"),
    FRIENDLY("Friendly", "Warm and approachable"),
    EMPATHETIC("Empathetic", "Compassionate and understanding"),
    POETIC("Poetic", "Creative and expressive")
}
