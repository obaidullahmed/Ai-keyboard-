package com.yourapp.aikeyboard.keyboard

import android.view.View
import android.widget.Button
import com.yourapp.aikeyboard.R

class KeyboardViewManager(
    private val rootView: View,
    private val actionHandler: KeyboardActionHandler,
    private val suggestionBarManager: SuggestionBarManager
) {

    fun initializeKeyboard() {
        bindCharacterKeys()
        bindActionKeys()
    }

    fun setAiButtonEnabled(enabled: Boolean) {
        val aiButton = rootView.findViewById<Button>(R.id.keyAiAction)
        aiButton.isEnabled = enabled
        aiButton.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun bindCharacterKeys() {
        val letterKeyIds = listOf(
            R.id.keyQ, R.id.keyW, R.id.keyE, R.id.keyR, R.id.keyT,
            R.id.keyY, R.id.keyU, R.id.keyI, R.id.keyO, R.id.keyP,
            R.id.keyA, R.id.keyS, R.id.keyD, R.id.keyF, R.id.keyG,
            R.id.keyH, R.id.keyJ, R.id.keyK, R.id.keyL,
            R.id.keyZ, R.id.keyX, R.id.keyC, R.id.keyV, R.id.keyB,
            R.id.keyN, R.id.keyM
        )

        letterKeyIds.forEach { keyId ->
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
}
