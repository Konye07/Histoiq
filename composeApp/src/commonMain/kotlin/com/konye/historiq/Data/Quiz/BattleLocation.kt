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
    @SerialName("battle_it") val battleIt: String? = null
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
}
