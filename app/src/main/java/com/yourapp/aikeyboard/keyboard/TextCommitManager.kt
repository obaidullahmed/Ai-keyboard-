package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService

class TextCommitManager(
    private val service: InputMethodService
) {

    fun commitText(text: String) {
        service.currentInputConnection?.commitText(text, 1)
    }

    fun deletePreviousCharacter() {
        service.currentInputConnection?.deleteSurroundingText(1, 0)
    }
}
