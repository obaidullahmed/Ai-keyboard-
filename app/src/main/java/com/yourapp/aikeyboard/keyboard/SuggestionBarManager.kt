package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.yourapp.aikeyboard.R

class SuggestionBarManager(
    rootView: View,
    private val onSuggestionClicked: (String) -> Unit,
    private val onRetryClicked: () -> Unit
) {

    private val chip1: TextView = rootView.findViewById(R.id.suggestionChip1)
    private val chip2: TextView = rootView.findViewById(R.id.suggestionChip2)
    private val chip3: TextView = rootView.findViewById(R.id.suggestionChip3)
    private val loadingIndicator: ProgressBar = rootView.findViewById(R.id.loadingIndicator)

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
            loadingIndicator.visibility = View.GONE
            chip1.text = "Secure field"
            chip2.text = "AI disabled"
            chip3.text = "Use keyboard only"
            currentSuggestions = emptyList()
            isErrorState = false
        }
    }

    fun showIdleState() {
        loadingIndicator.visibility = View.GONE
        chip1.text = "Tap AI for reply"
        chip2.text = "Need context"
        chip3.text = "Manual trigger only"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    fun showLoadingState() {
        loadingIndicator.visibility = View.VISIBLE
        chip1.text = "Generating…"
        chip2.text = "Please wait"
        chip3.text = "Fetching replies"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    fun showErrorState(message: String) {
        loadingIndicator.visibility = View.GONE
        chip1.text = "AI error"
        chip2.text = message
        chip3.text = "Retry"
        currentSuggestions = emptyList()
        isErrorState = true
    }

    fun updateSuggestions(suggestions: List<String>) {
        loadingIndicator.visibility = View.GONE
        currentSuggestions = suggestions
        chip1.text = suggestions.getOrNull(0).orEmpty()
        chip2.text = suggestions.getOrNull(1).orEmpty()
        chip3.text = suggestions.getOrNull(2).orEmpty()
        isErrorState = false
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        if (enabled) {
            chip1.visibility = View.VISIBLE
            chip2.visibility = View.VISIBLE
            chip3.visibility = View.VISIBLE
        } else {
            loadingIndicator.visibility = View.GONE
            chip1.visibility = View.GONE
            chip2.visibility = View.GONE
            chip3.visibility = View.GONE
        }
    }

    fun showSecureFieldState() {
        loadingIndicator.visibility = View.GONE
        chip1.text = "Secure input detected"
        chip2.text = "AI paused"
        chip3.text = "Use keyboard only"
        currentSuggestions = emptyList()
        isErrorState = false
    }

    private fun clickSuggestion(index: Int) {
        currentSuggestions.getOrNull(index)?.takeIf(String::isNotBlank)?.let(onSuggestionClicked)
    }
}
