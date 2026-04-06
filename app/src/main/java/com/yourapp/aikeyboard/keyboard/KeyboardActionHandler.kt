package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.yourapp.aikeyboard.settings.SettingsRepository

class KeyboardActionHandler(
    private val service: InputMethodService,
    private val textCommitManager: TextCommitManager,
    private val settingsRepository: SettingsRepository
) {

    private val inputConnection: InputConnection?
        get() = service.currentInputConnection

    private var lastCharacter: Char? = null

    fun onCharacterKey(character: String) {
        val commitText = if (settingsRepository.isAutoCapitalizationEnabled() && shouldAutoCap()) {
            character.uppercase()
        } else {
            character.lowercase()
        }

        textCommitManager.commitText(commitText)
        lastCharacter = commitText.lastOrNull()
        provideFeedback()
    }

    fun onBackspace() {
        textCommitManager.deletePreviousCharacter()
        lastCharacter = null
        provideFeedback()
    }

    fun onSpace() {
        if (settingsRepository.isPunctuationAssistEnabled()) {
            val cursorText = inputConnection?.getTextBeforeCursor(2, 0)?.toString() ?: ""
            if (cursorText == "  ") {
                inputConnection?.deleteSurroundingText(1, 0)
                textCommitManager.commitText(". ")
                lastCharacter = '.'
                provideFeedback()
                return
            }
        }

        textCommitManager.commitText(" ")
        lastCharacter = ' '
        provideFeedback()
    }

    fun onEnter() {
        textCommitManager.commitText("\n")
        lastCharacter = '\n'
        provideFeedback()
    }

    fun onAiButtonClicked() {
        if (service is AiKeyboardService) {
            service.handleAiActionRequest()
        }
    }

    private fun shouldAutoCap(): Boolean {
        val previousChar = lastCharacter
        return previousChar == null ||
            previousChar == ' ' ||
            previousChar == '\n' ||
            previousChar == '.' ||
            previousChar == '!' ||
            previousChar == '?'
    }

    private fun provideFeedback() {
        val decorView = service.window?.window?.decorView

        if (settingsRepository.isVibrationEnabled()) {
            decorView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }

        if (settingsRepository.isSoundEnabled()) {
            decorView?.playSoundEffect(SoundEffectConstants.CLICK)
        }

        if (settingsRepository.isKeyPopupEnabled()) {
            val popupText = lastCharacter?.toString() ?: ""
            Toast.makeText(service, popupText, Toast.LENGTH_SHORT).show()
        }
    }
}
