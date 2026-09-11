package com.yourorg.app.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoricalEvent(
    val id: String,
    val type: String,
    val event: String,
    @SerialName("correct_year") val correctYear: Int,
    val source: String,
    val difficulty: Int,
    @SerialName("image_asset") val imageAsset: String? = null,
    val hint: String? = null
)
