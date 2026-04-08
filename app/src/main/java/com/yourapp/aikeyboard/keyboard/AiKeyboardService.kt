package com.yourapp.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import com.yourapp.aikeyboard.R
import com.yourapp.aikeyboard.ai.AiReplyManager
import com.yourapp.aikeyboard.ai.AiResponseParser
import com.yourapp.aikeyboard.ai.HttpAiApiService
import com.yourapp.aikeyboard.ai.PromptBuilder
import com.yourapp.aikeyboard.ai.ReplyMode
import com.yourapp.aikeyboard.ai.ToneMode
import com.yourapp.aikeyboard.settings.ClipboardHistoryManager
import com.yourapp.aikeyboard.settings.SettingsRepository
import com.yourapp.aikeyboard.utils.Constants

/**
 * Main IME service for Lexora AI Keyboard
 * Orchestrates keyboard view, input handling, and AI feature integration
 */
class AiKeyboardService : InputMethodService() {

    private lateinit var keyboardViewManager: KeyboardViewManager
    private lateinit var keyboardActionHandler: KeyboardActionHandler
    private lateinit var suggestionBarManager: SuggestionBarManager
    private lateinit var textCommitManager: TextCommitManager
    private lateinit var inputContextReader: InputContextReader
    private lateinit var keyboardStateManager: KeyboardStateManager
    private lateinit var aiReplyManager: AiReplyManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var clipboardHistoryManager: ClipboardHistoryManager

    private var lastContext: InputContextReader.InputContextData? = null
    private var currentReplyMode: ReplyMode = ReplyMode.SHORT
    private var currentToneMode: ToneMode = ToneMode.CASUAL
    private var currentAiFeature: String = "reply"

    override fun onCreate() {
        super.onCreate()

        settingsRepository = SettingsRepository(this)
        keyboardStateManager = KeyboardStateManager()
        textCommitManager = TextCommitManager(this)
        inputContextReader = InputContextReader()

        clipboardHistoryManager = ClipboardHistoryManager(this, settingsRepository)
        clipboardHistoryManager.startListening()

        val apiService = HttpAiApiService(Constants.AI_ENDPOINT_URL, Constants.AI_API_KEY)
        aiReplyManager = AiReplyManager(apiService, PromptBuilder(), AiResponseParser())
    }

    override fun onCreateInputView(): View {
        val rootView = LayoutInflater.from(this).inflate(R.layout.keyboard_layout, null, false)

        suggestionBarManager = SuggestionBarManager(
            rootView,
            this::handleSuggestionSelection,
            this::retryLastRequest
        )

        keyboardActionHandler = KeyboardActionHandler(
            this,
            textCommitManager,
            settingsRepository
        )
        keyboardActionHandler.setAiManager(aiReplyManager)

        keyboardViewManager = KeyboardViewManager(
            rootView,
            keyboardActionHandler,
            suggestionBarManager,
            settingsRepository
        )

        keyboardViewManager.initializeKeyboard()
        return rootView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        val currentConnection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, currentConnection)

        keyboardStateManager.markSecureInput(contextData.isSecureField)
        keyboardStateManager.updateGlideTyping(settingsRepository.isGlideTypingEnabled())
        keyboardStateManager.updateEnabledLanguages(settingsRepository.getEnabledLanguages())

        applySecureInputState(contextData.isSecureField)
        keyboardViewManager.updateContextPreview(contextData.beforeCursorText.take(50))
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
        super.onUpdateSelection(
            selStart,
            selEnd,
            oldSelStart,
            oldSelEnd,
            candidatesStart,
            candidatesEnd
        )

