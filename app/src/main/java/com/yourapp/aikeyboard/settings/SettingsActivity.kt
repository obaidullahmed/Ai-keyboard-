package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.yourapp.aikeyboard.R
import com.yourapp.aikeyboard.settings.KeyboardTheme
import com.yourapp.aikeyboard.settings.SettingsRepository

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsRepository = SettingsRepository(this)

        val languageStatus = findViewById<TextView>(R.id.languageStatus)
        val btnEnglish = findViewById<Button>(R.id.btnEnglish)
        val btnBangla = findViewById<Button>(R.id.btnBangla)
        val switchSound = findViewById<SwitchCompat>(R.id.switchSound)
        val switchVibration = findViewById<SwitchCompat>(R.id.switchVibration)
        val btnThemeDark = findViewById<Button>(R.id.btnThemeDark)
        val btnThemeSoft = findViewById<Button>(R.id.btnThemeSoft)
        val btnThemeNeon = findViewById<Button>(R.id.btnThemeNeon)

        fun refreshLanguageStatus() {
            languageStatus.text = getString(
                R.string.languages_selected_summary,
                settingsRepository.getLanguageSummary().ifBlank { getString(R.string.no_languages_selected) }
            )
            btnEnglish.isSelected = settingsRepository.getEnabledLanguages().contains("English")
            btnBangla.isSelected = settingsRepository.getEnabledLanguages().contains("Bangla")
        }

        switchSound.isChecked = settingsRepository.isSoundEnabled()
        switchVibration.isChecked = settingsRepository.isVibrationEnabled()

        btnEnglish.setOnClickListener {
            val enabled = settingsRepository.getEnabledLanguages().toMutableSet()
            if (enabled.contains("English")) {
                enabled.remove("English")
            } else {
                enabled.add("English")
            }
            if (enabled.isEmpty()) {
                enabled.add("English")
            }
            settingsRepository.setEnabledLanguages(enabled)
            settingsRepository.setCurrentLanguage("English")
            refreshLanguageStatus()
        }

        btnBangla.setOnClickListener {
            val enabled = settingsRepository.getEnabledLanguages().toMutableSet()
            if (enabled.contains("Bangla")) {
                enabled.remove("Bangla")
            } else {
                enabled.add("Bangla")
                settingsRepository.setCurrentLanguage("Bangla")
            }
            if (enabled.isEmpty()) {
                enabled.add("English")
            }
            settingsRepository.setEnabledLanguages(enabled)
            refreshLanguageStatus()
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setSoundEnabled(isChecked)
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setVibrationEnabled(isChecked)
        }

        btnThemeDark.setOnClickListener { applyThemeSelection(KeyboardTheme.DARK) }
        btnThemeSoft.setOnClickListener { applyThemeSelection(KeyboardTheme.SOFT) }
        btnThemeNeon.setOnClickListener { applyThemeSelection(KeyboardTheme.NEON) }

        refreshLanguageStatus()
        highlightSelectedTheme()
    }

    private fun applyThemeSelection(theme: KeyboardTheme) {
        settingsRepository.setKeyboardTheme(theme)
        highlightSelectedTheme()
    }

    private fun highlightSelectedTheme() {
        val btnThemeDark = findViewById<Button>(R.id.btnThemeDark)
        val btnThemeSoft = findViewById<Button>(R.id.btnThemeSoft)
        val btnThemeNeon = findViewById<Button>(R.id.btnThemeNeon)
        val selected = settingsRepository.getKeyboardTheme()

        btnThemeDark.alpha = if (selected == KeyboardTheme.DARK) 1.0f else 0.65f
        btnThemeSoft.alpha = if (selected == KeyboardTheme.SOFT) 1.0f else 0.65f
        btnThemeNeon.alpha = if (selected == KeyboardTheme.NEON) 1.0f else 0.65f
    }
}
