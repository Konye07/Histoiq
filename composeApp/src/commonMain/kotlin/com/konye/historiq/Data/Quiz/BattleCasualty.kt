package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleCasualty(
    val id: String,
    val type: String,
    val battle: String,
    @SerialName("correct_casualties") val correctCasualties: Long,
    val year: Int,
    val source: String,
    val difficulty: Int,
    val hint: String? = null,
    val description: String? = null
)
