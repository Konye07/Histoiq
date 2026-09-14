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
    val difficulty: Int
)
