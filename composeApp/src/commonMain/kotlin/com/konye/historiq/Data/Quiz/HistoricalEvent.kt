package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoricalEvent(
    val id: String,
    val type: String,
    val event: String,
    @SerialName("correct_year") val correctYear: Int,
    val source: String,
    val difficulty: Int,
    @SerialName("image_asset") val imageAsset: String? = null,
    val hint: String? = null,
    val description: String? = null,
    @SerialName("event_hu") val eventHu: String? = null,
    @SerialName("hint_hu") val hintHu: String? = null,
    @SerialName("description_hu") val descriptionHu: String? = null,
    @SerialName("event_es") val eventEs: String? = null,
    @SerialName("event_fr") val eventFr: String? = null,
    @SerialName("event_de") val eventDe: String? = null,
    @SerialName("event_it") val eventIt: String? = null,
    @SerialName("hint_es") val hintEs: String? = null,
    @SerialName("hint_fr") val hintFr: String? = null,
    @SerialName("hint_de") val hintDe: String? = null,
    @SerialName("hint_it") val hintIt: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedEvent(languageCode: String): String = when (languageCode) {
        "hu" -> eventHu ?: event
        "es" -> eventEs ?: event
        "fr" -> eventFr ?: event
        "de" -> eventDe ?: event
        "it" -> eventIt ?: event
        else -> event
    }

    fun localizedHint(languageCode: String): String? = when (languageCode) {
        "hu" -> hintHu ?: hint
        "es" -> hintEs ?: hint
        "fr" -> hintFr ?: hint
        "de" -> hintDe ?: hint
        "it" -> hintIt ?: hint
        else -> hint
    }

    fun localizedDescription(languageCode: String): String? = when (languageCode) {
        "hu" -> descriptionHu ?: description
        else -> description
    }
}
