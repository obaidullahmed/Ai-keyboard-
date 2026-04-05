package com.yourapp.aikeyboard.ai

import android.os.Handler
import android.os.Looper
import com.yourapp.aikeyboard.data.model.AiReplyRequest
import java.util.concurrent.Executors

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
                val request = AiReplyRequest(
                    prompt = prompt,
                    mode = mode.displayName.lowercase()
                )
                val rawResponse = apiService.requestReplies(request)
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
