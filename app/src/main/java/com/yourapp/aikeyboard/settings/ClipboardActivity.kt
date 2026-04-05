package com.yourapp.aikeyboard.settings

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.yourapp.aikeyboard.R

class ClipboardActivity : BaseSettingsActivity() {
    private lateinit var listView: ListView
    private lateinit var emptyText: TextView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.clipboard_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_clipboard, contentContainer, false)
        contentContainer.addView(view)

        listView = view.findViewById(R.id.clipboardList)
        emptyText = view.findViewById(R.id.clipboardEmptyText)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        view.findViewById<Button>(R.id.buttonClearClipboard).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.clear_history_confirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    settingsRepository.clearClipboardHistory()
                    refreshList()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        refreshList()
    }

    private fun refreshList() {
        val history = settingsRepository.getClipboardHistory()
        adapter.clear()
        adapter.addAll(history)
        emptyText.text = if (history.isEmpty()) getString(R.string.clipboard_empty) else ""
    }
}
