package com.yourapp.aikeyboard.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.yourapp.aikeyboard.R

class VoiceTypingActivity : BaseSettingsActivity() {
    private lateinit var statusText: TextView

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            statusText.text = getString(R.string.voice_test_started)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.voice_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_voice_typing, contentContainer, false)
        contentContainer.addView(view)

        val voiceSwitch = view.findViewById<Switch>(R.id.switchVoiceTyping)
        statusText = view.findViewById(R.id.voiceStatus)
        voiceSwitch.isChecked = settingsRepository.isVoiceTypingEnabled()
        voiceSwitch.setOnCheckedChangeListener { _, checked -> settingsRepository.setVoiceTypingEnabled(checked) }

        val testButton = view.findViewById<Button>(R.id.buttonStartVoiceTest)
        testButton.setOnClickListener { startVoiceRecognition() }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_test_button))
        }

        try {
            speechLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.voice_not_available), Toast.LENGTH_SHORT).show()
        }
    }
}
