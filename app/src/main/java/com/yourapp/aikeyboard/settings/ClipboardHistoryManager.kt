package com.yourapp.aikeyboard.settings

import android.content.ClipboardManager
import android.content.Context

class ClipboardHistoryManager(
    context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val clipText = clip.getItemAt(0).coerceToText(context).toString()
            settingsRepository.addClipboardEntry(clipText)
        }
    }

    fun startListening() {
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    fun stopListening() {
        clipboardManager.removePrimaryClipChangedListener(listener)
    }
}
