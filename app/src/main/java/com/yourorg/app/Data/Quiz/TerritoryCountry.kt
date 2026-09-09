package com.yourorg.app.Data.Quiz

data class TerritoryCountry(
    val id: String,
    val type: String,
    val country: String,
    val era: String,
    val areaKm2: Long,
    val source: String,
    val difficulty: Int
)
