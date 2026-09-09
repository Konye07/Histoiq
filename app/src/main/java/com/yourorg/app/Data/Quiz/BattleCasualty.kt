package com.yourorg.app.Data.Quiz

data class BattleCasualty(
    val id: String,
    val type: String,
    val battle: String,
    val correctCasualties: Long,
    val source: String,
    val difficulty: Int,
    val imageAsset: String? = null
)
