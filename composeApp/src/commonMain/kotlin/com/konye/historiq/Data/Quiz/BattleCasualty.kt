package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleCasualty(
    val id: String,
    val type: String,
    val battle: String,
    @SerialName("correct_casualties") val correctCasualties: Long,
    val year: Int,
    val source: String,
    val difficulty: Int,
    val hint: String? = null,
    val description: String? = null,
    @SerialName("battle_hu") val battleHu: String? = null,
    @SerialName("hint_hu") val hintHu: String? = null,
    @SerialName("description_hu") val descriptionHu: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedBattle(languageCode: String): String = when (languageCode) {
        "hu" -> battleHu ?: battle
        else -> battle
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
