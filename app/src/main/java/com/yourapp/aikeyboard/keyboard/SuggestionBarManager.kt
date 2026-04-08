package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.TextView
import com.yourapp.aikeyboard.R

/**
 * Manages the main suggestion bar (bottom quick access area)
 */
class SuggestionBarManager(
    rootView: View,
    private val onSuggestionClicked: (String) -> Unit,
    private val onRetryClicked: () -> Unit
) {

    private val chip1: TextView = rootView.findViewById(R.id.suggestionChip1)
    private val chip2: TextView = rootView.findViewById(R.id.suggestionChip2)
    private val chip3: TextView = rootView.findViewById(R.id.suggestionChip3)

    private var currentSuggestions: List<String> = emptyList()
    private var isErrorState: Boolean = false

    init {
        chip1.setOnClickListener { if (!isErrorState) clickSuggestion(0) }
        chip2.setOnClickListener { if (!isErrorState) clickSuggestion(1) }
        chip3.setOnClickListener {
            if (isErrorState) {
                onRetryClicked()
            } else {
                clickSuggestion(2)
            }
        }
    }

    fun setSecureMode(isSecure: Boolean) {
        if (isSecure) {
            chip1.text = "🔒 Secure"
            chip2.text = "AI disabled"
            chip3.text = "in this field"
            currentSuggestions = emptyList()
            isErrorState = false
        }
    }

    fun showIdleState() {
        chip1.text = "Tap AI"
        chip2.text = "for suggestions"
        chip3.text = "or use tools"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    fun showLoadingState() {
        chip1.text = "Loading…"
        chip2.text = "generating"
        chip3.text = "results"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    fun showErrorState(message: String) {
        chip1.text = "Error"
        chip2.text = message.take(20)
        chip3.text = "Retry"
        currentSuggestions = emptyList()
        isErrorState = true
    }

    fun updateSuggestions(suggestions: List<String>) {
        currentSuggestions = suggestions
        chip1.text = suggestions.getOrNull(0)?.take(20) ?: "Result 1"
        chip2.text = suggestions.getOrNull(1)?.take(20) ?: "Result 2"
        chip3.text = suggestions.getOrNull(2)?.take(20) ?: "Result 3"
        isErrorState = false
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        if (enabled) {
            chip1.visibility = View.VISIBLE
            chip2.visibility = View.VISIBLE
            chip3.visibility = View.VISIBLE
        } else {
            chip1.visibility = View.GONE
            chip2.visibility = View.GONE
            chip3.visibility = View.GONE
        }
    }

    fun showSecureFieldState() {
        chip1.text = "🔒"
        chip2.text = "Secure field"
        chip3.text = "Type only"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    private fun clickSuggestion(index: Int) {
        currentSuggestions.getOrNull(index)?.takeIf(String::isNotBlank)?.let(onSuggestionClicked)
    }
}
