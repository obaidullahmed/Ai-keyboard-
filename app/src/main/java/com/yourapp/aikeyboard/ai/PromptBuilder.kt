package com.yourapp.aikeyboard.ai

class PromptBuilder {

    fun buildPrompt(beforeCursorText: String, selectedText: String, replyMode: ReplyMode): String {
        val safeContext = buildString {
            if (selectedText.isNotBlank()) {
                append("Selected text: \"")
                append(selectedText.trim())
                append("\"\n\n")
            }
            if (beforeCursorText.isNotBlank()) {
                append("Conversation context before cursor:\n")
                append(beforeCursorText.trim())
            }
        }.trim()

        val contextSection = if (safeContext.isBlank()) {
            "Use the available text context to generate a reply."
        } else {
            "Use the text context below to generate a reply:\n$safeContext"
        }

        return buildString {
            append("Generate exactly 3 reply suggestions in a ${replyMode.displayName.lowercase()} tone. ")
            append("The replies should be ${replyMode.toneDescription} and appropriate for a chat response. ")
            append("Do not include labels or numbering in the suggestions. ")
            append("Do not ask follow-up questions. ")
            append("$contextSection\n")
            append("Return only the reply text in a JSON array or plain text list if JSON is not supported.")
        }
    }
}
