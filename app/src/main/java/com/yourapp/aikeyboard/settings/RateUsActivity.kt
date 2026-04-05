package com.yourapp.aikeyboard.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.yourapp.aikeyboard.R

class RateUsActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.rate_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_rate_us, contentContainer, false)
        contentContainer.addView(view)

        view.findViewById<Button>(R.id.buttonRate).setOnClickListener {
            val uri = Uri.parse("market://details?id=${applicationContext.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (exception: ActivityNotFoundException) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${applicationContext.packageName}"))
                startActivity(webIntent)
            }
        }
    }
}
