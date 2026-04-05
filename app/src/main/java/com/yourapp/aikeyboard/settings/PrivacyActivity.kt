package com.yourapp.aikeyboard.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.yourapp.aikeyboard.R

class PrivacyActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.privacy_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_privacy, contentContainer, false)
        contentContainer.addView(view)

        view.findViewById<TextView>(R.id.privacySummary).text = getString(R.string.privacy_summary)
        view.findViewById<TextView>(R.id.privacyDetails).text = getString(R.string.privacy_details)
    }
}
