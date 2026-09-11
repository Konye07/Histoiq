package com.yourorg.app.Data.Quiz

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
    val difficulty: Int
)
