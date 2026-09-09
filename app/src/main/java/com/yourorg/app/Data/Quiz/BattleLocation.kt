package com.yourorg.app.Data.Quiz

data class BattleLocation(
    val id: String,
    val type: String,
    val battle: String,
    val correctLat: Double,
    val correctLng: Double,
    val source: String,
    val difficulty: Int
)
