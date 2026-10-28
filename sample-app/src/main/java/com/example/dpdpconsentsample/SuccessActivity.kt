package com.example.dpdpconsentsample

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class SuccessActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_JSON_DATA = "extra_json_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for Android 15 (API 35+) window insets compatibility
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        // Apply Android 15 Window Insets so top status bar / bottom gesture bar do not overlap content
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            layout.setPadding(48, systemBars.top + 32, 48, systemBars.bottom + 32)
            insets
        }

        val headerTv = TextView(this).apply {
            text = "Redirected to Next Screen!"
            textSize = 22f
            setTextColor(Color.parseColor("#1A1D20"))
            setPadding(0, 0, 0, 16)
        }
        layout.addView(headerTv)

        val subTv = TextView(this).apply {
            text = "Below is the submitted DPDP Consent JSON payload logged and passed to this screen:"
            textSize = 14f
            setTextColor(Color.parseColor("#5F6368"))
            setPadding(0, 0, 0, 24)
        }
        layout.addView(subTv)

        val jsonString = intent.getStringExtra(EXTRA_JSON_DATA) ?: "{}"

        val jsonTv = TextView(this).apply {
            text = jsonString
            textSize = 13f
            setTextColor(Color.parseColor("#202124"))
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.parseColor("#F0F4F8"))
        }
        layout.addView(jsonTv)

        val closeBtn = Button(this).apply {
            text = "Back to Home"
            setOnClickListener { finish() }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
            layoutParams = params
        }
        layout.addView(closeBtn)

        scroll.addView(layout)
        setContentView(scroll)
    }
}
