package com.example.dpdpconsent.model

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

/**
 * Configuration data parsed directly from the Backend API JSON schema.
 */
data class ConsentScreenConfig(
    val privacyNoticeUrl: String? = null,
    val mainGroups: List<MainGroup> = emptyList()
) : Serializable {

    companion object {
        fun parseFromJson(jsonString: String): ConsentScreenConfig {
            val root = JSONObject(jsonString)
            val privacyNoticeUrl = root.optString("URL", "").ifBlank { null }

            val groupsArray = root.optJSONArray("MainGroups") ?: JSONArray()
            val groupsList = mutableListOf<MainGroup>()

            for (i in 0 until groupsArray.length()) {
                val groupObj = groupsArray.getJSONObject(i)
                val isActive = groupObj.optInt("ISACTIVE", 1)
                if (isActive != 1) continue // Skip inactive groups

                val consentsArray = groupObj.optJSONArray("Consents") ?: JSONArray()
                val consentsList = mutableListOf<ConsentDetail>()

                for (j in 0 until consentsArray.length()) {
                    val consentObj = consentsArray.getJSONObject(j)
                    val consentIsActive = consentObj.optInt("CONSENTISACTIVE", 1)
                    if (consentIsActive != 1) continue // Skip inactive consents

                    consentsList.add(
                        ConsentDetail(
                            consentId = consentObj.optInt("CONSENTID", 0),
                            consentCode = consentObj.optString("CONSENTCODE", "CONS${j + 1}"),
                            consentDescription = consentObj.optString("CONSENTDESCRIPTION", ""),
                            consentIsActive = consentIsActive,
                            isCore = consentObj.optInt("ISCORE", 0),
                            consentSequence = consentObj.optInt("CONSENTSEQUENCE", j + 1),
                            defaultState = "NEUTRAL"
                        )
                    )
                }

                consentsList.sortBy { it.consentSequence }

                groupsList.add(
                    MainGroup(
                        groupId = groupObj.optInt("GROUPID", 0),
                        groupName = groupObj.optString("GROUPNAME", ""),
                        iconType = groupObj.optString("ICONTYPE", "profile"),
                        colorCode = groupObj.optString("COLORCODE", "#E8F0FE"),
                        groupSequence = groupObj.optInt("GROUPSEQUENCE", i + 1),
                        isActive = isActive,
                        consents = consentsList
                    )
                )
            }

            groupsList.sortBy { it.groupSequence }

            return ConsentScreenConfig(
                privacyNoticeUrl = privacyNoticeUrl,
                mainGroups = groupsList
            )
        }
    }
}

/**
 * Represents a Main Group from backend API JSON.
 */
data class MainGroup(
    val groupId: Int,
    val groupName: String,
    val iconType: String = "profile",
    val colorCode: String = "#E8F0FE",
    val groupSequence: Int = 0,
    val isActive: Int = 1,
    val consents: List<ConsentDetail> = emptyList()
) : Serializable

/**
 * Represents a Consent Detail from backend API JSON.
 */
data class ConsentDetail(
    val consentId: Int,
    val consentCode: String,
    val consentDescription: String,
    val consentIsActive: Int = 1,
    val isCore: Int = 0,
    val consentSequence: Int = 0,
    val defaultState: String = "NEUTRAL"
) : Serializable

/**
 * Data model for consent preferences submitted by the user.
 */
data class ConsentPreferenceSubmission(
    val submittedAt: Long = System.currentTimeMillis(),
    val preferences: Map<String, String> = emptyMap(), // Maps CONSENTCODE -> State ("NEUTRAL", "ON", "OFF")
    val jsonPayload: String = ""
) : Serializable {

    fun toJson(): String {
        if (jsonPayload.isNotBlank()) return jsonPayload
        val root = JSONObject()
        root.put("submittedAt", submittedAt)
        val prefsObj = JSONObject()
        preferences.forEach { (key, value) ->
            prefsObj.put(key, value)
        }
        root.put("preferences", prefsObj)
        return root.toString(2)
    }
}
