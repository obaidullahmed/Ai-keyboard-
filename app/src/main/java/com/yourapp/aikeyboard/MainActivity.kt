package com.yourapp.aikeyboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yourapp.aikeyboard.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button
    private lateinit var btnContinue: Button
    private lateinit var btnEnableKeyboard: Button
    private lateinit var btnSelectKeyboard: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var headlineText: TextView

    private val onboardingPages = listOf(
        OnboardingPage(R.string.onboarding_brand_title, R.string.onboarding_brand_description),
        OnboardingPage(R.string.onboarding_personalization_title, R.string.onboarding_personalization_description),
        OnboardingPage(R.string.onboarding_ai_assistant_title, R.string.onboarding_ai_assistant_description),
        OnboardingPage(R.string.onboarding_apps_title, R.string.onboarding_apps_description),
        OnboardingPage(R.string.onboarding_translate_title, R.string.onboarding_translate_description),
        OnboardingPage(R.string.onboarding_chat_title, R.string.onboarding_chat_description),
        OnboardingPage(R.string.onboarding_tone_title, R.string.onboarding_tone_description),
        OnboardingPage(R.string.onboarding_grammar_title, R.string.onboarding_grammar_description)
    )

    private val pagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updatePagingButtons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        dotsContainer = findViewById(R.id.dotsContainer)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)
        btnContinue = findViewById(R.id.btnContinue)
        btnEnableKeyboard = findViewById(R.id.btnEnableKeyboard)
        btnSelectKeyboard = findViewById(R.id.btnSelectKeyboard)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        headlineText = findViewById(R.id.headlineText)

        viewPager.adapter = OnboardingAdapter(onboardingPages)
        viewPager.registerOnPageChangeCallback(pagerCallback)

        setupDots()
        updatePagingButtons()

        btnSkip.setOnClickListener {
            viewPager.currentItem = onboardingPages.lastIndex
        }

        btnNext.setOnClickListener {
            val nextPage = viewPager.currentItem + 1
            if (nextPage < onboardingPages.size) {
                viewPager.currentItem = nextPage
            }
        }

        btnContinue.setOnClickListener {
            openKeyboardSettings()
        }

        btnEnableKeyboard.setOnClickListener {
            openKeyboardSettings()
        }

        btnSelectKeyboard.setOnClickListener {
            showInputMethodPicker()
        }

        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(pagerCallback)
    }

    private fun setupDots() {
        repeat(onboardingPages.size) { index ->
            val dot = View(this).apply {
                setBackgroundResource(R.drawable.shape_dot_inactive)
                val size = resources.getDimensionPixelSize(R.dimen.dot_size)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = if (index == 0) 0 else resources.getDimensionPixelSize(R.dimen.spacing_xs)
                }
            }
            dotsContainer.addView(dot)
        }
        updateDots()
    }

    private fun updateDots() {
        for (index in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(index)
            dot.setBackgroundResource(
                if (index == viewPager.currentItem) R.drawable.shape_dot_active
                else R.drawable.shape_dot_inactive
            )
        }
    }

    private fun updatePagingButtons() {
        updateDots()

        val currentPage = viewPager.currentItem
        if (currentPage == onboardingPages.lastIndex) {
            btnNext.visibility = View.GONE
            btnSkip.visibility = View.GONE
            btnContinue.visibility = View.VISIBLE
            btnEnableKeyboard.visibility = View.VISIBLE
            btnSelectKeyboard.visibility = View.VISIBLE
            btnOpenSettings.visibility = View.VISIBLE
        } else {
            btnNext.visibility = View.VISIBLE
            btnSkip.visibility = View.VISIBLE
            btnContinue.visibility = View.GONE
            btnEnableKeyboard.visibility = View.GONE
            btnSelectKeyboard.visibility = View.GONE
            btnOpenSettings.visibility = View.GONE
        }
    }

    private fun openKeyboardSettings() {
        startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun showInputMethodPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    private data class OnboardingPage(
        val titleRes: Int,
        val descriptionRes: Int
    )

    private inner class OnboardingAdapter(
        private val pages: List<OnboardingPage>
    ) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
            return OnboardingViewHolder(view)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(pages[position])
        }

        override fun getItemCount(): Int = pages.size

        inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title: TextView = itemView.findViewById(R.id.pageTitle)
            private val description: TextView = itemView.findViewById(R.id.pageDescription)

            fun bind(page: OnboardingPage) {
                title.text = getString(page.titleRes)
                description.text = getString(page.descriptionRes)
            }
        }
    }
}
