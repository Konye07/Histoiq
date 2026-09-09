package com.yourorg.app.Data.Quiz

data class HistoricalEvent(
    val id: String,
    val type: String,
    val event: String,
    val correctYear: Int,
    val source: String,
    val difficulty: Int,
    val imageAsset: String? = null
)
