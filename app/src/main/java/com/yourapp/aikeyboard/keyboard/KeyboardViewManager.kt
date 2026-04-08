package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.Button
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
    private val panelCloseKeyboard: Button? = rootView.findViewById(R.id.panelCloseKeyboard)

    private val numberRow: LinearLayout? = rootView.findViewById(R.id.numberRow)

    private var isShiftActive: Boolean = false
    private var currentLanguage: String = "English"

    fun initializeKeyboard() {
        bindCharacterKeys()
        bindActionKeys()
        bindAiToolButtons()
        bindModeChips()
        bindResultCards()
        bindEmojiButtons()
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
        }
    }

    fun cycleKeyboardMode() {
        val isVisible = numberRow?.visibility == View.VISIBLE
        numberRow?.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    fun updateShiftState(active: Boolean) {
        isShiftActive = active
        updateShiftLabels()
    }

    fun setKeyboardLanguage(language: String) {
        currentLanguage = language
        languageIndicator?.text = if (language.equals("Bangla", ignoreCase = true)) "BN" else "EN"
        applyLanguageLabels(language)
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

        val mapping = if (language.equals("Bangla", ignoreCase = true)) banglaMapping else englishMapping
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
            rootView.findViewById<Button>(keyId)?.setOnClickListener {
                val value = (it as? Button)?.text?.toString()?.let { label ->
                    if (label.length == 1 || label.length == 2) label else label.lowercase()
                } ?: return@setOnClickListener
                actionHandler.onCharacterKey(value)
            }
        }
    }

    private fun bindActionKeys() {
        rootView.findViewById<Button>(R.id.keyBackspace)?.setOnClickListener {
            actionHandler.onBackspace()
        }

        rootView.findViewById<Button>(R.id.keySpace)?.setOnClickListener {
            actionHandler.onSpace()
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

    private fun bindPanelCloseButton() {
        panelCloseKeyboard?.setOnClickListener {
            showEmojiPanel(false)
        }
    }

    private fun applyPreferences() {
        val showNumberRow = settingsRepository.isNumberRowEnabled()
        numberRow?.visibility = if (showNumberRow) View.VISIBLE else View.GONE

        suggestionBarManager.setSuggestionsEnabled(settingsRepository.isSuggestionsEnabled())
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
}
