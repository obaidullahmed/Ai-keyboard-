package com.yourapp.aikeyboard.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.yourapp.aikeyboard.R

class AboutActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.about_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_about, contentContainer, false)
        contentContainer.addView(view)

        val versionText = view.findViewById<TextView>(R.id.aboutVersion)
        val descriptionText = view.findViewById<TextView>(R.id.aboutDescription)

        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (exception: PackageManager.NameNotFoundException) {
            "1.0"
        }
        versionText.text = getString(R.string.about_app_version, version)
        descriptionText.text = getString(R.string.subtitle)
    }
}
