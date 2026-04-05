package com.yourapp.aikeyboard.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.yourapp.aikeyboard.R

class SettingsActivity : BaseSettingsActivity() {
    private lateinit var settingsContainer: LinearLayout
    private lateinit var languageSummaryView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings)
        setupToolbar(getString(R.string.settings_title))

        settingsContainer = findViewById(R.id.settingsContainer)
        buildRows()
    }

    override fun onResume() {
        super.onResume()
        updateLanguageSummary()
    }

    private fun buildRows() {
        addRow(
            android.R.drawable.ic_menu_manage,
            getString(R.string.languages_title),
            getString(R.string.languages_subtitle)
        ) {
            startActivity(Intent(this, LanguagesActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_preferences,
            getString(R.string.preferences_title),
            getString(R.string.preferences_subtitle)
        ) {
            startActivity(Intent(this, PreferencesActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_gallery,
            getString(R.string.theme_title),
            getString(R.string.theme_subtitle)
        ) {
            startActivity(Intent(this, ThemeActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_edit,
            getString(R.string.corrections_title),
            getString(R.string.corrections_subtitle)
        ) {
            startActivity(Intent(this, CorrectionsActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_compass,
            getString(R.string.glide_title),
            getString(R.string.glide_subtitle)
        ) {
            startActivity(Intent(this, GlideTypingActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_btn_speak_now,
            getString(R.string.voice_title),
            getString(R.string.voice_subtitle)
        ) {
            startActivity(Intent(this, VoiceTypingActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_sort_by_size,
            getString(R.string.clipboard_title),
            getString(R.string.clipboard_subtitle)
        ) {
            startActivity(Intent(this, ClipboardActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_edit,
            getString(R.string.dictionary_title),
            getString(R.string.dictionary_subtitle)
        ) {
            startActivity(Intent(this, DictionaryActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_crop,
            getString(R.string.media_title),
            getString(R.string.media_subtitle)
        ) {
            startActivity(Intent(this, EmojisActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_info_details,
            getString(R.string.privacy_title),
            getString(R.string.privacy_subtitle)
        ) {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_send,
            getString(R.string.rate_title),
            getString(R.string.rate_subtitle)
        ) {
            startActivity(Intent(this, RateUsActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_help,
            getString(R.string.help_title),
            getString(R.string.help_subtitle)
        ) {
            startActivity(Intent(this, HelpFeedbackActivity::class.java))
        }

        addRow(
            android.R.drawable.ic_menu_share,
            getString(R.string.share_title),
            getString(R.string.share_subtitle)
        ) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }

    private fun addRow(iconRes: Int, title: String, subtitle: String, action: () -> Unit) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.row_setting_item, settingsContainer, false)
        itemView.setOnClickListener { action() }
        itemView.findViewById<ImageView>(R.id.rowIcon).setImageResource(iconRes)
        itemView.findViewById<TextView>(R.id.rowTitle).text = title
        val subtitleView = itemView.findViewById<TextView>(R.id.rowSubtitle)
        subtitleView.text = subtitle
        settingsContainer.addView(itemView)

        if (title == getString(R.string.languages_title)) {
            languageSummaryView = subtitleView
            updateLanguageSummary()
        }
    }

    private fun updateLanguageSummary() {
        if (::languageSummaryView.isInitialized) {
            val selection = settingsRepository.getLanguageSummary().takeIf { it.isNotBlank() }
                ?: getString(R.string.no_languages_selected)
            languageSummaryView.text = getString(R.string.languages_selected_summary, selection)
        }
    }
}
