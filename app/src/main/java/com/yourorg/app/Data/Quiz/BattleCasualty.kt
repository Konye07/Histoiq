package com.yourorg.app.Data.Quiz

data class BattleCasualty(
    val id: String,
    val type: String,
    val battle: String,
    val correctCasualties: Long,
    val year: Int,
    val source: String,
    val difficulty: Int,
    val imageAsset: String? = null,
    val hint: String? = null
)
