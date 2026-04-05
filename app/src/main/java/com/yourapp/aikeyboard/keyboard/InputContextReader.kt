package com.yourapp.aikeyboard.keyboard

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class InputContextReader {

    data class InputContextData(
        val beforeCursorText: String,
        val selectedText: String,
        val isSecureField: Boolean
    )

    fun readCurrentContext(editorInfo: EditorInfo?, inputConnection: InputConnection?): InputContextData {
        if (editorInfo == null || inputConnection == null) {
            return InputContextData("", "", false)
        }

        val secureField = isSecureField(editorInfo)
        if (secureField) {
            // Secure or password fields must never expose text context to AI logic.
            return InputContextData("", "", true)
        }

        val beforeCursor = inputConnection.getTextBeforeCursor(128, 0)?.toString().orEmpty().trimEnd()
        val selectedText = inputConnection.getSelectedText(0)?.toString().orEmpty()
        return InputContextData(beforeCursor, selectedText, false)
    }

    fun isSecureField(editorInfo: EditorInfo): Boolean {
        val inputType = editorInfo.inputType
        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
        val isPassword = variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD || variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
        val isVisiblePassword = variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        // Additional privacy safety: do not enable AI in known secure or password variations.
        return isPassword || isVisiblePassword || editorInfo.privateImeOptions?.contains("secure") == true
    }
}
