package com.yourapp.aikeyboard.ai

import android.os.Handler
import android.os.Looper
import com.yourapp.aikeyboard.data.model.AiRequest
import com.yourapp.aikeyboard.data.model.AiResponse
import java.util.concurrent.Executors

/**
 * Manages all AI feature requests (reply, tone, grammar, rewrite, etc.)
 */
class AiReplyManager(
    private val apiService: AiApiService,
    private val promptBuilder: PromptBuilder,
    private val responseParser: AiResponseParser
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    sealed class AiReplyResult {
        object Loading : AiReplyResult()
        data class Success(val suggestions: List<String>) : AiReplyResult()
        data class Failure(val errorMessage: String) : AiReplyResult()
    }

    /**
     * Request replies (conversation suggestions)
     */
    fun requestReplies(
        beforeCursorText: String,
        selectedText: String,
        mode: ReplyMode,
        callback: (AiReplyResult) -> Unit
    ) {
        val contextAvailable = beforeCursorText.isNotBlank() || selectedText.isNotBlank()
        if (!contextAvailable) {
            callback(AiReplyResult.Failure("No text context available for AI reply."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = promptBuilder.buildPrompt(beforeCursorText, selectedText, mode)
                val request = AiRequest(
                    feature = "reply",
                    text = selectedText.ifBlank { beforeCursorText },
                    context = beforeCursorText,
                    mode = mode.displayName.lowercase()
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = mode.displayName.lowercase()))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                if (suggestions.isEmpty()) {
                    postToMain {
                        callback(AiReplyResult.Failure("AI returned no valid suggestions."))
                    }
                    return@submit
                }

                postToMain {
                    callback(AiReplyResult.Success(suggestions))
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "AI request failed." }))
                }
            }
        }
    }

    /**
     * Request tone transformation
     */
    fun requestToneChange(
        text: String,
        toneMode: ToneMode,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text selected for tone change."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Rewrite this text in a ${toneMode.displayName.lowercase()} tone. ")
                    append("Keep it concise. Return only the rewritten text without explanation.\n\n")
                    append("Original: \"$text\"")
                }
                val request = AiRequest(
                    feature = "tone",
                    text = text,
                    mode = toneMode.displayName.lowercase()
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "tone"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(3)))
                    } else {
                        callback(AiReplyResult.Failure("No tone variations generated."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Tone change failed." }))
                }
            }
        }
    }

    /**
     * Request grammar correction
     */
    fun requestGrammarFix(
        text: String,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text for grammar fix."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Fix the grammar, spelling, and punctuation in this text. ")
                    append("Keep the meaning intact. Return only the corrected text.\n\n")
                    append("Original: \"$text\"")
                }
                val request = AiRequest(
                    feature = "grammar",
                    text = text
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "grammar"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(1)))
                    } else {
                        callback(AiReplyResult.Failure("Grammar fix failed."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Grammar fix failed." }))
                }
            }
        }
    }

    /**
     * Request rewrite variations
     */
    fun requestRewrite(
        text: String,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text to rewrite."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Generate 3 different ways to express this text. ")
                    append("Keep each variation concise and natural. ")
                    append("Return only the variations, one per line.\n\n")
                    append("Original: \"$text\"")
                }
                val request = AiRequest(
                    feature = "rewrite",
                    text = text
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "rewrite"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(3)))
                    } else {
                        callback(AiReplyResult.Failure("Rewrite generation failed."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Rewrite failed." }))
                }
            }
        }
    }

    /**
     * Request continue writing
     */
    fun requestContinue(
        text: String,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text to continue from."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Continue this text naturally. Keep it concise and on-topic. ")
                    append("Return only the continuation without repeating the original.\n\n")
                    append("Text: \"$text\"")
                }
                val request = AiRequest(
                    feature = "continue",
                    text = text
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "continue"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(1)))
                    } else {
                        callback(AiReplyResult.Failure("Continue writing failed."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Continue writing failed." }))
                }
            }
        }
    }

    /**
     * Request summarization
     */
    fun requestSummarize(
        text: String,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text to summarize."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Summarize this text in 1-2 sentences. Keep it concise and clear. ")
                    append("Return only the summary.\n\n")
                    append("Text: \"$text\"")
                }
                val request = AiRequest(
                    feature = "summarize",
                    text = text
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "summarize"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(1)))
                    } else {
                        callback(AiReplyResult.Failure("Summarization failed."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Summarization failed." }))
                }
            }
        }
    }

    /**
     * Request translation
     */
    fun requestTranslate(
        text: String,
        targetLanguage: String,
        callback: (AiReplyResult) -> Unit
    ) {
        if (text.isBlank()) {
            callback(AiReplyResult.Failure("No text to translate."))
            return
        }

        postToMain { callback(AiReplyResult.Loading) }

        backgroundExecutor.submit {
            try {
                val prompt = buildString {
                    append("Translate this text to $targetLanguage. Keep the meaning exact. ")
                    append("Return only the translation.\n\n")
                    append("Original: \"$text\"")
                }
                val request = AiRequest(
                    feature = "translate",
                    text = text,
                    targetLanguage = targetLanguage
                )
                val rawResponse = apiService.requestReplies(AiReplyRequest(prompt = prompt, mode = "translate"))
                val suggestions = responseParser.parseSuggestions(rawResponse)

                postToMain {
                    if (suggestions.isNotEmpty()) {
                        callback(AiReplyResult.Success(suggestions.take(1)))
                    } else {
                        callback(AiReplyResult.Failure("Translation failed."))
                    }
                }
            } catch (throwable: Throwable) {
                postToMain {
                    callback(AiReplyResult.Failure(throwable.message.orEmpty().ifBlank { "Translation failed." }))
                }
            }
        }
    }

    fun shutdown() {
        backgroundExecutor.shutdownNow()
    }

    private fun postToMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}

/**
 * Legacy class kept for backward compatibility
 */
data class AiReplyRequest(
    val prompt: String,
    val mode: String
) {
    fun toJson(): String {
        return """
            {
                "prompt": "${prompt.replace("\"", "\\\"")}",
                "mode": "$mode"
            }
        """.trimIndent()
    }
}

