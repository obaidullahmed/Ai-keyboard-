package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.yourapp.aikeyboard.R
import com.yourapp.aikeyboard.settings.KeyboardTheme
import com.yourapp.aikeyboard.settings.SettingsRepository

class KeyboardViewManager(
    private val rootView: View,
    private val actionHandler: KeyboardActionHandler,
    private val suggestionBarManager: SuggestionBarManager,
    private val settingsRepository: SettingsRepository
) {

    fun initializeKeyboard() {
        bindCharacterKeys()
        bindActionKeys()
        applyPreferences()
        applyTheme(settingsRepository.getKeyboardTheme())
    }

    fun setAiButtonEnabled(enabled: Boolean) {
        val aiButton = rootView.findViewById<Button>(R.id.keyAiAction)
        aiButton.isEnabled = enabled
        aiButton.alpha = if (enabled) 1.0f else 0.5f
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
    }

    private fun applyPreferences() {
        val showNumberRow = settingsRepository.isNumberRowEnabled()
        val numberKeyIds = listOf(
            R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
            R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0
        )
        numberKeyIds.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.visibility = if (showNumberRow) View.VISIBLE else View.GONE
        }
        suggestionBarManager.setSuggestionsEnabled(settingsRepository.isSuggestionsEnabled())
    }

    fun applyTheme(theme: KeyboardTheme) {
        when (theme) {
            KeyboardTheme.DARK -> {
                rootView.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.themeDarkBackground))
                setKeyBackground(R.color.keyboardKey, R.color.keyboardKeyText)
            }
            KeyboardTheme.SOFT -> {
                rootView.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.themeSoftBackground))
                setKeyBackground(R.color.button_secondary, R.color.text_primary)
            }
            KeyboardTheme.NEON -> {
                rootView.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.themeNeonBackground))
                setKeyBackground(R.color.keyboardAiKey, R.color.text_primary)
            }
        }
    }

    private fun setKeyBackground(keyColorRes: Int, textColorRes: Int) {
        val keyColor = ContextCompat.getColor(rootView.context, keyColorRes)
        val textColor = ContextCompat.getColor(rootView.context, textColorRes)
        val keys = listOf(
            R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
            R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0,
            R.id.keyQ, R.id.keyW, R.id.keyE, R.id.keyR, R.id.keyT,
            R.id.keyY, R.id.keyU, R.id.keyI, R.id.keyO, R.id.keyP,
            R.id.keyA, R.id.keyS, R.id.keyD, R.id.keyF, R.id.keyG,
            R.id.keyH, R.id.keyJ, R.id.keyK, R.id.keyL,
            R.id.keyZ, R.id.keyX, R.id.keyC, R.id.keyV, R.id.keyB,
            R.id.keyN, R.id.keyM, R.id.keyBackspace, R.id.keySpace, R.id.keyEnter
        )
        keys.forEach { keyId ->
            rootView.findViewById<Button>(keyId)?.apply {
                setBackgroundColor(keyColor)
                setTextColor(textColor)
            }
        }
    }
}
