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
    @SerialName("battle_hu") val battleHu: String? = null
) {
    /** Falls back to the English `battle` when a language's translation is missing. */
    fun localizedBattle(languageCode: String): String = when (languageCode) {
        "hu" -> battleHu ?: battle
        else -> battle
    }
}
