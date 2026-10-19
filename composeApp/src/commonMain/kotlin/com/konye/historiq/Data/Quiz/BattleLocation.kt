package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleLocation(
    val id: String,
    val type: String,
    val battle: String,
    @SerialName("correct_lat") val correctLat: Double,
    @SerialName("correct_lng") val correctLng: Double,
    val source: String,
    val difficulty: Int,
    @SerialName("battle_hu") val battleHu: String? = null,
    @SerialName("battle_es") val battleEs: String? = null,
    @SerialName("battle_fr") val battleFr: String? = null,
    @SerialName("battle_de") val battleDe: String? = null,
    @SerialName("battle_it") val battleIt: String? = null,
    val description: String? = null,
    @SerialName("description_hu") val descriptionHu: String? = null,
    @SerialName("description_es") val descriptionEs: String? = null,
    @SerialName("description_fr") val descriptionFr: String? = null,
    @SerialName("description_de") val descriptionDe: String? = null,
    @SerialName("description_it") val descriptionIt: String? = null
) {
    /** Falls back to the English `battle` when a language's translation is missing. */
    fun localizedBattle(languageCode: String): String = when (languageCode) {
        "hu" -> battleHu ?: battle
        "es" -> battleEs ?: battle
        "fr" -> battleFr ?: battle
        "de" -> battleDe ?: battle
        "it" -> battleIt ?: battle
        else -> battle
    }

    /** The "why it matters" write-up under the answer; falls back to English, and is null for a battle that has none. */
    fun localizedDescription(languageCode: String): String? = when (languageCode) {
        "hu" -> descriptionHu ?: description
        "es" -> descriptionEs ?: description
        "fr" -> descriptionFr ?: description
        "de" -> descriptionDe ?: description
        "it" -> descriptionIt ?: description
        else -> description
    }
}
