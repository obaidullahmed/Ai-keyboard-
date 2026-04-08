package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.Button
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

    // Preview bar
    private val previewBar: LinearLayout? = rootView.findViewById(R.id.previewBar)
    private val contextPreviewText: TextView? = rootView.findViewById(R.id.contextPreviewText)
    private val previewActionButton: Button? = rootView.findViewById(R.id.previewActionButton)

    // AI tools panel
    private val aiToolsPanel: LinearLayout? = rootView.findViewById(R.id.aiToolsPanel)
    private val toolGrammar: Button? = rootView.findViewById(R.id.toolGrammar)
    private val toolTone: Button? = rootView.findViewById(R.id.toolTone)
    private val toolRewrite: Button? = rootView.findViewById(R.id.toolRewrite)
    private val toolContinue: Button? = rootView.findViewById(R.id.toolContinue)
    private val toolTranslate: Button? = rootView.findViewById(R.id.toolTranslate)

    // Mode chips
    private val modeChip1: Button? = rootView.findViewById(R.id.modeChip1)
    private val modeChip2: Button? = rootView.findViewById(R.id.modeChip2)
    private val modeChip3: Button? = rootView.findViewById(R.id.modeChip3)

    // Results cards
    private val resultsCardsPanel: LinearLayout? = rootView.findViewById(R.id.resultsCardsPanel)
    private val resultsLoadingProgress: ProgressBar? = rootView.findViewById(R.id.resultsLoadingProgress)
    private val resultsStatusText: TextView? = rootView.findViewById(R.id.resultsStatusText)
    private val resultCard1: Button? = rootView.findViewById(R.id.resultCard1)
    private val resultCard2: Button? = rootView.findViewById(R.id.resultCard2)
    private val resultCard3: Button? = rootView.findViewById(R.id.resultCard3)

    fun initializeKeyboard() {
        bindCharacterKeys()
        bindActionKeys()
        bindAiToolButtons()
        bindModeChips()
        bindResultCards()
        applyPreferences()
        applyTheme(settingsRepository.getKeyboardTheme())
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
        results.forEachIndexed { index, result ->
            if (index < resultCards.size) {
                resultCards[index]?.apply {
                    text = result
                    visibility = View.VISIBLE
                }
            }
        }
    }

    fun showModeChips(modes: List<String>) {
        val chips = listOf(modeChip1, modeChip2, modeChip3)
        modes.forEachIndexed { index, mode ->
            if (index < chips.size) {
                chips[index]?.apply {
                    text = mode
                    visibility = View.VISIBLE
                }
            }
        }
    }

    fun hideModeChips() {
        listOf(modeChip1, modeChip2, modeChip3).forEach { it?.visibility = View.GONE }
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
                val value = (it as? Button)?.text?.toString()?.lowercase() ?: return@setOnClickListener
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
            // Future: number/symbol mode switching
        }

        rootView.findViewById<Button>(R.id.keyEmoji)?.setOnClickListener {
            // Future: emoji picker
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
        resultCards.forEachIndexed { index, card ->
            card?.setOnClickListener {
                val text = (it as Button).text.toString()
                actionHandler.onResultCardSelected(text)
            }
        }
    }

    private fun applyPreferences() {
        val showNumberRow = settingsRepository.isNumberRowEnabled()
        rootView.findViewById<LinearLayout>(R.id.numberRow)?.visibility =
            if (showNumberRow) View.VISIBLE else View.GONE

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
        val keyColor = ContextCompat.getColor(rootView.context, keyColorRes)
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
