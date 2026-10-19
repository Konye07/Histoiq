package com.konye.historiq.Features.Play.Shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Data.Quiz.BattleLeg

/**
 * What a game shows in place of its results once its round, played as one game of the Daily Battle, is over: it reports the
 * round's score ([score], on the battle's 0-1000 scale) and the battle moves on, replacing this screen at once.
 */
@Composable
fun BattleLegFinished(leg: BattleLeg, score: Double) {
    LaunchedEffect(Unit) { leg.onFinished(score) }
    HistoriqLoadingIndicator()
}
