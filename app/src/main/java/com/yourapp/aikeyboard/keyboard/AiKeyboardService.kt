package com.yourapp.aikeyboard.keyboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import com.yourapp.aikeyboard.R
import com.yourapp.aikeyboard.ai.AiReplyManager
import com.yourapp.aikeyboard.ai.AiResponseParser
import com.yourapp.aikeyboard.ai.HttpAiApiService
import com.yourapp.aikeyboard.ai.PromptBuilder
import com.yourapp.aikeyboard.ai.ToneMode
import com.yourapp.aikeyboard.settings.SettingsRepository
import com.yourapp.aikeyboard.utils.Constants

class AiKeyboardService : InputMethodService() {

    private lateinit var keyboardViewManager: KeyboardViewManager
    private lateinit var keyboardActionHandler: KeyboardActionHandler
    private lateinit var suggestionBarManager: SuggestionBarManager
    private lateinit var textCommitManager: TextCommitManager
    private lateinit var inputContextReader: InputContextReader
    private lateinit var keyboardStateManager: KeyboardStateManager
    private lateinit var aiReplyManager: AiReplyManager
    private lateinit var settingsRepository: SettingsRepository

    private var lastContext: InputContextReader.InputContextData? = null
    private var currentToneMode: ToneMode = ToneMode.CASUAL
    private var currentAiFeature: String = "reply"

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        keyboardStateManager = KeyboardStateManager()
        textCommitManager = TextCommitManager(this)
        inputContextReader = InputContextReader()

