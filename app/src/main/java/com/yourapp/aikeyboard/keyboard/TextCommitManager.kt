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

        if (selectedStart != selectedEnd) {
            val lengthToDelete = selectedEnd - selectedStart
            connection.setSelection(selectedStart, selectedEnd)
            connection.deleteSurroundingText(0, lengthToDelete)
            connection.commitText(newText, 1)
        } else {
            connection.commitText(newText, 1)
        }
    }

    fun deletePreviousWord() {
        val connection = inputConnection ?: return
        val beforeText = connection.getTextBeforeCursor(200, 0)?.toString().orEmpty()
        val trimmed = beforeText.trimEnd()
        if (trimmed.isEmpty()) {
            connection.deleteSurroundingText(1, 0)
            return
        }

        val boundaryIndex = trimmed.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', '!', '?', ';', ':'))
        val count = if (boundaryIndex >= 0) trimmed.length - boundaryIndex - 1 else trimmed.length
        connection.deleteSurroundingText(count, 0)
    }

    fun replacePreviousWord(fragment: String, replacement: String) {
        val connection = inputConnection ?: return
        val beforeText = connection.getTextBeforeCursor(200, 0)?.toString().orEmpty()
        val trimmed = beforeText.trimEnd()
        if (trimmed.isEmpty() || fragment.isBlank()) {
            connection.commitText(replacement, 1)
            return
        }

        val lastIndex = trimmed.lastIndexOf(fragment)
        if (lastIndex >= 0 && lastIndex + fragment.length == trimmed.length) {
            val charsToDelete = fragment.length
            connection.deleteSurroundingText(charsToDelete, 0)
            connection.commitText(replacement, 1)
        } else {
            connection.commitText(replacement, 1)
        }
    }

    fun moveCursorBy(offset: Int) {
        val connection = inputConnection ?: return
        val beforeTextLength = connection.getTextBeforeCursor(1000, 0)?.length ?: 0
        val afterTextLength = connection.getTextAfterCursor(1000, 0)?.length ?: 0
        val newCursorPosition = (beforeTextLength + offset).coerceIn(0, beforeTextLength + afterTextLength)
        connection.setSelection(newCursorPosition, newCursorPosition)
    }

    fun getTextAfterCursor(maxChars: Int = 500): String {
        return inputConnection?.getTextAfterCursor(maxChars, 0)?.toString() ?: ""
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
