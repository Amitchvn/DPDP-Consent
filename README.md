# 🛡️ DPDPConsentKit

A robust, lightweight, and backend-driven Android library module designed to implement user privacy and consent management in compliance with India's **Digital Personal Data Protection (DPDP) Act, 2023**.

`DPDPConsentKit` enables Android applications to dynamically render granular data processing consent preferences directly from a backend JSON configuration. Users can explicitly grant (`ON`), revoke (`OFF`), or defer (`NEUTRAL`) consent per data category and usage purpose.

---

## ✨ Features

- **📱 Backend-Driven Dynamic UI**: Automatically builds consent screens directly from your API's JSON response — modify consent items without pushing app updates.
- **🎛️ 3-State Segmented Control Widget**: Built-in **`ON`** (Granted), **`NEUTRAL`** (Default / Unset), and **`OFF`** (Revoked) toggle switch for every consent item.
- **📂 Hierarchical Grouping**: Supports single-consent categories (e.g., PAN, Location) as well as multi-consent categories (e.g., Mobile No. with separate OTP, Transactional, and Marketing toggles).
- **🔄 Smart State Synchronization**: Toggling a parent group auto-cascades to all sub-consents, while individual sub-consent toggles update parent category status.
- **📖 Bulleted Purpose Descriptions**: Converts raw legal text into scannable bullet points with collapsible *"View details"* sections.
- **🔗 Privacy Notice Integration**: Embedded header card displaying privacy notice text with a direct clickable link to your app's Privacy Policy URL.
- **📱 Android 15 & Edge-to-Edge Ready**: Built with `WindowInsetsCompat` for proper status bar and navigation bar insets across Android versions (compiled with SDK 36, minSdk 26).
- **🚀 Zero XML Layout Dependency**: Clean, performant programmatic UI construction without XML layout inflation overhead.
- **📦 Simple Clean API**: Returns a structured `ConsentPreferenceSubmission` object containing timestamp, preference map (`CONSENTCODE` -> State), and formatted JSON payload.

---

## 🏗️ Project Architecture

```
DPDPConsentKit/
├── dpdp-consent/          # 📦 Reusable Android Library Module (com.example.dpdpconsent)
│   └── src/main/java/com/example/dpdpconsent/
│       ├── DPDPConsent.kt                    # Main Entry Point API
│       ├── model/
│       │   └── ConsentModels.kt              # Data Models & JSON Parser
│       └── ui/
│           └── ConsentActivity.kt            # Programmatic Dynamic UI & 3-State Controls
└── sample-app/            # 🚀 Sample Application Module
    └── src/main/java/com/example/dpdpconsentsample/
        ├── MainActivity.kt                    # Demo launcher & backend JSON ingestion
        └── SuccessActivity.kt                 # Consent payload listener & JSON viewer
```

---

## 📋 Requirements

- **Android Studio**: Ladybug / 2024.2.1+
- **JDK**: 17 or higher
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Compile / Target SDK**: 36 (Android 15)
- **Kotlin**: 1.9+

---

## ⚙️ Installation & Setup

### 1. Add Library Module to Project

Copy the `dpdp-consent` folder into your Android project root directory and include it in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
include(":dpdp-consent")
```

### 2. Add Dependency in Module `build.gradle.kts`

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":dpdp-consent"))

    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.16.0")
}
```

---

## 🔌 API Usage

### Quick Start (Launch directly from Backend JSON)

Pass your backend API JSON response string directly into `openConsentScreenFromJson`:

```kotlin
import com.example.dpdpconsent.DPDPConsent

DPDPConsent.openConsentScreenFromJson(
    activity = this,
    jsonString = backendJsonResponseString
) { submission ->
    // Access formatted JSON payload or raw preferences map
    val jsonPayload = submission.toJson()
    val preferencesMap = submission.preferences // e.g., {"CONS001": "ON", "CONS002": "OFF"}
    val timestamp = submission.submittedAt

    // Submit user consent preferences to your backend API
    sendPreferencesToBackend(jsonPayload)
}
```

### Advanced (Pre-parsed Configuration)

To parse the JSON beforehand or build/modify configurations programmatically:

```kotlin
import com.example.dpdpconsent.DPDPConsent
import com.example.dpdpconsent.model.ConsentScreenConfig

// Parse configuration object
val config = ConsentScreenConfig.parseFromJson(jsonString)

// Launch consent activity
DPDPConsent.openConsentScreen(
    activity = this,
    config = config
) { submission ->
    Log.d("DPDP", "Consent saved: ${submission.toJson()}")
}
```

---

## 🌐 Backend API JSON Contract

The consent form is rendered dynamically based on the JSON structure returned by your backend API:

