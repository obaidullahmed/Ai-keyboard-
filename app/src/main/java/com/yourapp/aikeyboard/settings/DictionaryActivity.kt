package com.yourapp.aikeyboard.settings

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.core.widget.doAfterTextChanged
import com.yourapp.aikeyboard.R

class DictionaryActivity : BaseSettingsActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(R.layout.activity_settings_detail)
        setupToolbar(getString(R.string.dictionary_title))

        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        val view = layoutInflater.inflate(R.layout.content_dictionary, contentContainer, false)
        contentContainer.addView(view)

        listView = view.findViewById(R.id.dictionaryList)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        view.findViewById<Button>(R.id.buttonAddWord).setOnClickListener { showAddWordDialog() }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val word = adapter.getItem(position) ?: return@setOnItemLongClickListener true
            showDeleteWordDialog(word)
            true
        }

        refreshWords()
    }

    private fun refreshWords() {
        adapter.clear()
        adapter.addAll(settingsRepository.getPersonalDictionary())
    }

    private fun showAddWordDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.dictionary_hint)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dictionary_title)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                val word = input.text.toString().trim()
                if (word.isNotEmpty()) {
                    settingsRepository.addDictionaryWord(word)
                    refreshWords()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
        input.doAfterTextChanged { dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = it?.isNotBlank() == true }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
    }

    private fun showDeleteWordDialog(word: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_word_title))
            .setMessage(getString(R.string.delete_word_message, word))
            .setPositiveButton(R.string.delete) { _, _ ->
                settingsRepository.removeDictionaryWord(word)
                refreshWords()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
