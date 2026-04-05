package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import com.yourapp.aikeyboard.R

class EmojisActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.media_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_emojis, contentContainer, false)
        contentContainer.addView(view)

        val emojiSwitch = view.findViewById<Switch>(R.id.switchEmojis)
        val stickerSwitch = view.findViewById<Switch>(R.id.switchStickers)
        emojiSwitch.isChecked = settingsRepository.isEmojiSuggestionsEnabled()
        stickerSwitch.isChecked = settingsRepository.isStickerPanelEnabled()

        emojiSwitch.setOnCheckedChangeListener { _, checked -> settingsRepository.setEmojiSuggestionsEnabled(checked) }
        stickerSwitch.setOnCheckedChangeListener { _, checked -> settingsRepository.setStickerPanelEnabled(checked) }
    }
}
