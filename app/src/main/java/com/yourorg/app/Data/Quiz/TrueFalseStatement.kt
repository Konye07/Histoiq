package com.yourorg.app.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrueFalseStatement(
    val id: String,
    val type: String,
    val statement: String,
    @SerialName("correct_answer") val correctAnswer: Boolean,
    val source: String,
    val difficulty: Int
)
