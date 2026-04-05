package com.yourapp.aikeyboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.yourapp.aikeyboard.R

abstract class BaseSettingsActivity : AppCompatActivity() {
    protected lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(this)
    }

    protected fun setupToolbar(titleText: String) {
        setTheme(R.style.Theme_Settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = titleText
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