```json
{
  "URL": "https://example.com/privacy-policy",
  "MainGroups": [
    {
      "GROUPID": 1,
      "GROUPNAME": "Mobile No.",
      "ICONTYPE": "phone",
      "COLORCODE": "#E6F4EA",
      "GROUPSEQUENCE": 1,
      "ISACTIVE": 1,
      "Consents": [
        {
          "CONSENTID": 101,
          "CONSENTCODE": "CONS001",
          "CONSENTDESCRIPTION": "Partial Consent for Authentication OTP",
          "CONSENTISACTIVE": 1,
          "ISCORE": 0,
          "CONSENTSEQUENCE": 1
        },
        {
          "CONSENTID": 102,
          "CONSENTCODE": "CONS002",
          "CONSENTDESCRIPTION": "Partial Consent for Transactional SMS/WhatsApp and Calls",
          "CONSENTISACTIVE": 1,
          "ISCORE": 0,
          "CONSENTSEQUENCE": 2
        }
      ]
    },
    {
      "GROUPID": 2,
      "GROUPNAME": "PAN",
      "ICONTYPE": "device",
      "COLORCODE": "#FEF7E0",
      "GROUPSEQUENCE": 2,
      "ISACTIVE": 1,
      "Consents": [
        {
          "CONSENTID": 103,
          "CONSENTCODE": "CONS003",
          "CONSENTDESCRIPTION": "Income Tax, TDS and TCS compliances",
          "CONSENTISACTIVE": 1,
          "ISCORE": 0,
          "CONSENTSEQUENCE": 1
        }
      ]
    }
  ]
}
```

### JSON Schema Reference

| Object | Field | Type | Description |
| :--- | :--- | :--- | :--- |
| **Root** | `URL` | String | *(Optional)* URL for the Privacy Policy web page. |
| **MainGroups** | `GROUPID` | Int | Unique identifier for the data category group. |
| | `GROUPNAME` | String | Data category title (e.g., "Mobile No.", "Location"). |
| | `ICONTYPE` | String | Category icon indicator (`profile`, `phone`, `location`, `family`, `device`, `bell`, `photo`, `biometric`). |
| | `COLORCODE` | String | Hex color string for icon background circle (e.g., `#E8F0FE`). |
| | `GROUPSEQUENCE`| Int | Display order of the category group. |
| | `ISACTIVE` | Int | `1` to render group, `0` to ignore/skip. |
| | `Consents` | Array | List of granular consent items for this group. |
| **Consents** | `CONSENTID` | Int | Unique numerical ID for the consent item. |
| | `CONSENTCODE` | String | Unique string code recorded in submission payload (e.g., `CONS001`). |
| | `CONSENTDESCRIPTION`| String | Detailed purpose statement. Automatically split by commas into bullet points. |
| | `CONSENTISACTIVE` | Int | `1` to display item, `0` to hide. |
| | `ISCORE` | Int | `1` if core mandatory consent, `0` if optional. |
| | `CONSENTSEQUENCE`| Int | Sorting order of consent options within the group. |

---

## 📤 Submission Payload Format

When the user taps **"Save preferences"**, the callback receives a `ConsentPreferenceSubmission` object containing the timestamp and a key-value mapping of each `CONSENTCODE` to its selected state (`ON`, `OFF`, or `NEUTRAL`).

### Formatted JSON Output Example

```json
{
  "submittedAt": 1741523400000,
  "preferences": {
    "CONS001": "ON",
    "CONS002": "ON",
    "CONS003": "OFF",
    "CONS004": "NEUTRAL",
    "CONS005": "ON"
  }
}
```

---

## 🎨 Supported Icons & Category Colors

The library supports predefined icon types with fallback color themes:

| Icon Key (`ICONTYPE`) | Default Color | Category Usage |
| :--- | :--- | :--- |
| `profile` | `#E8F0FE` | Full Name, DOB, Identity Documents |
| `phone` / `contact` | `#E6F4EA` | Mobile Number, Calls, SMS |
| `location` | `#FCE8E6` | Address & Location Coordinates |
| `family` | `#F3E8FD` | Spouse & Children Information |
| `device` | `#FEF7E0` | PAN, GSTN, Hardware Identifiers |
| `bell` / `notification` | `#E6F4EA` | Email ID & Notification Preferences |
| `photo` / `image` | `#E8F0FE` | Photos, Profile Picture, Media |
| `biometric` | `#FCE8E6` | Fingerprint & Face Verification |

---

## 🛡️ DPDP Act Compliance Checklist

> [!NOTE]
> `DPDPConsentKit` provides the client-side UI and payload schema for compliance with India's **Digital Personal Data Protection (DPDP) Act, 2023**. Always have your organisation's legal team validate notice text and backend consent logging.

- [x] **Clear & Accessible Notice**: Displays privacy notice text with clickable links to full privacy terms.
- [x] **Itemized & Granular Choice**: Categorizes data types and allows granular opt-in/opt-out for distinct purposes.
- [x] **Explicit Affirmative Action**: 3-State control requires active selection (`ON` / `OFF`) rather than pre-selected defaults.
- [x] **Right to Revoke**: Users can switch preferences to `OFF` at any time to record consent withdrawal.
- [x] **Audit Trail Ready**: Captures precise submission timestamps and itemized consent status mappings for server logs.

---

## 📄 License

```
Copyright 2026 DPDPConsentKit

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
