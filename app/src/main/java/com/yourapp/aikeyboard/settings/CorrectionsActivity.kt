package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import com.yourapp.aikeyboard.R

class CorrectionsActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.corrections_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_corrections, contentContainer, false)
        contentContainer.addView(view)

        bindSwitch(view, R.id.switchAutoCorrect) { settingsRepository.setAutoCorrectEnabled(it) }
        bindSwitch(view, R.id.switchSuggestions) { settingsRepository.setSuggestionsEnabled(it) }
        bindSwitch(view, R.id.switchCapsAssist) { settingsRepository.setCapsAssistEnabled(it) }
        bindSwitch(view, R.id.switchPunctuationAssist) { settingsRepository.setPunctuationAssistEnabled(it) }

        view.findViewById<Switch>(R.id.switchAutoCorrect).isChecked = settingsRepository.isAutoCorrectEnabled()
        view.findViewById<Switch>(R.id.switchSuggestions).isChecked = settingsRepository.isSuggestionsEnabled()
        view.findViewById<Switch>(R.id.switchCapsAssist).isChecked = settingsRepository.isCapsAssistEnabled()
        view.findViewById<Switch>(R.id.switchPunctuationAssist).isChecked = settingsRepository.isPunctuationAssistEnabled()
    }

    private fun bindSwitch(view: android.view.View, id: Int, action: (Boolean) -> Unit) {
        view.findViewById<Switch>(id).setOnCheckedChangeListener { _, checked -> action(checked) }
    }
}
