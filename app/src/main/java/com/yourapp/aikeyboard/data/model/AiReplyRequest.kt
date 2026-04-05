package com.yourapp.aikeyboard.data.model

import org.json.JSONObject

data class AiReplyRequest(
    val prompt: String,
    val model: String = "gpt-4.1",
    val maxTokens: Int = 120,
    val temperature: Double = 0.7,
    val topP: Double = 1.0,
    val n: Int = 3,
    val mode: String = "short"
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("model", model)
            put("prompt", prompt)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            put("top_p", topP)
            put("n", n)
            put("mode", mode)
        }
    }
}
