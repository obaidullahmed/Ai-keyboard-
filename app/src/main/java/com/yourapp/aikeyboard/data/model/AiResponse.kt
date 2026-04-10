package com.yourapp.aikeyboard.data.model

/**
 * Data class to represent API response from AI service
 */
data class AiResponse(
    val success: Boolean,
    val suggestions: List<String> = emptyList(),
    val message: String = "",
    val result: String = ""
)
