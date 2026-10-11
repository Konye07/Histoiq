package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TerritoryCountry(
    val id: String,
    val type: String,
    val country: String,
    val era: String,
    @SerialName("area_km2") val areaKm2: Long,
    val source: String,
    val difficulty: Int,
    @SerialName("country_hu") val countryHu: String? = null,
    @SerialName("era_hu") val eraHu: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedCountry(languageCode: String): String = when (languageCode) {
        "hu" -> countryHu ?: country
        else -> country
    }

    fun localizedEra(languageCode: String): String = when (languageCode) {
        "hu" -> eraHu ?: era
        else -> era
    }
}
