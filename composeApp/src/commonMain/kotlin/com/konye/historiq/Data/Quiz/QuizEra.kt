package com.konye.historiq.Data.Quiz

/**
 * A historical-era filter for the games whose question banks carry a year ([GameKeys.ERA_FILTERABLE]).
 * [key] is the suffix that turns a game key into an era mode key ("year_guess:ancient"); these strings,
 * and the year boundaries behind them, are defined server-side by the era_for_year Postgres function.
 */
enum class QuizEra(val key: String) {
    ANCIENT("ancient"),
    MEDIEVAL("medieval"),
    EARLY_MODERN("early_modern"),
    MODERN("modern"),
    CONTEMPORARY("contemporary");

    companion object {
        fun fromKey(key: String?): QuizEra? = entries.firstOrNull { it.key == key }
    }
}
