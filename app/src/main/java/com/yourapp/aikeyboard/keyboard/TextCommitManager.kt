package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputConnection

/**
 * Manages text input, replacement, and cursor operations
 */
class TextCommitManager(
    private val service: InputMethodService
) {

    private val inputConnection: InputConnection?
        get() = service.currentInputConnection

    /**
     * Commit plain text (letter, number, symbol)
     */
    fun commitText(text: String) {
        inputConnection?.commitText(text, 1)
    }

    /**
     * Delete previous character
     */
    fun deletePreviousCharacter() {
        inputConnection?.deleteSurroundingText(1, 0)
    }

    /**
     * Replace selected text with AI output
     * If text is selected, replace it. Otherwise insert intelligently.
     */
    fun replaceOrInsertText(newText: String, selectedStart: Int, selectedEnd: Int) {
        val connection = inputConnection ?: return

        // If text is selected (selectedStart != selectedEnd), replace the selection
        if (selectedStart != selectedEnd) {
            val lengthToDelete = selectedEnd - selectedStart
            connection.setSelection(selectedStart, selectedEnd)
            connection.deleteSurroundingText(0, lengthToDelete)
            connection.commitText(newText, 1)
        } else {
            // No selection: insert text at cursor with smart spacing
            connection.commitText(newText, 1)
        }
    }

    /**
     * Replace specific surrounding text with new text
     * Used for tone changes, grammar fixes, etc.
     */
    fun replaceSurroundingText(
        beforeDeleteCount: Int,
        afterDeleteCount: Int,
        newText: String
    ) {
        inputConnection?.deleteSurroundingText(beforeDeleteCount, afterDeleteCount)
        inputConnection?.commitText(newText, 1)
    }

    /**
     * Insert text after current content (for continue writing)
     */
    fun insertTextAfterCursor(text: String) {
        inputConnection?.commitText(text, 1)
    }

    /**
     * Get current selection bounds
     */
    fun getSelectionBounds(): Pair<Int, Int>? {
        val connection = inputConnection ?: return null
        val before = connection.getTextBeforeCursor(1000, 0)?.length ?: 0
        val selected = connection.getSelectedText(0)?.length ?: 0

        return if (selected > 0) {
            Pair(before, before + selected)
        } else {
            null
        }
    }

    /**
     * Get text before cursor within limit
     */
    fun getTextBeforeCursor(maxChars: Int = 500): String {
        return inputConnection?.getTextBeforeCursor(maxChars, 0)?.toString() ?: ""
    }

    /**
     * Get selected text
     */
    fun getSelectedText(): String {
        return inputConnection?.getSelectedText(0)?.toString() ?: ""
    }

    /**
     * Commit text with newline after
     */
    fun commitTextWithNewline(text: String) {
        commitText(text)
        commitText("\n")
    }

    /**
     * Commit text with space after
     */
    fun commitTextWithSpace(text: String) {
        commitText(text)
        commitText(" ")
    }
}
