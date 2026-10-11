package com.konye.historiq.Data.Quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrueFalseStatement(
    val id: String,
    val type: String,
    val statement: String,
    @SerialName("correct_answer") val correctAnswer: Boolean,
    val source: String,
    val difficulty: Int,
    @SerialName("statement_es") val statementEs: String? = null,
    @SerialName("statement_fr") val statementFr: String? = null,
    @SerialName("statement_de") val statementDe: String? = null,
    @SerialName("statement_it") val statementIt: String? = null,
    @SerialName("statement_hu") val statementHu: String? = null
) {
    /** Falls back to the English `statement` when a language's translation is missing. */
    fun localized(languageCode: String): String = when (languageCode) {
        "es" -> statementEs ?: statement
        "fr" -> statementFr ?: statement
        "de" -> statementDe ?: statement
        "it" -> statementIt ?: statement
        "hu" -> statementHu ?: statement
        else -> statement
    }
}