        val apiService = HttpAiApiService(Constants.AI_ENDPOINT_URL, Constants.AI_API_KEY)
        aiReplyManager = AiReplyManager(apiService, PromptBuilder(), AiResponseParser())
    }

    override fun onCreateInputView(): View {
        val rootView = LayoutInflater.from(this).inflate(R.layout.keyboard_layout, null, false)

        suggestionBarManager = SuggestionBarManager(rootView, this::handleSuggestionSelection, this::retryLastRequest)
        keyboardActionHandler = KeyboardActionHandler(this, textCommitManager, settingsRepository)
        keyboardActionHandler.setAiManager(aiReplyManager)

        keyboardViewManager = KeyboardViewManager(rootView, keyboardActionHandler, suggestionBarManager, settingsRepository)
        keyboardViewManager.initializeKeyboard()
        keyboardViewManager.setKeyboardLanguage(settingsRepository.getCurrentLanguage())

        return rootView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        keyboardStateManager.markSecureInput(contextData.isSecureField)
        keyboardStateManager.updateGlideTyping(settingsRepository.isGlideTypingEnabled())
        keyboardStateManager.updateEnabledLanguages(settingsRepository.getEnabledLanguages())

        keyboardViewManager.applyPreferences()
        keyboardViewManager.setKeyboardLanguage(settingsRepository.getCurrentLanguage())
        keyboardViewManager.refreshClipboardHistory()

        applySecureInputState(contextData.isSecureField)
        keyboardViewManager.updateContextPreview(contextData.beforeCursorText.take(50))
        keyboardViewManager.showAiToolsPanel(false)
        keyboardViewManager.showEmojiPanel(false)
        keyboardViewManager.showClipboardPanel(false)
        suggestionBarManager.showIdleState()
    }

    override fun onUpdateSelection(
        selStart: Int,
        selEnd: Int,
        oldSelStart: Int,
        oldSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(selStart, selEnd, oldSelStart, oldSelEnd, candidatesStart, candidatesEnd)

        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        keyboardStateManager.markSecureInput(contextData.isSecureField)
        applySecureInputState(contextData.isSecureField)
        keyboardViewManager.updateContextPreview(contextData.beforeCursorText.take(50))
    }

    override fun onDestroy() {
        aiReplyManager.shutdown()
        super.onDestroy()
    }

    internal fun toggleKeyboardMode() {
        keyboardViewManager.toggleKeyboardMode()
    }

    internal fun toggleEmojiPanel() {
        keyboardViewManager.toggleEmojiPanel()
    }

    internal fun switchLanguage() {
        val enabled = settingsRepository.getEnabledLanguages().toList().ifEmpty { listOf("English") }
        val current = settingsRepository.getCurrentLanguage().takeIf { it in enabled } ?: enabled.first()
        val nextIndex = (enabled.indexOf(current).takeIf { it >= 0 }?.plus(1) ?: 0) % enabled.size
        val nextLanguage = enabled[nextIndex]
        settingsRepository.setCurrentLanguage(nextLanguage)
        keyboardViewManager.setKeyboardLanguage(nextLanguage)
        suggestionBarManager.showIdleState()
    }

    internal fun updateShiftState(active: Boolean) {
        keyboardViewManager.updateShiftState(active)
    }

    internal fun handleToneChipSelected(index: Int) {
        currentToneMode = ToneMode.values().getOrNull(index) ?: currentToneMode
        currentAiFeature = "tone"
        handleToneRequest()
    }

    internal fun handleAiActionRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showSecureFieldState()
            return
        }

        currentAiFeature = "reply"
        keyboardViewManager.showAiToolsPanel(true)
        suggestionBarManager.showIdleState()
    }

    internal fun handleLanguageSwitchRequest() {
        switchLanguage()
    }

    internal fun handleGrammarToolClicked() {
        handleGrammarRequest()
    }

    internal fun handleToneToolClicked() {
        handleToneRequest()
    }

    internal fun handleRewriteToolClicked() {
        handleRewriteRequest()
    }

    internal fun handleContinueToolClicked() {
        handleContinueRequest()
    }

    internal fun handleTranslateToolClicked() {
        handleTranslateRequest()
    }

    internal fun handleGrammarRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState(getString(R.string.privacy_summary))
            return
        }

        currentAiFeature = "grammar"
        val textToProcess = if (contextData.selectedText.isNotBlank()) contextData.selectedText else contextData.beforeCursorText
        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to fix grammar")
            return
        }

        keyboardViewManager.showModeChips(emptyList())
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestGrammarFix(textToProcess) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> keyboardViewManager.showResultsPanel(loading = true)
                is AiReplyManager.AiReplyResult.Success -> keyboardViewManager.displayResults(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    internal fun handleToneRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState(getString(R.string.privacy_summary))
            return
        }

        currentAiFeature = "tone"
        val textToProcess = if (contextData.selectedText.isNotBlank()) contextData.selectedText else contextData.beforeCursorText
        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to change tone")
            return
        }

        keyboardViewManager.showModeChips(listOf(ToneMode.CASUAL.displayName, ToneMode.PROFESSIONAL.displayName, ToneMode.FRIENDLY.displayName))
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestToneChange(textToProcess, currentToneMode) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> keyboardViewManager.showResultsPanel(loading = true)
                is AiReplyManager.AiReplyResult.Success -> keyboardViewManager.displayResults(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    internal fun handleRewriteRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState(getString(R.string.privacy_summary))
            return
        }

        currentAiFeature = "rewrite"
        val textToProcess = if (contextData.selectedText.isNotBlank()) contextData.selectedText else contextData.beforeCursorText
        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to rewrite")
            return
        }

        keyboardViewManager.hideModeChips()
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestRewrite(textToProcess) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> keyboardViewManager.showResultsPanel(loading = true)
                is AiReplyManager.AiReplyResult.Success -> keyboardViewManager.displayResults(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    internal fun handleContinueRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState(getString(R.string.privacy_summary))
            return
        }

        currentAiFeature = "continue"
        if (contextData.beforeCursorText.isBlank()) {
            suggestionBarManager.showErrorState("Start typing to continue")
            return
        }

        keyboardViewManager.hideModeChips()
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestContinue(contextData.beforeCursorText) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> keyboardViewManager.showResultsPanel(loading = true)
                is AiReplyManager.AiReplyResult.Success -> keyboardViewManager.displayResults(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    internal fun handleTranslateRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState(getString(R.string.privacy_summary))
            return
        }

        currentAiFeature = "translate"
        val textToProcess = if (contextData.selectedText.isNotBlank()) contextData.selectedText else contextData.beforeCursorText
        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to translate")
            return
        }

        keyboardViewManager.hideModeChips()
        keyboardViewManager.showResultsPanel(loading = true)

        val targetLanguage = if (settingsRepository.getCurrentLanguage().equals("Bangla", ignoreCase = true)) "English" else "Bangla"
        aiReplyManager.requestTranslate(textToProcess, targetLanguage) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> keyboardViewManager.showResultsPanel(loading = true)
                is AiReplyManager.AiReplyResult.Success -> keyboardViewManager.displayResults(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    internal fun handleVoiceTypingRequest() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_test_button))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
            suggestionBarManager.showIdleState()
        } catch (exception: ActivityNotFoundException) {
            suggestionBarManager.showErrorState(getString(R.string.voice_not_available))
        }
    }

    internal fun handleTypingUpdate() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData

        if (contextData.isSecureField) {
            suggestionBarManager.showSecureFieldState()
            return
        }

        val typedText = contextData.beforeCursorText
        val query = typedText.trimEnd().split(" ").lastOrNull().orEmpty()
        val suggestions = if (query.isNotBlank()) {
            TypingAssistant.generateWordSuggestions(query, settingsRepository.getCurrentLanguage())
        } else {
            TypingAssistant.generateNextWordSuggestions(typedText, settingsRepository.getCurrentLanguage())
        }

        if (suggestions.isEmpty()) {
            suggestionBarManager.showIdleState()
        } else {
            suggestionBarManager.updateSuggestions(suggestions)
        }
    }

    private fun retryLastRequest() {
        val contextData = lastContext
        if (contextData == null || contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot retry in this field")
            return
        }
        when (currentAiFeature) {
            "reply" -> handleAiActionRequest()
            "grammar" -> handleGrammarRequest()
            "tone" -> handleToneRequest()
            "rewrite" -> handleRewriteRequest()
            "continue" -> handleContinueRequest()
            "translate" -> handleTranslateRequest()
        }
    }

    private fun handleSuggestionSelection(suggestionText: String) {
        if (suggestionText.isBlank()) return
        textCommitManager.commitText(suggestionText)
        suggestionBarManager.showIdleState()
        keyboardViewManager.hideResultsPanel()
        keyboardViewManager.showAiToolsPanel(false)
    }

    private fun applySecureInputState(isSecure: Boolean) {
        suggestionBarManager.setSecureMode(isSecure)
        keyboardViewManager.setAiButtonEnabled(!isSecure)
    }
}
