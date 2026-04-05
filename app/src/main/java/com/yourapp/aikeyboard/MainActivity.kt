package com.yourapp.aikeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnEnableKeyboard: Button
    private lateinit var btnSelectKeyboard: Button
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnEnableKeyboard = findViewById(R.id.btn_enable_keyboard)
        btnSelectKeyboard = findViewById(R.id.btn_select_keyboard)
        btnContinue = findViewById(R.id.btn_continue)

        btnEnableKeyboard.setOnClickListener {
            // Open keyboard settings
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
            // After returning, show next button
            btnEnableKeyboard.visibility = android.view.View.GONE
            btnSelectKeyboard.visibility = android.view.View.VISIBLE
        }

        btnSelectKeyboard.setOnClickListener {
            // Open input method picker
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            // After selecting, show continue
            btnSelectKeyboard.visibility = android.view.View.GONE
            btnContinue.visibility = android.view.View.VISIBLE
        }

        btnContinue.setOnClickListener {
            // Finish onboarding
            finish()
        }
    }
}