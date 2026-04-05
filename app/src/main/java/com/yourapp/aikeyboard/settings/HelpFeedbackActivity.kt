package com.yourapp.aikeyboard.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.yourapp.aikeyboard.R

class HelpFeedbackActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.help_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_help_feedback, contentContainer, false)
        contentContainer.addView(view)

        view.findViewById<TextView>(R.id.faqText).text = "${getString(R.string.help_faq_1)}\n\n${getString(R.string.help_faq_2)}\n\n${getString(R.string.help_faq_3)}"
        view.findViewById<Button>(R.id.buttonSendFeedback).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.settings_support_email)}")
                putExtra(Intent.EXTRA_SUBJECT, "AI Keyboard Feedback")
            }
            startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_button)))
        }
    }
}
