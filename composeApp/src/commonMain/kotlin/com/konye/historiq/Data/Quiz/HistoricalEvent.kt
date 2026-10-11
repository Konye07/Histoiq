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
    @SerialName("description_hu") val descriptionHu: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedEvent(languageCode: String): String = when (languageCode) {
        "hu" -> eventHu ?: event
        else -> event
    }

    fun localizedHint(languageCode: String): String? = when (languageCode) {
        "hu" -> hintHu ?: hint
        else -> hint
    }

    fun localizedDescription(languageCode: String): String? = when (languageCode) {
        "hu" -> descriptionHu ?: description
        else -> description
    }
}
