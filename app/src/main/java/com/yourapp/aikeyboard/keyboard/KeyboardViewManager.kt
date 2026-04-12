package com.yourapp.aikeyboard.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.yourapp.aikeyboard.R
import com.yourapp.aikeyboard.ai.ToneMode
import com.yourapp.aikeyboard.settings.KeyboardTheme
import com.yourapp.aikeyboard.settings.SettingsRepository

/**
 * Manages all keyboard UI elements and their interactions
 */
class KeyboardViewManager(
    private val rootView: View,
    private val actionHandler: KeyboardActionHandler,
    private val suggestionBarManager: SuggestionBarManager,
    private val settingsRepository: SettingsRepository
) {

    private val previewBar: LinearLayout? = rootView.findViewById(R.id.previewBar)
    private val contextPreviewText: TextView? = rootView.findViewById(R.id.contextPreviewText)
    private val languageIndicator: TextView? = rootView.findViewById(R.id.languageIndicator)
    private val previewActionButton: Button? = rootView.findViewById(R.id.previewActionButton)
    private val clipboardButton: Button? = rootView.findViewById(R.id.keyClipboard)
    private val voiceButton: Button? = rootView.findViewById(R.id.keyVoice)

    private val aiToolsPanel: LinearLayout? = rootView.findViewById(R.id.aiToolsPanel)
    private val toolGrammar: Button? = rootView.findViewById(R.id.toolGrammar)
    private val toolTone: Button? = rootView.findViewById(R.id.toolTone)
    private val toolRewrite: Button? = rootView.findViewById(R.id.toolRewrite)
    private val toolContinue: Button? = rootView.findViewById(R.id.toolContinue)
    private val toolTranslate: Button? = rootView.findViewById(R.id.toolTranslate)

    private val modeChip1: Button? = rootView.findViewById(R.id.modeChip1)
    private val modeChip2: Button? = rootView.findViewById(R.id.modeChip2)
    private val modeChip3: Button? = rootView.findViewById(R.id.modeChip3)

    private val resultsCardsPanel: LinearLayout? = rootView.findViewById(R.id.resultsCardsPanel)
    private val resultsLoadingProgress: ProgressBar? = rootView.findViewById(R.id.resultsLoadingProgress)
    private val resultsStatusText: TextView? = rootView.findViewById(R.id.resultsStatusText)
    private val resultCard1: Button? = rootView.findViewById(R.id.resultCard1)
    private val resultCard2: Button? = rootView.findViewById(R.id.resultCard2)
    private val resultCard3: Button? = rootView.findViewById(R.id.resultCard3)

    private val emojiPanel: LinearLayout? = rootView.findViewById(R.id.emojiPanel)
    private val emojiSearchInput: EditText? = rootView.findViewById(R.id.emojiSearchInput)
    private val panelCloseKeyboard: Button? = rootView.findViewById(R.id.panelCloseKeyboard)

    private val clipboardPanel: LinearLayout? = rootView.findViewById(R.id.clipboardPanel)
    private val clipboardHistoryContainer: LinearLayout? = rootView.findViewById(R.id.clipboardHistoryContainer)
    private val clipboardClearButton: Button? = rootView.findViewById(R.id.clipboardClearButton)

    private val numberRow: LinearLayout? = rootView.findViewById(R.id.numberRow)
    private val keyboardKeysContainer: LinearLayout? = rootView.findViewById(R.id.keyboardKeysContainer)

    private var isShiftActive: Boolean = false
    private var isSymbolMode: Boolean = false
    private var currentLanguage: String = "English"

    private val emojiList = listOf(
        "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎", "🤩", "🤔",
        "🙌", "👏", "👍", "👎", "🙏", "🔥", "✨", "🎉", "💬", "❤️",
        "🥳", "😢", "😮", "🤗", "😇", "😴", "😜", "😡", "💡", "🎯"
    )

    private val emojiKeywords = mapOf(
        "😀" to listOf("smile", "happy"),
        "😂" to listOf("laugh", "funny"),
        "😍" to listOf("love", "heart"),
        "👍" to listOf("like", "good"),
        "🙏" to listOf("thanks", "please"),
        "🔥" to listOf("hot", "fire", "great"),
        "🎉" to listOf("party", "celebrate"),
        "💬" to listOf("chat", "message")
    )

    private val symbolModeMapping = mapOf(
        R.id.keyQ to "!", R.id.keyW to "@", R.id.keyE to "#", R.id.keyR to "$", R.id.keyT to "%",
        R.id.keyY to "^", R.id.keyU to "&", R.id.keyI to "*", R.id.keyO to "(", R.id.keyP to ")",
        R.id.keyA to "-", R.id.keyS to "=", R.id.keyD to "_", R.id.keyF to "+", R.id.keyG to "{",
        R.id.keyH to "}", R.id.keyJ to "[", R.id.keyK to "]", R.id.keyL to ":",
        R.id.keyZ to ";", R.id.keyX to '"'.toString(), R.id.keyC to "'", R.id.keyV to "<", R.id.keyB to ">",
        R.id.keyN to "/", R.id.keyM to "?"
    )

    private val longPressCharacters = mapOf(
        R.id.keyQ to "1", R.id.keyW to "2", R.id.keyE to "3", R.id.keyR to "4", R.id.keyT to "5",
        R.id.keyY to "6", R.id.keyU to "7", R.id.keyI to "8", R.id.keyO to "9", R.id.keyP to "0",
        R.id.keyA to "@", R.id.keyS to "#", R.id.keyD to "$", R.id.keyF to "%", R.id.keyG to "&",
        R.id.keyH to "*", R.id.keyJ to "(", R.id.keyK to ")", R.id.keyL to "-", R.id.keyZ to "+",
        R.id.keyX to "=", R.id.keyC to "_", R.id.keyV to ";", R.id.keyB to ":", R.id.keyN to "'",
        R.id.keyM to "\""
    )

    fun initializeKeyboard() {
        bindCharacterKeys()
        bindActionKeys()
        bindAiToolButtons()
        bindModeChips()
        bindResultCards()
        bindEmojiButtons()
        bindEmojiSearch()
        bindClipboardControls()
        bindPanelCloseButton()
        applyPreferences()
        applyTheme(settingsRepository.getKeyboardTheme())
        setKeyboardLanguage(settingsRepository.getCurrentLanguage())
    }

    fun setAiButtonEnabled(enabled: Boolean) {
        val aiButton = rootView.findViewById<Button>(R.id.keyAiAction)
        aiButton?.isEnabled = enabled
        aiButton?.alpha = if (enabled) 1.0f else 0.5f
    }

    fun setVoiceTypingEnabled(enabled: Boolean) {
        voiceButton?.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    fun updateContextPreview(text: String) {
        contextPreviewText?.text = if (text.isBlank()) "Ready to type..." else text.take(50)
    }

    fun showResultsPanel(loading: Boolean = false) {
        resultsCardsPanel?.visibility = View.VISIBLE
        if (loading) {
            resultsLoadingProgress?.visibility = View.VISIBLE
            resultsStatusText?.text = "Generating..."
            resultCard1?.visibility = View.GONE
            resultCard2?.visibility = View.GONE
            resultCard3?.visibility = View.GONE
        }
    }

    fun hideResultsPanel() {
        resultsCardsPanel?.visibility = View.GONE
    }

    fun displayResults(results: List<String>) {
        resultsLoadingProgress?.visibility = View.GONE
        resultsStatusText?.text = "Select a result to insert:"

        val resultCards = listOf(resultCard1, resultCard2, resultCard3)
        resultCards.forEachIndexed { index, card ->
            card?.apply {
                text = results.getOrNull(index)?.take(100) ?: ""
                visibility = if (results.getOrNull(index).isNullOrBlank()) View.GONE else View.VISIBLE
            }
        }
    }

    fun showModeChips(modes: List<String>) {
        val chips = listOf(modeChip1, modeChip2, modeChip3)
        chips.forEachIndexed { index, chip ->
            chip?.apply {
                text = modes.getOrNull(index).orEmpty()
                visibility = if (index < modes.size) View.VISIBLE else View.GONE
            }
        }
    }

    fun hideModeChips() {
        listOf(modeChip1, modeChip2, modeChip3).forEach { it?.visibility = View.GONE }
    }

    fun showAiToolsPanel(show: Boolean) {
        aiToolsPanel?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showEmojiPanel(show: Boolean) {
        emojiPanel?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun toggleEmojiPanel() {
        val visible = emojiPanel?.visibility != View.VISIBLE
        showEmojiPanel(visible)
        if (visible) {
            resultsCardsPanel?.visibility = View.GONE
            showClipboardPanel(false)
        }
    }

    fun showClipboardPanel(show: Boolean) {
        clipboardPanel?.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            showEmojiPanel(false)
            refreshClipboardHistory()
        }
    }

    fun toggleKeyboardMode() {
        isSymbolMode = !isSymbolMode
        applyLanguageLabels(currentLanguage)
        rootView.findViewById<Button>(R.id.keySwitchMode)?.text = if (isSymbolMode) "ABC" else "?123"
    }

    fun updateShiftState(active: Boolean) {
        isShiftActive = active
        updateShiftLabels()
    }

    fun setKeyboardLanguage(language: String) {
        currentLanguage = language
        languageIndicator?.text = if (language.equals("Bangla", ignoreCase = true)) "BN" else "EN"
        applyLanguageLabels(language)
        setVoiceTypingEnabled(settingsRepository.isVoiceTypingEnabled())
    }

    fun refreshClipboardHistory() {
        val clipboardManager = rootView.context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val systemText = clipboardManager?.primaryClip?.getItemAt(0)?.coerceToText(rootView.context)?.toString()?.trim().orEmpty()
        if (systemText.isNotBlank()) {
            settingsRepository.addClipboardEntry(systemText)
        }

        clipboardHistoryContainer?.removeAllViews()
        val history = settingsRepository.getClipboardHistory().take(4)

        if (history.isEmpty()) {
            val emptyView = TextView(rootView.context).apply {
                text = rootView.context.getString(R.string.clipboard_history_empty)
                setTextColor(ContextCompat.getColor(rootView.context, R.color.colorTextSecondary))
                textSize = 12f
                setPadding(8, 8, 8, 8)
            }
            clipboardHistoryContainer?.addView(emptyView)
            return
        }

        history.forEach { item ->
            val historyButton = Button(rootView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
                text = item.take(36)
                setBackgroundResource(R.drawable.shape_surface_card)
                setTextColor(ContextCompat.getColor(rootView.context, R.color.colorTextPrimary))
                textSize = 12f
                setOnClickListener {
                    actionHandler.onPasteClipboardText(item)
                    showClipboardPanel(false)
                }
            }
            clipboardHistoryContainer?.addView(historyButton)
        }
    }

    fun updateShiftLabels() {
        val characterKeys = listOf(
            R.id.keyQ, R.id.keyW, R.id.keyE, R.id.keyR, R.id.keyT,
            R.id.keyY, R.id.keyU, R.id.keyI, R.id.keyO, R.id.keyP,
            R.id.keyA, R.id.keyS, R.id.keyD, R.id.keyF, R.id.keyG,
            R.id.keyH, R.id.keyJ, R.id.keyK, R.id.keyL,
            R.id.keyZ, R.id.keyX, R.id.keyC, R.id.keyV, R.id.keyB,
            R.id.keyN, R.id.keyM
        )

        characterKeys.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.text =
                rootView.findViewById<Button>(keyId)?.text?.toString()?.let { label ->
                    if (isShiftActive) label.uppercase() else label.lowercase()
                }
        }
    }

    private fun applyLanguageLabels(language: String) {
        val englishMapping = mapOf(
            R.id.keyQ to "q", R.id.keyW to "w", R.id.keyE to "e", R.id.keyR to "r", R.id.keyT to "t",
            R.id.keyY to "y", R.id.keyU to "u", R.id.keyI to "i", R.id.keyO to "o", R.id.keyP to "p",
            R.id.keyA to "a", R.id.keyS to "s", R.id.keyD to "d", R.id.keyF to "f", R.id.keyG to "g",
            R.id.keyH to "h", R.id.keyJ to "j", R.id.keyK to "k", R.id.keyL to "l",
            R.id.keyZ to "z", R.id.keyX to "x", R.id.keyC to "c", R.id.keyV to "v", R.id.keyB to "b",
            R.id.keyN to "n", R.id.keyM to "m"
        )

        val banglaMapping = mapOf(
            R.id.keyQ to "ক", R.id.keyW to "খ", R.id.keyE to "গ", R.id.keyR to "ঘ", R.id.keyT to "ঙ",
            R.id.keyY to "চ", R.id.keyU to "ছ", R.id.keyI to "জ", R.id.keyO to "ঝ", R.id.keyP to "ঞ",
            R.id.keyA to "ট", R.id.keyS to "ঠ", R.id.keyD to "ড", R.id.keyF to "ঢ", R.id.keyG to "ণ",
            R.id.keyH to "ত", R.id.keyJ to "থ", R.id.keyK to "দ", R.id.keyL to "ধ",
            R.id.keyZ to "প", R.id.keyX to "ফ", R.id.keyC to "ব", R.id.keyV to "ম", R.id.keyB to "য",
            R.id.keyN to "র", R.id.keyM to "ল"
        )

        val mapping = if (isSymbolMode) symbolModeMapping else if (language.equals("Bangla", ignoreCase = true)) banglaMapping else englishMapping
        mapping.forEach { (keyId, label) ->
            rootView.findViewById<Button>(keyId)?.text = if (isShiftActive) label.uppercase() else label
        }
    }

    private fun bindCharacterKeys() {
        val keyIds = listOf(
            R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
            R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0,
            R.id.keyQ, R.id.keyW, R.id.keyE, R.id.keyR, R.id.keyT,
            R.id.keyY, R.id.keyU, R.id.keyI, R.id.keyO, R.id.keyP,
            R.id.keyA, R.id.keyS, R.id.keyD, R.id.keyF, R.id.keyG,
            R.id.keyH, R.id.keyJ, R.id.keyK, R.id.keyL,
            R.id.keyZ, R.id.keyX, R.id.keyC, R.id.keyV, R.id.keyB,
            R.id.keyN, R.id.keyM
        )

        keyIds.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.apply {
                setOnClickListener {
                    val value = text?.toString().orEmpty()
                    actionHandler.onCharacterKey(value)
                }
                setOnLongClickListener {
                    longPressCharacters[keyId]?.let { actionHandler.onCharacterKey(it); true } ?: false
                }
            }
        }
    }

    private fun bindActionKeys() {
        rootView.findViewById<Button>(R.id.keyBackspace)?.setOnClickListener {
            actionHandler.onBackspace()
        }

        rootView.findViewById<Button>(R.id.keyBackspace)?.setOnLongClickListener {
            actionHandler.onBackspaceWord()
            true
        }

        var spaceTouchStartX = 0f

        rootView.findViewById<Button>(R.id.keySpace)?.setOnClickListener {
            actionHandler.onSpace()
        }

        rootView.findViewById<Button>(R.id.keySpace)?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    spaceTouchStartX = event.x
                }
                MotionEvent.ACTION_UP -> {
                    val touchDeltaX = event.x - spaceTouchStartX
                    if (touchDeltaX < -100) {
                        actionHandler.onSpaceGestureMove(-1)
                        return@setOnTouchListener true
                    }
                    if (touchDeltaX > 100) {
                        actionHandler.onSpaceGestureMove(1)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        rootView.findViewById<Button>(R.id.keyEnter)?.setOnClickListener {
            actionHandler.onEnter()
        }

        rootView.findViewById<Button>(R.id.keyAiAction)?.setOnClickListener {
            actionHandler.onAiButtonClicked()
        }

        rootView.findViewById<Button>(R.id.keyShift)?.setOnClickListener {
            actionHandler.onShiftToggled()
        }

        rootView.findViewById<Button>(R.id.keySwitchMode)?.setOnClickListener {
            actionHandler.onSwitchModeClicked()
        }

        rootView.findViewById<Button>(R.id.keyEmoji)?.setOnClickListener {
            actionHandler.onEmojiClicked()
        }

        voiceButton?.setOnClickListener {
            actionHandler.onVoiceTypingClicked()
        }

        clipboardButton?.setOnClickListener {
            showClipboardPanel(clipboardPanel?.visibility != View.VISIBLE)
        }

        languageIndicator?.setOnClickListener {
            actionHandler.onLanguageSwitchPressed()
        }

        previewActionButton?.setOnClickListener {
            actionHandler.onAiButtonClicked()
        }
    }

    private fun bindAiToolButtons() {
        toolGrammar?.setOnClickListener {
            actionHandler.onGrammarToolClicked()
        }

        toolTone?.setOnClickListener {
            actionHandler.onToneToolClicked()
        }

        toolRewrite?.setOnClickListener {
            actionHandler.onRewriteToolClicked()
        }

        toolContinue?.setOnClickListener {
            actionHandler.onContinueToolClicked()
        }

        toolTranslate?.setOnClickListener {
            actionHandler.onTranslateToolClicked()
        }
    }

    private fun bindModeChips() {
        val modeChips = listOf(modeChip1, modeChip2, modeChip3)
        modeChips.forEachIndexed { index, chip ->
            chip?.setOnClickListener {
                actionHandler.onModeChipSelected(index)
            }
        }
    }

    private fun bindResultCards() {
        val resultCards = listOf(resultCard1, resultCard2, resultCard3)
        resultCards.forEach { card ->
            card?.setOnClickListener {
                val text = (it as Button).text.toString()
                actionHandler.onResultCardSelected(text)
            }
        }
    }

    private fun bindEmojiButtons() {
        val emojiButtons = listOf(
            R.id.emojiButton1, R.id.emojiButton2, R.id.emojiButton3, R.id.emojiButton4, R.id.emojiButton5,
            R.id.emojiButton6, R.id.emojiButton7, R.id.emojiButton8, R.id.emojiButton9, R.id.emojiButton10
        )

        emojiButtons.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.setOnClickListener {
                val emoji = (it as? Button)?.text?.toString() ?: return@setOnClickListener
                actionHandler.onCharacterKey(emoji)
            }
        }
    }

    private fun bindEmojiSearch() {
        emojiSearchInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateEmojiButtons(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun bindClipboardControls() {
        clipboardClearButton?.setOnClickListener {
            settingsRepository.clearClipboardHistory()
            refreshClipboardHistory()
        }
    }

    private fun bindPanelCloseButton() {
        panelCloseKeyboard?.setOnClickListener {
            showEmojiPanel(false)
        }
    }

    internal fun applyPreferences() {
        val showNumberRow = settingsRepository.isNumberRowEnabled()
        numberRow?.visibility = if (showNumberRow) View.VISIBLE else View.GONE
        suggestionBarManager.setSuggestionsEnabled(settingsRepository.isSuggestionsEnabled())
        setVoiceTypingEnabled(settingsRepository.isVoiceTypingEnabled())
        isSymbolMode = false
        currentLanguage = settingsRepository.getCurrentLanguage()
        applyLanguageLabels(currentLanguage)
        updateOneHandedMode(settingsRepository.isOneHandedModeEnabled())
    }

    private fun updateOneHandedMode(enabled: Boolean) {
        val padding = if (enabled) 60 else 0
        keyboardKeysContainer?.setPadding(24, 0, padding, 0)
    }

    fun applyTheme(theme: KeyboardTheme) {
        val context = rootView.context
        when (theme) {
            KeyboardTheme.DARK -> {
                rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorKeyboardBackground))
                setKeyBackground(R.color.colorKeyBackground, R.color.colorTextPrimary)
            }
            KeyboardTheme.SOFT -> {
                rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSurface))
                setKeyBackground(R.color.colorElevatedSurface, R.color.colorTextPrimary)
            }
            KeyboardTheme.NEON -> {
                rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorKeyboardBackground))
                setKeyBackground(R.color.colorPrimary, R.color.colorTextPrimary)
            }
        }
    }

    private fun setKeyBackground(keyColorRes: Int, textColorRes: Int) {
        val textColor = ContextCompat.getColor(rootView.context, textColorRes)

        val allKeyIds = listOf(
            R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
            R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0,
            R.id.keyQ, R.id.keyW, R.id.keyE, R.id.keyR, R.id.keyT,
            R.id.keyY, R.id.keyU, R.id.keyI, R.id.keyO, R.id.keyP,
            R.id.keyA, R.id.keyS, R.id.keyD, R.id.keyF, R.id.keyG,
            R.id.keyH, R.id.keyJ, R.id.keyK, R.id.keyL,
            R.id.keyZ, R.id.keyX, R.id.keyC, R.id.keyV, R.id.keyB,
            R.id.keyN, R.id.keyM, R.id.keySpace
        )

        allKeyIds.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.setTextColor(textColor)
        }
    }

    private fun updateEmojiButtons(query: String) {
        val filtered = if (query.isBlank()) {
            emojiList
        } else {
            emojiList.filter { emoji ->
                emojiKeywords[emoji]?.any { it.contains(query, ignoreCase = true) } == true || emoji.contains(query)
            }.take(10).ifEmpty { emojiList.take(10) }
        }

        val emojiButtons = listOf(
            R.id.emojiButton1, R.id.emojiButton2, R.id.emojiButton3, R.id.emojiButton4, R.id.emojiButton5,
            R.id.emojiButton6, R.id.emojiButton7, R.id.emojiButton8, R.id.emojiButton9, R.id.emojiButton10
        )

        emojiButtons.forEachIndexed { index, keyId ->
            rootView.findViewById<Button>(keyId)?.apply {
                text = filtered.getOrNull(index).orEmpty()
                visibility = if (index < filtered.size) View.VISIBLE else View.GONE
            }
        }
    }
}
