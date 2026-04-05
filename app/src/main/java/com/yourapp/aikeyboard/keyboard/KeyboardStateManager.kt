package com.yourapp.aikeyboard.keyboard

class KeyboardStateManager {
    var isSecureInput: Boolean = false
        private set

    var isGlideTypingEnabled: Boolean = false
        private set

    var enabledLanguages: Set<String> = emptySet()
        private set

    fun markSecureInput(isSecure: Boolean) {
        isSecureInput = isSecure
    }

    fun updateGlideTyping(enabled: Boolean) {
        isGlideTypingEnabled = enabled
    }

    fun updateEnabledLanguages(languages: Set<String>) {
        enabledLanguages = languages
    }
}
