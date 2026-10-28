package com.example.dpdpconsentsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dpdpconsent.DPDPConsent

class MainActivity : AppCompatActivity() {

    // Actual Backend API JSON response provided by backend
    private val backendApiJson = """
    {
      "URL": "https://example.com/privacy-policy",
      "MainGroups": [
        {
          "GROUPID": 1,
          "GROUPNAME": "Name(Full Name)",
          "ICONTYPE": "profile",
          "COLORCODE": "#E8F0FE",
          "GROUPSEQUENCE": 1,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 1,
              "CONSENTCODE": "CONS001",
              "CONSENTDESCRIPTION": "Single Consent",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 2,
          "GROUPNAME": "Mobile No.",
          "ICONTYPE": "phone",
          "COLORCODE": "#E6F4EA",
          "GROUPSEQUENCE": 2,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 2,
              "CONSENTCODE": "CONS002",
              "CONSENTDESCRIPTION": "Partial Consent for Authentication OTP",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            },
            {
              "CONSENTID": 3,
              "CONSENTCODE": "CONS003",
              "CONSENTDESCRIPTION": "Partial Consent for Transactional SMS/WhatsApp and Calls",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 2
            },
            {
              "CONSENTID": 4,
              "CONSENTCODE": "CONS004",
              "CONSENTDESCRIPTION": "Partial Consent for Revoked For Promotional/Marketing SMS/WhatsApp",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 3
            }
          ]
        },
        {
          "GROUPID": 3,
          "GROUPNAME": "Address and Location Co-ordinates",
          "ICONTYPE": "location",
          "COLORCODE": "#FCE8E6",
          "GROUPSEQUENCE": 3,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 5,
              "CONSENTCODE": "CONS005",
              "CONSENTDESCRIPTION": "Single Consent",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 4,
          "GROUPNAME": "Emial ID",
          "ICONTYPE": "bell",
          "COLORCODE": "#E6F4EA",
          "GROUPSEQUENCE": 4,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 6,
              "CONSENTCODE": "CONS006",
              "CONSENTDESCRIPTION": "Consent For Transactional Emails",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            },
            {
              "CONSENTID": 7,
              "CONSENTCODE": "CONS007",
              "CONSENTDESCRIPTION": "Consent For Promotional Emails",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 2
            }
          ]
        },
        {
          "GROUPID": 5,
          "GROUPNAME": "PAN",
          "ICONTYPE": "device",
          "COLORCODE": "#FEF7E0",
          "GROUPSEQUENCE": 5,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 8,
              "CONSENTCODE": "CONS008",
              "CONSENTDESCRIPTION": "Income Tax ,TDS and TCS compliances",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 6,
          "GROUPNAME": "DMI Photo",
          "ICONTYPE": "photo",
          "COLORCODE": "#E8F0FE",
          "GROUPSEQUENCE": 6,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 9,
              "CONSENTCODE": "CONS009",
              "CONSENTDESCRIPTION": "Person Identification",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 7,
          "GROUPNAME": "DMI Aadhar, Voter ID, Driving License",
          "ICONTYPE": "profile",
          "COLORCODE": "#E8F0FE",
          "GROUPSEQUENCE": 7,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 10,
              "CONSENTCODE": "CONS010",
              "CONSENTDESCRIPTION": "Person Identity verification, Insurances Activation",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 8,
          "GROUPNAME": "Date Of Birth",
          "ICONTYPE": "profile",
          "COLORCODE": "#E8F0FE",
          "GROUPSEQUENCE": 8,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 11,
              "CONSENTCODE": "CONS011",
              "CONSENTDESCRIPTION": "Used in the loyalty program to allocate special birthday bonuses(If applicable) and to calculate age for insurance activation",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 9,
          "GROUPNAME": "Spouse Name,DOB,AadharCard,Voter ID,Driving License",
          "ICONTYPE": "family",
          "COLORCODE": "#F3E8FD",
          "GROUPSEQUENCE": 9,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 12,
              "CONSENTCODE": "CONS012",
              "CONSENTDESCRIPTION": "Personal Identification and Family Group Insurance Activation ( DMI provides this information as the legal guardian of spouse.)",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 10,
          "GROUPNAME": "Child1 Name,DOB,Aadhar, Voter ID, Driving License, Birth Certificate",
          "ICONTYPE": "family",
          "COLORCODE": "#F3E8FD",
          "GROUPSEQUENCE": 10,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 13,
              "CONSENTCODE": "CONS013",
              "CONSENTDESCRIPTION": "Person Identification and Family group Insurances Activation (DMI provides this information as the legal guardian of child)",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 11,
          "GROUPNAME": "Child2 Name,DOB,Aadhar, Voter ID, Driving License, Birth Certificate",
          "ICONTYPE": "family",
          "COLORCODE": "#F3E8FD",
          "GROUPSEQUENCE": 11,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 14,
              "CONSENTCODE": "CONS014",
              "CONSENTDESCRIPTION": "Person Identification and Family group Insurances Activation (DMI provides this information as the legal guardian of child)",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        },
        {
          "GROUPID": 12,
          "GROUPNAME": "GSTN",
          "ICONTYPE": "device",
          "COLORCODE": "#FEF7E0",
          "GROUPSEQUENCE": 12,
          "ISACTIVE": 1,
          "Consents": [
            {
              "CONSENTID": 15,
              "CONSENTCODE": "CONS015",
              "CONSENTDESCRIPTION": "Shop Identyfication,Business Supply",
              "CONSENTISACTIVE": 1,
              "ISCORE": 0,
              "CONSENTSEQUENCE": 1
            }
          ]
        }
      ]
    }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val titleText = TextView(this).apply {
            text = "DPDP Consent Kit Sample"
            textSize = 22f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(titleText)

        val openFormButton = Button(this).apply {
            text = "Open Privacy & Data Consent"
            setOnClickListener {
                // Open consent module screen passing backend JSON response
                DPDPConsent.openConsentScreenFromJson(
                    activity = this@MainActivity,
                    jsonString = backendApiJson
                ) { submission ->
                    val jsonPayload = submission.toJson()

                    // 1. Log JSON output
                    Log.d("DPDPConsentSample", "Submitted Backend API Consent JSON:\n$jsonPayload")
                    Toast.makeText(this@MainActivity, "Consent Preferences Saved! Redirecting...", Toast.LENGTH_SHORT).show()

                    // 2. Redirect to next screen (SuccessActivity) with JSON payload
                    val nextScreenIntent = Intent(this@MainActivity, SuccessActivity::class.java).apply {
                        putExtra(SuccessActivity.EXTRA_JSON_DATA, jsonPayload)
                    }
                    startActivity(nextScreenIntent)
                }
            }
        }
        layout.addView(openFormButton)

        setContentView(layout)
    }
}
