package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputConnection

class KeyboardActionHandler(
    private val service: InputMethodService,
    private val textCommitManager: TextCommitManager
) {

    private val inputConnection: InputConnection?
        get() = service.currentInputConnection

    fun onCharacterKey(character: String) {
        inputConnection?.let { conn ->
            textCommitManager.commitText(character)
        }
    }

    fun onBackspace() {
        inputConnection?.let { conn ->
            textCommitManager.deletePreviousCharacter()
        }
    }

    fun onSpace() {
        textCommitManager.commitText(" ")
    }

    fun onEnter() {
        textCommitManager.commitText("\n")
    }

    fun onAiButtonClicked() {
        if (service is AiKeyboardService) {
            service.handleAiActionRequest()
        }
    }
}
