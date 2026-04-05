package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.yourapp.aikeyboard.R

class LanguagesActivity : BaseSettingsActivity() {
    private val languageOptions = listOf("English", "Spanish", "French", "German", "Portuguese", "Chinese")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.languages_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val header = layoutInflater.inflate(R.layout.content_languages, contentContainer, false)
        contentContainer.addView(header)

        val languageList = header.findViewById<ListView>(R.id.languageList)
        val summaryText = header.findViewById<TextView>(R.id.languageSummary)
        summaryText.text = getString(R.string.language_picker_summary)

        languageList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, languageOptions)
        languageList.adapter = adapter

        val selectedLanguages = settingsRepository.getEnabledLanguages()
        languageOptions.forEachIndexed { index, item ->
            languageList.setItemChecked(index, selectedLanguages.contains(item))
        }

        languageList.setOnItemClickListener { _, _, position, _ ->
            val updatedSelection = languageOptions.filterIndexed { index, _ -> languageList.isItemChecked(index) }.toSet()
            if (updatedSelection.isEmpty()) {
                settingsRepository.setEnabledLanguages(setOf("English"))
                languageOptions.forEachIndexed { index, _ ->
                    languageList.setItemChecked(index, languageOptions[index] == "English")
                }
            } else {
                settingsRepository.setEnabledLanguages(updatedSelection)
            }
        }
    }
}
