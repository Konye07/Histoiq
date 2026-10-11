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
    @SerialName("description_hu") val descriptionHu: String? = null,
    @SerialName("battle_es") val battleEs: String? = null,
    @SerialName("battle_fr") val battleFr: String? = null,
    @SerialName("battle_de") val battleDe: String? = null,
    @SerialName("battle_it") val battleIt: String? = null,
    @SerialName("hint_es") val hintEs: String? = null,
    @SerialName("hint_fr") val hintFr: String? = null,
    @SerialName("hint_de") val hintDe: String? = null,
    @SerialName("hint_it") val hintIt: String? = null
) {
    /** Each falls back to the English text when a language's translation is missing. */
    fun localizedBattle(languageCode: String): String = when (languageCode) {
        "hu" -> battleHu ?: battle
        "es" -> battleEs ?: battle
        "fr" -> battleFr ?: battle
        "de" -> battleDe ?: battle
        "it" -> battleIt ?: battle
        else -> battle
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
