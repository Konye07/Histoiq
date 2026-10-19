package com.konye.historiq.Features.Onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Data.Quiz.BattleLegs
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Features.Play.DailyBattle.BattleGameRow
import com.konye.historiq.Features.Play.DailyBattle.rememberGameLogos

/** What the tutorial's sample battle has scored on the questions it has answered so far (out of 1 000 each). */
private val SAMPLE_SCORES = listOf(870.0, 540.0)

/**
 * "What the Daily Battle looks like": the overview the battle opens on, with the same cards in the same colors a couple of
 * questions in. Drawn from the app's own strings, like the era pages, so it follows the language, dark mode and text size.
 */
@Composable
fun BattleOverviewIllustration(modifier: Modifier = Modifier) {
    val logos = rememberGameLogos()
    val dark = LocalIsDarkTheme.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BattleLegs.games.forEachIndexed { i, game ->
            BattleGameRow(
                number = i + 1,
                game = game,
                logo = logos[game],
                score = SAMPLE_SCORES.getOrNull(i),
                isNext = i == SAMPLE_SCORES.size,
                color = GameKeys.color(game, dark),
                compact = true
            )
        }
    }
}
