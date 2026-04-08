package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.InputConnection
import com.yourapp.aikeyboard.ai.AiReplyManager
import com.yourapp.aikeyboard.ai.ToneMode
import com.yourapp.aikeyboard.settings.SettingsRepository

/**
 * Handles keyboard and AI action events
 */
class KeyboardActionHandler(
    private val service: InputMethodService,
    private val textCommitManager: TextCommitManager,
    private val settingsRepository: SettingsRepository
) {

    private val inputConnection: InputConnection?
        get() = service.currentInputConnection

    private var lastCharacter: Char? = null
    private var isShiftActive: Boolean = false
    private var currentAiManager: AiReplyManager? = null

    fun setAiManager(aiManager: AiReplyManager) {
        currentAiManager = aiManager
    }

    fun onCharacterKey(character: String) {
        val commitText = if (settingsRepository.isAutoCapitalizationEnabled() && shouldAutoCap()) {
            if (character.length == 1) character.uppercase() else character
        } else {
            character
        }

        textCommitManager.commitText(commitText)
        lastCharacter = commitText.lastOrNull()
        if (isShiftActive) {
            isShiftActive = false
            if (service is AiKeyboardService) {
                service.updateShiftState(false)
            }
        }
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

    fun onShiftToggled() {
        isShiftActive = !isShiftActive
        if (service is AiKeyboardService) {
            service.updateShiftState(isShiftActive)
        }
        provideFeedback()
    }

    fun onAiButtonClicked() {
        if (service is AiKeyboardService) {
            service.handleAiActionRequest()
        }
    }

    fun onSwitchModeClicked() {
        if (service is AiKeyboardService) {
            service.toggleKeyboardMode()
        }
    }

    fun onEmojiClicked() {
        if (service is AiKeyboardService) {
            service.toggleEmojiPanel()
        }
    }

    /**
     * Grammar correction tool clicked
     */
    fun onGrammarToolClicked() {
        if (service is AiKeyboardService) {
            service.handleGrammarRequest()
        }
    }

    /**
     * Tone changer tool clicked
     */
    fun onToneToolClicked() {
        if (service is AiKeyboardService) {
            service.handleToneRequest()
        }
    }

    /**
     * Rewrite tool clicked
     */
    fun onRewriteToolClicked() {
        if (service is AiKeyboardService) {
            service.handleRewriteRequest()
        }
    }

    /**
     * Continue writing tool clicked
     */
    fun onContinueToolClicked() {
        if (service is AiKeyboardService) {
            service.handleContinueRequest()
        }
    }

    /**
     * Translate tool clicked
     */
    fun onTranslateToolClicked() {
        if (service is AiKeyboardService) {
            service.handleTranslateRequest()
        }
    }

    /**
     * Mode chip selected (for tone selection, etc.)
     */
    fun onModeChipSelected(index: Int) {
        if (service is AiKeyboardService) {
            service.handleToneChipSelected(index)
        }
    }

    /**
     * Result card selected - insert/replace with the result
     */
    fun onResultCardSelected(text: String) {
        if (text.isBlank()) return

        val selectedBounds = textCommitManager.getSelectionBounds()
        if (selectedBounds != null) {
            textCommitManager.replaceOrInsertText(text, selectedBounds.first, selectedBounds.second)
        } else {
            textCommitManager.commitText(text)
        }

        provideFeedback()
    }

    private fun shouldAutoCap(): Boolean {
        val previousChar = lastCharacter
        return previousChar == null || previousChar == ' ' || previousChar == '\n' || previousChar == '.' || previousChar == '!' || previousChar == '?'
    }

    private fun provideFeedback() {
        val decorView = service.window?.window?.decorView

        if (settingsRepository.isVibrationEnabled()) {
            decorView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }

        if (settingsRepository.isSoundEnabled()) {
            decorView?.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }
}
