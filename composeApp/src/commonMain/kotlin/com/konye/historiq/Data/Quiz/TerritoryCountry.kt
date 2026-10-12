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
    @SerialName("era_hu") val eraHu: String? = null,
    @SerialName("country_es") val countryEs: String? = null,
    @SerialName("country_fr") val countryFr: String? = null,
    @SerialName("country_de") val countryDe: String? = null,
    @SerialName("country_it") val countryIt: String? = null,
    @SerialName("era_es") val eraEs: String? = null,
    @SerialName("era_fr") val eraFr: String? = null,
    @SerialName("era_de") val eraDe: String? = null,
    @SerialName("era_it") val eraIt: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedCountry(languageCode: String): String = when (languageCode) {
        "hu" -> countryHu ?: country
        "es" -> countryEs ?: country
        "fr" -> countryFr ?: country
        "de" -> countryDe ?: country
        "it" -> countryIt ?: country
        else -> country
    }

    fun localizedEra(languageCode: String): String = when (languageCode) {
        "hu" -> eraHu ?: era
        "es" -> eraEs ?: era
        "fr" -> eraFr ?: era
        "de" -> eraDe ?: era
        "it" -> eraIt ?: era
        else -> era
    }
}
