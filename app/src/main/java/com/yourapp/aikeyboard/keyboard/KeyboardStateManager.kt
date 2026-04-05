package com.yourapp.aikeyboard.keyboard

class KeyboardStateManager {
    var isSecureInput: Boolean = false
        private set

    fun markSecureInput(isSecure: Boolean) {
        isSecureInput = isSecure
    }
}
