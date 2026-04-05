package com.yourapp.aikeyboard.ai

import org.json.JSONArray
import org.json.JSONObject

class AiResponseParser {

    fun parseSuggestions(rawResponse: String): List<String> {
        val json = JSONObject(rawResponse)

        if (json.has("choices")) {
            return parseChoices(json.getJSONArray("choices"))
        }

        if (json.has("suggestions")) {
            return parseStringArray(json.getJSONArray("suggestions"))
        }

        if (json.has("replies")) {
            return parseStringArray(json.getJSONArray("replies"))
        }

        return parseFallbackText(json.optString("text", rawResponse))
    }

    private fun parseChoices(choicesArray: JSONArray): List<String> {
        val extracted = mutableListOf<String>()
        for (index in 0 until choicesArray.length()) {
            val choice = choicesArray.optJSONObject(index) ?: continue
            val text = when {
                choice.has("message") -> choice.optJSONObject("message")?.optString("content").orEmpty()
                else -> choice.optString("text")
            }
            extracted += splitAndClean(text)
            if (extracted.size >= 3) break
        }
        return extracted.take(3)
    }

    private fun parseStringArray(array: JSONArray): List<String> {
        val results = mutableListOf<String>()
        for (index in 0 until array.length()) {
            val item = array.optString(index).trim()
            if (item.isNotEmpty()) {
                results += item
            }
            if (results.size >= 3) break
        }
        return results.take(3)
    }

    private fun parseFallbackText(text: String): List<String> {
        val candidateList = splitAndClean(text)
        if (candidateList.size >= 3) {
            return candidateList.take(3)
        }
        throw AiResponseParseException("Unable to parse AI response into 3 suggestions.")
    }

    private fun splitAndClean(text: String): List<String> {
        return text
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.removePrefix("-").trim() }
            .filter { it.isNotEmpty() }
    }
}

class AiResponseParseException(message: String) : Exception(message)
