package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import com.yourapp.aikeyboard.R

class GlideTypingActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.glide_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_glide_typing, contentContainer, false)
        contentContainer.addView(view)

        val glideSwitch = view.findViewById<Switch>(R.id.switchGlideTyping)
        glideSwitch.isChecked = settingsRepository.isGlideTypingEnabled()
        glideSwitch.setOnCheckedChangeListener { _, checked -> settingsRepository.setGlideTypingEnabled(checked) }
    }
}
