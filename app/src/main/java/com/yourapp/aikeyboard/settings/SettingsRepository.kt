package com.yourapp.aikeyboard.settings

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ai_keyboard_prefs"
        private const val KEY_LANGUAGES = "key_languages"
        private const val KEY_KEYPOPUP = "key_popup_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_NUMBER_ROW = "number_row_enabled"
        private const val KEY_AUTO_CAPS = "auto_caps_enabled"
        private const val KEY_AUTOCORRECT = "autocorrect_enabled"
        private const val KEY_SUGGESTIONS = "suggestions_enabled"
        private const val KEY_CAPS_ASSIST = "caps_assist_enabled"
        private const val KEY_PUNCTUATION_ASSIST = "punctuation_assist_enabled"
        private const val KEY_GLIDE_TYPING = "glide_typing_enabled"
        private const val KEY_VOICE_TYPING = "voice_typing_enabled"
        private const val KEY_EMOJI_SUGGESTIONS = "emoji_suggestions_enabled"
        private const val KEY_STICKER_PANEL = "sticker_panel_enabled"
        private const val KEY_THEME = "keyboard_theme"
        private const val KEY_DICTIONARY = "personal_dictionary"
        private const val KEY_CLIPBOARD_HISTORY = "clipboard_history"
    }

    fun getEnabledLanguages(): Set<String> {
        return prefs.getStringSet(KEY_LANGUAGES, setOf("English")) ?: setOf("English")
    }

    fun setEnabledLanguages(languages: Set<String>) {
        prefs.edit().putStringSet(KEY_LANGUAGES, languages).apply()
    }

    fun isKeyPopupEnabled(): Boolean = prefs.getBoolean(KEY_KEYPOPUP, true)
    fun setKeyPopupEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_KEYPOPUP, enabled).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()

    fun isNumberRowEnabled(): Boolean = prefs.getBoolean(KEY_NUMBER_ROW, false)
    fun setNumberRowEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NUMBER_ROW, enabled).apply()

    fun isAutoCapitalizationEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CAPS, true)
    fun setAutoCapitalizationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_CAPS, enabled).apply()

    fun isAutoCorrectEnabled(): Boolean = prefs.getBoolean(KEY_AUTOCORRECT, true)
    fun setAutoCorrectEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTOCORRECT, enabled).apply()

    fun isSuggestionsEnabled(): Boolean = prefs.getBoolean(KEY_SUGGESTIONS, true)
    fun setSuggestionsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SUGGESTIONS, enabled).apply()

    fun isCapsAssistEnabled(): Boolean = prefs.getBoolean(KEY_CAPS_ASSIST, true)
    fun setCapsAssistEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_CAPS_ASSIST, enabled).apply()

    fun isPunctuationAssistEnabled(): Boolean = prefs.getBoolean(KEY_PUNCTUATION_ASSIST, true)
    fun setPunctuationAssistEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_PUNCTUATION_ASSIST, enabled).apply()

    fun isGlideTypingEnabled(): Boolean = prefs.getBoolean(KEY_GLIDE_TYPING, false)
    fun setGlideTypingEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GLIDE_TYPING, enabled).apply()

    fun isVoiceTypingEnabled(): Boolean = prefs.getBoolean(KEY_VOICE_TYPING, false)
    fun setVoiceTypingEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VOICE_TYPING, enabled).apply()

    fun isEmojiSuggestionsEnabled(): Boolean = prefs.getBoolean(KEY_EMOJI_SUGGESTIONS, false)
    fun setEmojiSuggestionsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_EMOJI_SUGGESTIONS, enabled).apply()

    fun isStickerPanelEnabled(): Boolean = prefs.getBoolean(KEY_STICKER_PANEL, false)
    fun setStickerPanelEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_STICKER_PANEL, enabled).apply()

    fun getKeyboardTheme(): KeyboardTheme {
        val themeKey = prefs.getString(KEY_THEME, KeyboardTheme.DARK.key) ?: KeyboardTheme.DARK.key
        return KeyboardTheme.fromKey(themeKey)
    }

    fun setKeyboardTheme(theme: KeyboardTheme) = prefs.edit().putString(KEY_THEME, theme.key).apply()

    fun getPersonalDictionary(): List<String> = jsonToList(prefs.getString(KEY_DICTIONARY, "[]") ?: "[]")

    fun savePersonalDictionary(words: List<String>) {
        prefs.edit().putString(KEY_DICTIONARY, listToJson(words)).apply()
    }

    fun addDictionaryWord(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.isEmpty()) return
        val dictionary = getPersonalDictionary().toMutableList()
        if (!dictionary.contains(normalized)) {
            dictionary.add(normalized)
            savePersonalDictionary(dictionary)
        }
    }

    fun removeDictionaryWord(word: String) {
        val dictionary = getPersonalDictionary().toMutableList()
        if (dictionary.remove(word.trim().lowercase())) {
            savePersonalDictionary(dictionary)
        }
    }

    fun getClipboardHistory(): List<String> = jsonToList(prefs.getString(KEY_CLIPBOARD_HISTORY, "[]") ?: "[]")

    fun addClipboardEntry(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val history = getClipboardHistory().toMutableList()
        history.remove(trimmed)
        history.add(0, trimmed)
        if (history.size > 20) {
            history.subList(20, history.size).clear()
        }
        prefs.edit().putString(KEY_CLIPBOARD_HISTORY, listToJson(history)).apply()
    }

    fun clearClipboardHistory() {
        prefs.edit().putString(KEY_CLIPBOARD_HISTORY, "[]").apply()
    }

    fun getLanguageSummary(): String {
        val languages = getEnabledLanguages().toList().sorted()
        return if (languages.isEmpty()) {
            ""
        } else {
            languages.joinToString(", ")
        }
    }

    private fun listToJson(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    private fun jsonToList(json: String): List<String> {
        return try {
            val array = JSONArray(json)
            List(array.length()) { index -> array.optString(index) }
        } catch (exception: Exception) {
            emptyList()
        }
    }
}

enum class KeyboardTheme(val key: String) {
    DARK("dark"),
    SOFT("soft"),
    NEON("neon");

    companion object {
        fun fromKey(key: String): KeyboardTheme = values().firstOrNull { it.key == key } ?: DARK
    }
}
