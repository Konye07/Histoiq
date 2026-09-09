package com.yourorg.app.Data.Quiz

data class TrueFalseStatement(
    val id: String,
    val type: String,
    val statement: String,
    val correctAnswer: Boolean,
    val source: String,
    val difficulty: Int
)
