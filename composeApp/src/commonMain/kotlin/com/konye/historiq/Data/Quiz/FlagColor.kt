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
    val difficulty: Int,
    @SerialName("flag_name_hu") val flagNameHu: String? = null
) {
    /** Falls back to the English `flagName` when a language's translation is missing. */
    fun localizedName(languageCode: String): String = when (languageCode) {
        "hu" -> flagNameHu ?: flagName
        else -> flagName
    }
}
