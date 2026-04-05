package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import com.yourapp.aikeyboard.R

class PreferencesActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.preferences_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_preferences, contentContainer, false)
        contentContainer.addView(view)

        bindSwitch(view, R.id.switchKeyPopup) { settingsRepository.setKeyPopupEnabled(it) }
        bindSwitch(view, R.id.switchVibration) { settingsRepository.setVibrationEnabled(it) }
        bindSwitch(view, R.id.switchSound) { settingsRepository.setSoundEnabled(it) }
        bindSwitch(view, R.id.switchNumberRow) { settingsRepository.setNumberRowEnabled(it) }
        bindSwitch(view, R.id.switchAutoCaps) { settingsRepository.setAutoCapitalizationEnabled(it) }

        view.findViewById<Switch>(R.id.switchKeyPopup).isChecked = settingsRepository.isKeyPopupEnabled()
        view.findViewById<Switch>(R.id.switchVibration).isChecked = settingsRepository.isVibrationEnabled()
        view.findViewById<Switch>(R.id.switchSound).isChecked = settingsRepository.isSoundEnabled()
        view.findViewById<Switch>(R.id.switchNumberRow).isChecked = settingsRepository.isNumberRowEnabled()
        view.findViewById<Switch>(R.id.switchAutoCaps).isChecked = settingsRepository.isAutoCapitalizationEnabled()
    }

    private fun bindSwitch(view: android.view.View, id: Int, action: (Boolean) -> Unit) {
        view.findViewById<Switch>(id).setOnCheckedChangeListener { _, checked -> action(checked) }
    }
}
