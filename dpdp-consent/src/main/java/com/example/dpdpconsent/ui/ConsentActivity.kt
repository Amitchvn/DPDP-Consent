package com.example.dpdpconsent.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dpdpconsent.R
import com.example.dpdpconsent.model.ConsentPreferenceSubmission
import com.example.dpdpconsent.model.ConsentScreenConfig
import com.example.dpdpconsent.model.MainGroup

class ConsentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCREEN_CONFIG = "extra_screen_config"
        const val EXTRA_SUBMISSION = "extra_preference_submission"
        const val TAG = "DPDPConsent"

        // Hardcoded UI strings
        const val SCREEN_TITLE = "Privacy & Data Consent"
        const val HEADER_TITLE = "Control how your personal data is used."
        const val HEADER_SUBTITLE = "Under the Digital Personal Data Protection (DPDP) Act, review each data category and its usage preferences below."
        const val SAVE_BUTTON_TEXT = "Save preferences"

        @Volatile var resultCallback: ((ConsentPreferenceSubmission) -> Unit)? = null

        fun start(
            activity: Activity,
            config: ConsentScreenConfig,
            onSaved: (ConsentPreferenceSubmission) -> Unit
        ) {
            resultCallback = onSaved
            val intent = Intent(activity, ConsentActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_CONFIG, config)
            }
            activity.startActivity(intent)
        }
    }

    private val userPreferences = mutableMapOf<String, String>()
    private var screenConfig: ConsentScreenConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for Android 15 (API 35+)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        screenConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_SCREEN_CONFIG, ConsentScreenConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_SCREEN_CONFIG) as? ConsentScreenConfig
        }

        val config = screenConfig
        if (config == null) {
            Toast.makeText(this, "Missing DPDP Screen Configuration", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize default states to "NEUTRAL"
        config.mainGroups.forEach { group ->
            if (group.consents.size == 1) {
                val consent = group.consents.first()
                userPreferences[consent.consentCode] = consent.defaultState.ifBlank { "NEUTRAL" }
            } else {
                userPreferences["GROUP_${group.groupId}"] = "NEUTRAL"
                group.consents.forEach { consent ->
                    userPreferences[consent.consentCode] = consent.defaultState.ifBlank { "NEUTRAL" }
                }
            }
        }

        buildUi(config)
    }

    private fun buildUi(config: ConsentScreenConfig) {
        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(Color.parseColor("#F8FAFC"))
        }

        // 1. Top Action Bar / Toolbar
        val actionBar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#0F67DE"))
            setPadding(dp(16), dp(16), dp(16), dp(16))
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_back_arrow)
            setOnClickListener { finish() }
            val params = LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                rightMargin = dp(16)
            }
            layoutParams = params
        }
        actionBar.addView(backButton)

        val titleView = TextView(this).apply {
            text = SCREEN_TITLE
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }
        actionBar.addView(titleView)

        rootLayout.addView(
            actionBar,
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_TOP) }
        )

        // 2. Fixed Bottom Save Button
        val bottomBar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(12), dp(16), dp(16))
            elevation = dp(8).toFloat()
        }

        val saveButton = Button(this).apply {
            text = SAVE_BUTTON_TEXT
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0F67DE"))
                cornerRadius = dp(12).toFloat()
            }
            setOnClickListener { savePreferences() }
        }
        bottomBar.addView(
            saveButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        rootLayout.addView(
            bottomBar,
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) }
        )

        // 3. Android 15 Window Insets handling
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val topInset = maxOf(systemBars.top, dp(24))
            val bottomInset = maxOf(systemBars.bottom, dp(16))

            actionBar.setPadding(dp(16), topInset + dp(12), dp(16), dp(12))
            bottomBar.setPadding(dp(16), dp(12), dp(16), bottomInset + dp(12))

            insets
        }

        // 4. Scrollable Body Content
        val scrollView = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        contentLayout.addView(createBannerHeaderCard(config))

        // Render main groups from backend JSON
        config.mainGroups.forEach { group ->
            contentLayout.addView(createMainGroupCard(group))
        }

        scrollView.addView(contentLayout)

        val scrollParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            addRule(RelativeLayout.BELOW, actionBar.id)
            addRule(RelativeLayout.ABOVE, bottomBar.id)
        }
        rootLayout.addView(scrollView, scrollParams)

        setContentView(rootLayout)
    }

    private fun createBannerHeaderCard(config: ConsentScreenConfig): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EEF4FD"))
                cornerRadius = dp(16).toFloat()
            }
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
            layoutParams = params
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            layoutParams = params
        }

        val titleTv = TextView(this).apply {
            text = HEADER_TITLE
            setTextColor(Color.parseColor("#1A1D20"))
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }
        textLayout.addView(titleTv)

        val subTv = TextView(this).apply {
            text = HEADER_SUBTITLE
            setTextColor(Color.parseColor("#5F6368"))
            textSize = 13f
            setPadding(0, dp(6), 0, 0)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        }
        textLayout.addView(subTv)

        // Inject clickable Privacy Policy URL button if provided by backend API
        config.privacyNoticeUrl?.takeIf { it.isNotBlank() }?.let { url ->
            val privacyBtn = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(6), dp(12), dp(6))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#DBEAFE"))
                    cornerRadius = dp(16).toFloat()
                }
                setOnClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Toast.makeText(this@ConsentActivity, "Cannot open browser", Toast.LENGTH_SHORT).show()
                    }
                }
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(10) }
                layoutParams = params
            }

            val privacyTv = TextView(this).apply {
                text = "View Privacy Policy"
                setTextColor(Color.parseColor("#0F67DE"))
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
            }
            privacyBtn.addView(privacyTv)

            textLayout.addView(privacyBtn)
        }

        card.addView(textLayout)

        val shieldIv = ImageView(this).apply {
            setImageResource(R.drawable.ic_shield_lock)
            val params = LinearLayout.LayoutParams(dp(56), dp(56)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dp(12)
            }
            layoutParams = params
        }
        card.addView(shieldIv)

        return card
    }

    private fun createMainGroupCard(group: MainGroup): View {
        val hasMultipleConsents = group.consents.size > 1

        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            if (hasMultipleConsents) {
                // Grouped Card Container for items with multiple sub-consents
                setPadding(dp(14), dp(14), dp(14), dp(14))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F8FAFC"))
                    setStroke(dp(1), Color.parseColor("#CBD5E1"))
                    cornerRadius = dp(14).toFloat()
                }
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
                layoutParams = params
            } else {
                // Flat Card Container for single consent items
                setPadding(dp(14), dp(14), dp(14), dp(14))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(dp(1), Color.parseColor("#E2E8F0"))
                    cornerRadius = dp(12).toFloat()
                }
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
                layoutParams = params
            }
        }

        val mainRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Left Circular Icon
        val circleBg = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            val colorHex = group.colorCode.ifBlank { getFallbackIconColor(group.iconType) }
            background = GradientDrawable().apply {
                setColor(Color.parseColor(colorHex))
                shape = GradientDrawable.OVAL
            }
            val params = LinearLayout.LayoutParams(dp(40), dp(40)).apply {
                rightMargin = dp(12)
            }
            layoutParams = params
        }

        val itemIconIv = ImageView(this).apply {
            setImageResource(getItemIconRes(group.iconType))
        }
        circleBg.addView(itemIconIv, LinearLayout.LayoutParams(dp(20), dp(20)))
        mainRow.addView(circleBg)

        // Middle Title Column
        val middleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val itemTitleTv = TextView(this).apply {
            text = group.groupName
            setTextColor(Color.parseColor("#0F172A"))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
        }
        middleColumn.addView(itemTitleTv)

        // For single consent items, show description / view details if present and not "Single Consent"
        var detailTv: LinearLayout? = null
        if (!hasMultipleConsents && group.consents.isNotEmpty()) {
            val singleConsent = group.consents.first()
            val description = singleConsent.consentDescription.trim()

            if (description.isNotBlank() && !description.equals("Single Consent", ignoreCase = true)) {
                detailTv = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    visibility = View.GONE
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#F0F7FF"))
                        cornerRadius = dp(8).toFloat()
                        setStroke(dp(1), Color.parseColor("#BAE6FD"))
                    }
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                    layoutParams = params
                }

                val detailHeaderTv = TextView(this).apply {
                    text = "DATA PROCESSING DETAILS:"
                    setTextColor(Color.parseColor("#0369A1"))
                    textSize = 10f
                    setTypeface(typeface, Typeface.BOLD)
                }
                detailTv.addView(detailHeaderTv)

                val detailBodyTv = TextView(this).apply {
                    text = formatTextToReadableBullets(description)
                    setTextColor(Color.parseColor("#334155"))
                    textSize = 12f
                    setPadding(0, dp(4), 0, 0)
                    setLineSpacing(dp(3).toFloat(), 1.0f)
                }
                detailTv.addView(detailBodyTv)

                val viewDetailsRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(6), 0, 0)
                }

                val detailsText = TextView(this).apply {
                    text = "View details"
                    setTextColor(Color.parseColor("#0F67DE"))
                    textSize = 12f
                    setTypeface(typeface, Typeface.BOLD)
                }
                viewDetailsRow.addView(detailsText)

                val chevronIv = ImageView(this).apply {
                    setImageResource(R.drawable.ic_chevron_down)
                    setPadding(dp(4), 0, 0, 0)
                }
                viewDetailsRow.addView(chevronIv, LinearLayout.LayoutParams(dp(18), dp(18)))

                viewDetailsRow.setOnClickListener {
                    if (detailTv.visibility == View.GONE) {
                        detailTv.visibility = View.VISIBLE
                        detailsText.text = "Hide details"
                        chevronIv.setImageResource(R.drawable.ic_chevron_up)
                    } else {
                        detailTv.visibility = View.GONE
                        detailsText.text = "View details"
                        chevronIv.setImageResource(R.drawable.ic_chevron_down)
                    }
                }

                middleColumn.addView(viewDetailsRow)
            }
        }

        mainRow.addView(
            middleColumn,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        )

        // Parent / Single Pill Control
        val parentKey = if (hasMultipleConsents) "GROUP_${group.groupId}" else group.consents.firstOrNull()?.consentCode ?: "KEY_${group.groupId}"
        var parentState = userPreferences[parentKey] ?: "NEUTRAL"
        val subItemUpdaters = mutableListOf<(String) -> Unit>()

        val (parentPillControl, updateParentPill) = createSegmentedPillControl(parentState) { newState ->
            parentState = newState
            userPreferences[parentKey] = newState

            if (hasMultipleConsents) {
                group.consents.forEach { consent ->
                    userPreferences[consent.consentCode] = newState
                }
                subItemUpdaters.forEach { updater -> updater(newState) }
            }
        }

        mainRow.addView(parentPillControl)
        rowLayout.addView(mainRow)
        detailTv?.let { rowLayout.addView(it) }

        // Render Child Sub-Consents for Multi-Consent Groups (Mobile No., Email, etc.)
        if (hasMultipleConsents) {
            rowLayout.addView(View(this).apply {
                setBackgroundColor(Color.parseColor("#E2E8F0"))
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {
                    topMargin = dp(12)
                    bottomMargin = dp(10)
                }
                layoutParams = params
            })

            val subGroupHeaderTv = TextView(this).apply {
                text = "GRANULAR CONSENT PURPOSES:"
                setTextColor(Color.parseColor("#0F67DE"))
                textSize = 10.5f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(2), 0, 0, dp(8))
            }
            rowLayout.addView(subGroupHeaderTv)

            val subItemsContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            val prefixLabels = listOf("A", "B", "C", "D", "E", "F", "G", "H")

            group.consents.forEachIndexed { subIndex, consent ->
                val prefixLetter = prefixLabels.getOrElse(subIndex) { "${subIndex + 1}" }

                val subCard = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        setStroke(dp(1), Color.parseColor("#CBD5E1"))
                        cornerRadius = dp(10).toFloat()
                    }
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                    layoutParams = params
                }

                val subHeaderRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val letterBadge = TextView(this).apply {
                    text = prefixLetter
                    setTextColor(Color.parseColor("#0F67DE"))
                    textSize = 12f
                    setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#EEF2FF"))
                        shape = GradientDrawable.OVAL
                    }
                    val params = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                        rightMargin = dp(10)
                    }
                    layoutParams = params
                }
                subHeaderRow.addView(letterBadge)

                val subMiddleColumn = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val subTitleTv = TextView(this).apply {
                    text = "Consent Option $prefixLetter"
                    setTextColor(Color.parseColor("#0F172A"))
                    textSize = 13.5f
                    setTypeface(typeface, Typeface.BOLD)
                }
                subMiddleColumn.addView(subTitleTv)

                // Expandable Purpose Details for Sub-Item
                var subDetailContainer: LinearLayout? = null
                if (consent.consentDescription.isNotBlank()) {
                    subDetailContainer = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(10), dp(8), dp(10), dp(8))
                        visibility = View.GONE
                        background = GradientDrawable().apply {
                            setColor(Color.parseColor("#F0F7FF"))
                            cornerRadius = dp(6).toFloat()
                            setStroke(dp(1), Color.parseColor("#BAE6FD"))
                        }
                        val params = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = dp(8) }
                        layoutParams = params
                    }

                    val purposeHeaderTv = TextView(this).apply {
                        text = "PURPOSE & USAGE:"
                        setTextColor(Color.parseColor("#0369A1"))
                        textSize = 10f
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    subDetailContainer.addView(purposeHeaderTv)

                    val subDescTv = TextView(this).apply {
                        text = formatTextToReadableBullets(consent.consentDescription)
                        setTextColor(Color.parseColor("#334155"))
                        textSize = 12f
                        setPadding(0, dp(4), 0, 0)
                        setLineSpacing(dp(3).toFloat(), 1.0f)
                    }
                    subDetailContainer.addView(subDescTv)

                    val subViewDetailsRow = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, dp(4), 0, 0)
                    }

                    val subDetailsText = TextView(this).apply {
                        text = "View details"
                        setTextColor(Color.parseColor("#0F67DE"))
                        textSize = 12f
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    subViewDetailsRow.addView(subDetailsText)

                    val subChevronIv = ImageView(this).apply {
                        setImageResource(R.drawable.ic_chevron_down)
                        setPadding(dp(4), 0, 0, 0)
                    }
                    subViewDetailsRow.addView(subChevronIv, LinearLayout.LayoutParams(dp(18), dp(18)))

                    subViewDetailsRow.setOnClickListener {
                        if (subDetailContainer.visibility == View.GONE) {
                            subDetailContainer.visibility = View.VISIBLE
                            subDetailsText.text = "Hide details"
                            subChevronIv.setImageResource(R.drawable.ic_chevron_up)
                        } else {
                            subDetailContainer.visibility = View.GONE
                            subDetailsText.text = "View details"
                            subChevronIv.setImageResource(R.drawable.ic_chevron_down)
                        }
                    }

                    subMiddleColumn.addView(subViewDetailsRow)
                }

                subHeaderRow.addView(
                    subMiddleColumn,
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
                )

                // Sub-Item 3-Segment Pill Control Widget bound to CONSENTCODE
                var subState = userPreferences[consent.consentCode] ?: "NEUTRAL"

                val (subPillControl, updateSubPill) = createSegmentedPillControl(subState) { newState ->
                    subState = newState
                    userPreferences[consent.consentCode] = newState

                    val subStates = group.consents.map { userPreferences[it.consentCode] ?: "NEUTRAL" }
                    parentState = if (subStates.all { it == "ON" }) "ON"
                    else if (subStates.all { it == "OFF" }) "OFF"
                    else "NEUTRAL"

                    userPreferences[parentKey] = parentState
                    updateParentPill(parentState)
                }

                subItemUpdaters.add { newState ->
                    subState = newState
                    updateSubPill(newState)
                }

                subHeaderRow.addView(subPillControl)
                subCard.addView(subHeaderRow)

                subDetailContainer?.let { subCard.addView(it) }

                subItemsContainer.addView(subCard)
            }

            rowLayout.addView(subItemsContainer)
        }

        return rowLayout
    }

    /**
     * Creates a Segmented Pill Control [ ON | (Pill Dot) | OFF ].
     */
    private fun createSegmentedPillControl(
        initialState: String,
        onStateChanged: (String) -> Unit
    ): Pair<LinearLayout, (String) -> Unit> {
        var currentState = initialState.uppercase()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(3), dp(3), dp(3), dp(3))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F5F9"))
                setStroke(dp(1), Color.parseColor("#CBD5E1"))
                cornerRadius = dp(20).toFloat()
            }
        }

        val btnOn = TextView(this).apply {
            text = "ON"
            textSize = 11.5f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true
            isFocusable = true
        }

        val btnNeutralContainer = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(6), dp(10), dp(6))
            isClickable = true
            isFocusable = true
        }

        val neutralPillDot = View(this).apply {
            val params = LinearLayout.LayoutParams(dp(12), dp(6))
            layoutParams = params
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#94A3B8"))
                cornerRadius = dp(3).toFloat()
            }
        }
        btnNeutralContainer.addView(neutralPillDot)

        val btnOff = TextView(this).apply {
            text = "OFF"
            textSize = 11.5f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true
            isFocusable = true
        }

        fun renderControl() {
            container.removeAllViews()

            if (currentState == "NEUTRAL") {
                container.addView(btnOn)
                container.addView(btnNeutralContainer)
                container.addView(btnOff)

                btnOn.setTextColor(Color.parseColor("#64748B"))
                btnOn.setTypeface(btnOn.typeface, Typeface.NORMAL)
                btnOn.background = null

                btnOff.setTextColor(Color.parseColor("#64748B"))
                btnOff.setTypeface(btnOff.typeface, Typeface.NORMAL)
                btnOff.background = null

                (neutralPillDot.background as? GradientDrawable)?.setColor(Color.WHITE)
                btnNeutralContainer.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#4B5563"))
                    cornerRadius = dp(16).toFloat()
                }
            } else {
                container.addView(btnOn)
                container.addView(btnOff)

                if (currentState == "ON") {
                    btnOn.setTextColor(Color.WHITE)
                    btnOn.setTypeface(btnOn.typeface, Typeface.BOLD)
                    btnOn.background = GradientDrawable().apply {
                        setColor(Color.parseColor("#10B981")) // Green
                        cornerRadius = dp(16).toFloat()
                    }

                    btnOff.setTextColor(Color.parseColor("#64748B"))
                    btnOff.setTypeface(btnOff.typeface, Typeface.NORMAL)
                    btnOff.background = null
                } else { // "OFF"
                    btnOff.setTextColor(Color.WHITE)
                    btnOff.setTypeface(btnOff.typeface, Typeface.BOLD)
                    btnOff.background = GradientDrawable().apply {
                        setColor(Color.parseColor("#EF4444")) // Red
                        cornerRadius = dp(16).toFloat()
                    }

                    btnOn.setTextColor(Color.parseColor("#64748B"))
                    btnOn.setTypeface(btnOn.typeface, Typeface.NORMAL)
                    btnOn.background = null
                }
            }
        }

        renderControl()

        btnOn.setOnClickListener {
            currentState = "ON"
            renderControl()
            onStateChanged("ON")
        }

        btnOff.setOnClickListener {
            currentState = "OFF"
            renderControl()
            onStateChanged("OFF")
        }

        btnNeutralContainer.setOnClickListener {
            currentState = "NEUTRAL"
            renderControl()
            onStateChanged("NEUTRAL")
        }

        val updateFromExternal: (String) -> Unit = { newState ->
            currentState = newState.uppercase()
            renderControl()
        }

        return Pair(container, updateFromExternal)
    }

    private fun formatTextToReadableBullets(rawText: String): String {
        val cleanText = rawText.trim()
        if (!cleanText.contains(",")) return cleanText

        val parts = cleanText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.size <= 1) return cleanText

        return parts.joinToString("\n") { part ->
            "•  " + part.removeSuffix(".").trim()
        }
    }

    private fun savePreferences() {
        val submission = ConsentPreferenceSubmission(
            submittedAt = System.currentTimeMillis(),
            preferences = userPreferences.toMap()
        )
        val jsonPayload = submission.toJson()

        Log.d(TAG, "Consent Preferences Saved JSON:\n$jsonPayload")

        val callback = resultCallback
        resultCallback = null
        callback?.invoke(submission)

        val returnIntent = Intent().apply {
            putExtra(EXTRA_SUBMISSION, submission)
        }
        setResult(RESULT_OK, returnIntent)

        Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getItemIconRes(iconType: String?): Int {
        return when (iconType?.lowercase()) {
            "profile" -> R.drawable.ic_person
            "phone", "contact" -> R.drawable.ic_phone
            "location" -> R.drawable.ic_location
            "family" -> R.drawable.ic_family
            "device" -> R.drawable.ic_device
            "biometric" -> R.drawable.ic_fingerprint
            "photo", "image" -> R.drawable.ic_image
            "bell", "notification" -> R.drawable.ic_bell
            else -> R.drawable.ic_person
        }
    }

    private fun getFallbackIconColor(iconType: String?): String {
        return when (iconType?.lowercase()) {
            "profile" -> "#E8F0FE"
            "phone", "contact" -> "#E6F4EA"
            "location" -> "#FCE8E6"
            "family" -> "#F3E8FD"
            "device" -> "#FEF7E0"
            "biometric" -> "#FCE8E6"
            "photo", "image" -> "#E8F0FE"
            "bell", "notification" -> "#E6F4EA"
            else -> "#E8F0FE"
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
