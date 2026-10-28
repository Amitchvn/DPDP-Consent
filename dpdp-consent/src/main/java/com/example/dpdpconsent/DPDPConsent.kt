package com.example.dpdpconsent

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.dpdpconsent.model.ConsentPreferenceSubmission
import com.example.dpdpconsent.model.ConsentScreenConfig
import com.example.dpdpconsent.ui.ConsentActivity

object DPDPConsent {

    /**
     * Opens the DPDP Privacy & Data Preferences screen with the given configuration object.
     */
    fun openConsentScreen(
        activity: Activity,
        config: ConsentScreenConfig,
        onSaved: (ConsentPreferenceSubmission) -> Unit
    ) {
        ConsentActivity.start(activity, config, onSaved)
    }

    /**
     * Opens the DPDP Privacy & Data Preferences screen directly from a JSON string provided by backend API.
     */
    fun openConsentScreenFromJson(
        activity: Activity,
        jsonString: String,
        onSaved: (ConsentPreferenceSubmission) -> Unit
    ) {
        val config = ConsentScreenConfig.parseFromJson(jsonString)
        openConsentScreen(activity, config, onSaved)
    }

    /**
     * Creates an [Intent] to launch [ConsentActivity] manually.
     */
    fun createFormIntent(context: Context, config: ConsentScreenConfig): Intent {
        return Intent(context, ConsentActivity::class.java).apply {
            putExtra(ConsentActivity.EXTRA_SCREEN_CONFIG, config)
        }
    }
}
