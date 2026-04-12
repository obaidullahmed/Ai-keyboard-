package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.inputmethod.InputConnection
import com.yourapp.aikeyboard.ai.AiReplyManager
import com.yourapp.aikeyboard.ai.ToneMode
import com.yourapp.aikeyboard.keyboard.TypingAssistant
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
    private var currentPendingFragment: String = ""

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
        updatePendingFragment(commitText)
        lastCharacter = commitText.lastOrNull()

        if (isShiftActive) {
            isShiftActive = false
            if (service is AiKeyboardService) {
                service.updateShiftState(false)
            }
        }

        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        provideFeedback()
    }

    fun onBackspace() {
        textCommitManager.deletePreviousCharacter()
        currentPendingFragment = ""
        lastCharacter = null
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        provideFeedback()
    }

    fun onBackspaceWord() {
        textCommitManager.deletePreviousWord()
        currentPendingFragment = ""
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        provideFeedback()
    }

    fun onSpace() {
        if (settingsRepository.isPunctuationAssistEnabled()) {
            val cursorText = inputConnection?.getTextBeforeCursor(2, 0)?.toString() ?: ""
            if (cursorText == "  ") {
                inputConnection?.deleteSurroundingText(1, 0)
                textCommitManager.commitText(". ")
                lastCharacter = '.'
                currentPendingFragment = ""
                if (service is AiKeyboardService) {
                    service.handleTypingUpdate()
                }
                provideFeedback()
                return
            }
        }

        val beforeText = inputConnection?.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        val lastWord = beforeText.trimEnd().split(" ").lastOrNull().orEmpty()
        val correctedWord = if (settingsRepository.isAutoCorrectEnabled() && lastWord.isNotBlank()) {
            TypingAssistant.suggestCorrection(lastWord, settingsRepository.getCurrentLanguage())
        } else {
            null
        }

        if (correctedWord != null && correctedWord != lastWord) {
            textCommitManager.replacePreviousWord(lastWord, "$correctedWord ")
        } else {
            textCommitManager.commitText(" ")
        }

        currentPendingFragment = ""
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        lastCharacter = ' '
        provideFeedback()
    }

    fun onEnter() {
        textCommitManager.commitText("\n")
        currentPendingFragment = ""
        lastCharacter = '\n'
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
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

    fun onVoiceTypingClicked() {
        if (service is AiKeyboardService) {
            service.handleVoiceTypingRequest()
        }
    }

    fun onLanguageSwitchPressed() {
        if (service is AiKeyboardService) {
            service.handleLanguageSwitchRequest()
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

    fun onPasteClipboardText(text: String) {
        if (text.isBlank()) return
        val selectedBounds = textCommitManager.getSelectionBounds()
        if (selectedBounds != null) {
            textCommitManager.replaceOrInsertText(text, selectedBounds.first, selectedBounds.second)
        } else {
            textCommitManager.commitText(text)
        }
        currentPendingFragment = ""
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        provideFeedback()
    }

    fun onSpaceGestureMove(offset: Int) {
        textCommitManager.moveCursorBy(offset)
        provideFeedback()
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
        } else if (currentPendingFragment.isNotBlank() && text.startsWith(currentPendingFragment, ignoreCase = true)) {
            textCommitManager.replacePreviousWord(currentPendingFragment, text)
        } else {
            textCommitManager.commitText(text)
        }

        currentPendingFragment = ""
        if (service is AiKeyboardService) {
            service.handleTypingUpdate()
        }
        provideFeedback()
    }

    private fun updatePendingFragment(commitText: String) {
        if (commitText.isBlank() || commitText.last().isWhitespace() || commitText.last() in listOf('.', ',', '!', '?', ':', ';')) {
            currentPendingFragment = ""
            return
        }

        currentPendingFragment += commitText
        if (currentPendingFragment.length > 32) {
            currentPendingFragment = currentPendingFragment.takeLast(32)
        }
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
