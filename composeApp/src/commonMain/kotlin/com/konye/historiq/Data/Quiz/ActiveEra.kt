package com.konye.historiq.Data.Quiz

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The era game mode the Play tab is in — null is All-time, the default the app always opens in. Kept in
 * memory only: an era is a side of the app you step into, not a saved preference. A game screen reads it
 * once when it opens, so the round it shows never changes underneath the player.
 */
object ActiveEra {
    private val state = MutableStateFlow<QuizEra?>(null)

    val selected: StateFlow<QuizEra?> = state.asStateFlow()

    fun current(): QuizEra? = state.value

    fun select(era: QuizEra?) {
        state.value = era
    }
}
