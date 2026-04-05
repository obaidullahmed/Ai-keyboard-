package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import com.yourapp.aikeyboard.R

class ThemeActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.theme_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_theme, contentContainer, false)
        contentContainer.addView(view)

        val radioDark = view.findViewById<RadioButton>(R.id.radioDark)
        val radioSoft = view.findViewById<RadioButton>(R.id.radioSoft)
        val radioNeon = view.findViewById<RadioButton>(R.id.radioNeon)
        val applyButton = view.findViewById<Button>(R.id.buttonApplyTheme)

        when (settingsRepository.getKeyboardTheme()) {
            KeyboardTheme.DARK -> radioDark.isChecked = true
            KeyboardTheme.SOFT -> radioSoft.isChecked = true
            KeyboardTheme.NEON -> radioNeon.isChecked = true
        }

        applyButton.setOnClickListener {
            val selectedTheme = when {
                radioSoft.isChecked -> KeyboardTheme.SOFT
                radioNeon.isChecked -> KeyboardTheme.NEON
                else -> KeyboardTheme.DARK
            }
            settingsRepository.setKeyboardTheme(selectedTheme)
            finish()
        }
    }
}