        val info = currentInputEditorInfo
        val currentConnection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, currentConnection)

        keyboardStateManager.markSecureInput(contextData.isSecureField)
        applySecureInputState(contextData.isSecureField)
        keyboardViewManager.updateContextPreview(contextData.beforeCursorText.take(50))
    }

    override fun onDestroy() {
        clipboardHistoryManager.stopListening()
        aiReplyManager.shutdown()
        super.onDestroy()
    }

    private fun applySecureInputState(isSecure: Boolean) {
        suggestionBarManager.setSecureMode(isSecure)
        keyboardViewManager.setAiButtonEnabled(!isSecure)
    }

    /**
     * Handle AI reply request (conversation suggestions)
     */
    internal fun handleAiActionRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "reply"

        if (contextData.isSecureField) {
            suggestionBarManager.showSecureFieldState()
            return
        }

        if (contextData.beforeCursorText.isBlank() && contextData.selectedText.isBlank()) {
            suggestionBarManager.showErrorState("No context available")
            return
        }

        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestReplies(
            contextData.beforeCursorText,
            contextData.selectedText,
            currentReplyMode
        ) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    /**
     * Handle grammar correction request
     */
    internal fun handleGrammarRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "grammar"

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot process secure fields")
            return
        }

        val textToProcess = if (contextData.selectedText.isNotBlank()) {
            contextData.selectedText
        } else {
            contextData.beforeCursorText
        }

        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to fix grammar")
            return
        }

        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestGrammarFix(textToProcess) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    /**
     * Handle tone transformation request
     */
    internal fun handleToneRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "tone"

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot process secure fields")
            return
        }

        val textToProcess = if (contextData.selectedText.isNotBlank()) {
            contextData.selectedText
        } else {
            contextData.beforeCursorText
        }

        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to change tone")
            return
        }

        keyboardViewManager.showModeChips(
            listOf(ToneMode.CASUAL.displayName, ToneMode.PROFESSIONAL.displayName, ToneMode.FRIENDLY.displayName)
        )
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestToneChange(textToProcess, currentToneMode) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    /**
     * Handle rewrite request
     */
    internal fun handleRewriteRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "rewrite"

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot process secure fields")
            return
        }

        val textToProcess = if (contextData.selectedText.isNotBlank()) {
            contextData.selectedText
        } else {
            contextData.beforeCursorText
        }

        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to rewrite")
            return
        }

        keyboardViewManager.hideModeChips()
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestRewrite(textToProcess) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    /**
     * Handle continue writing request
     */
    internal fun handleContinueRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "continue"

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot process secure fields")
            return
        }

        if (contextData.beforeCursorText.isBlank()) {
            suggestionBarManager.showErrorState("Start typing to continue")
            return
        }

        keyboardViewManager.hideModeChips()
        keyboardViewManager.showResultsPanel(loading = true)

        aiReplyManager.requestContinue(contextData.beforeCursorText) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    /**
     * Handle translate request
     */
    internal fun handleTranslateRequest() {
        val info = currentInputEditorInfo
        val connection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, connection)
        lastContext = contextData
        currentAiFeature = "translate"

        if (contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot process secure fields")
            return
        }

        val textToProcess = if (contextData.selectedText.isNotBlank()) {
            contextData.selectedText
        } else {
            contextData.beforeCursorText
        }

        if (textToProcess.isBlank()) {
            suggestionBarManager.showErrorState("Select text to translate")
            return
        }

        keyboardViewManager.showModeChips(
            listOf("Spanish", "French", "German")  // Future: more languages
        )
        keyboardViewManager.showResultsPanel(loading = true)

        // Default to Spanish for now
        aiReplyManager.requestTranslate(textToProcess, "Spanish") { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> {
                    keyboardViewManager.showResultsPanel(loading = true)
                }
                is AiReplyManager.AiReplyResult.Success -> {
                    keyboardViewManager.displayResults(result.suggestions)
                }
                is AiReplyManager.AiReplyResult.Failure -> {
                    keyboardViewManager.hideResultsPanel()
                    suggestionBarManager.showErrorState(result.errorMessage)
                }
            }
        }
    }

    private fun retryLastRequest() {
        val contextData = lastContext

        if (contextData == null || contextData.isSecureField) {
            suggestionBarManager.showErrorState("Cannot retry in this field")
            return
        }

        if (contextData.beforeCursorText.isBlank() && contextData.selectedText.isBlank()) {
            suggestionBarManager.showErrorState("No context available")
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
    }
}
