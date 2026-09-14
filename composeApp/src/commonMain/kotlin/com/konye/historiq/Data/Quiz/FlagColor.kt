package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlagColor(
    val id: String,
    val type: String,
    @SerialName("flag_name") val flagName: String,
    @SerialName("image_asset") val imageAsset: String,
    val source: String,
    val difficulty: Int
)
