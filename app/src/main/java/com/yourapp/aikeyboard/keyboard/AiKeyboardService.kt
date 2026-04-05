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
import com.yourapp.aikeyboard.utils.Constants

class AiKeyboardService : InputMethodService() {

    private lateinit var keyboardViewManager: KeyboardViewManager
    private lateinit var keyboardActionHandler: KeyboardActionHandler
    private lateinit var suggestionBarManager: SuggestionBarManager
    private lateinit var textCommitManager: TextCommitManager
    private lateinit var inputContextReader: InputContextReader
    private lateinit var keyboardStateManager: KeyboardStateManager
    private lateinit var aiReplyManager: AiReplyManager

    private var lastContext: InputContextReader.InputContextData? = null
    private var currentReplyMode: ReplyMode = ReplyMode.SHORT

    override fun onCreate() {
        super.onCreate()
        keyboardStateManager = KeyboardStateManager()
        textCommitManager = TextCommitManager(this)
        inputContextReader = InputContextReader()

        val apiService = HttpAiApiService(Constants.AI_ENDPOINT_URL, Constants.AI_API_KEY)
        aiReplyManager = AiReplyManager(apiService, PromptBuilder(), AiResponseParser())
    }

    override fun onCreateInputView(): View {
        val rootView = LayoutInflater.from(this).inflate(R.layout.keyboard_layout, null, false)
        suggestionBarManager = SuggestionBarManager(rootView, this::handleSuggestionSelection, this::retryLastRequest)
        keyboardActionHandler = KeyboardActionHandler(this, textCommitManager)
        keyboardViewManager = KeyboardViewManager(rootView, keyboardActionHandler, suggestionBarManager)
        keyboardViewManager.initializeKeyboard()
        return rootView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val currentConnection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, currentConnection)
        keyboardStateManager.markSecureInput(contextData.isSecureField)
        applySecureInputState(contextData.isSecureField)
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
        val currentConnection = currentInputConnection
        val contextData = inputContextReader.readCurrentContext(info, currentConnection)
        keyboardStateManager.markSecureInput(contextData.isSecureField)
        applySecureInputState(contextData.isSecureField)
    }

    override fun onDestroy() {
        aiReplyManager.shutdown()
        super.onDestroy()
    }

    private fun applySecureInputState(isSecure: Boolean) {
        // Prevent AI actions when the input field is secure or password-protected.
        suggestionBarManager.setSecureMode(isSecure)
        keyboardViewManager.setAiButtonEnabled(!isSecure)
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

        if (contextData.beforeCursorText.isBlank() && contextData.selectedText.isBlank()) {
            suggestionBarManager.showErrorState("No context available")
            return
        }

        suggestionBarManager.showLoadingState()
        aiReplyManager.requestReplies(contextData.beforeCursorText, contextData.selectedText, currentReplyMode) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> suggestionBarManager.showLoadingState()
                is AiReplyManager.AiReplyResult.Success -> suggestionBarManager.updateSuggestions(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> suggestionBarManager.showErrorState(result.errorMessage)
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

        suggestionBarManager.showLoadingState()
        aiReplyManager.requestReplies(contextData.beforeCursorText, contextData.selectedText, currentReplyMode) { result ->
            when (result) {
                is AiReplyManager.AiReplyResult.Loading -> suggestionBarManager.showLoadingState()
                is AiReplyManager.AiReplyResult.Success -> suggestionBarManager.updateSuggestions(result.suggestions)
                is AiReplyManager.AiReplyResult.Failure -> suggestionBarManager.showErrorState(result.errorMessage)
            }
        }
    }

    private fun handleSuggestionSelection(suggestionText: String) {
        if (suggestionText.isBlank()) {
            return
        }
        textCommitManager.commitText(suggestionText)
        suggestionBarManager.showIdleState()
    }
}
