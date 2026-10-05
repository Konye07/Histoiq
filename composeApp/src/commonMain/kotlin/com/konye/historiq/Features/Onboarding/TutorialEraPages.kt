package com.konye.historiq.Features.Onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsEras
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.StringsSocial
import com.konye.historiq.Core.Localization.StringsTutorial
import com.konye.historiq.Core.Theme.EraThemes
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizEra
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** The games that have an era mode, in the order the app lists them. */
private val ERA_GAMES = listOf(GameKeys.YEAR_GUESS, GameKeys.CASUALTIES_MULTIPLIER, GameKeys.BLIND_MAP_BATTLE)

private const val SHOWCASE_INTERVAL_MS = 2_400L
private const val CROSSFADE_MS = 450

/** Card pictures by era (null is All-time) and game. */
typealias EraLogos = Map<Pair<QuizEra?, String>, ImageBitmap>

/**
 * Every era's card pictures, loaded once up front so the era pages never show one popping in — least of
 * all the showcase, which swaps eras on a timer. An era without its own picture gets the all-time one.
 */
@Composable
fun rememberEraLogos(): EraLogos {
    val logos by produceState<EraLogos>(emptyMap()) {
        value = withContext(Dispatchers.Default) {
            val decoded = mutableMapOf<String, ImageBitmap?>()
            suspend fun load(path: String) = decoded.getOrPut(path) { tryLoadImageBitmap(path) }
            buildMap {
                for (era in listOf<QuizEra?>(null) + QuizEra.entries) {
                    for (game in ERA_GAMES) {
                        val own = era?.let { load(EraThemes.logoAsset(it, game)) }
                        (own ?: load(EraThemes.allTimeLogoAsset(game)))?.let { put(era to game, it) }
                    }
                }
            }
        }
    }
    return logos
}

/**
 * "Which eras are there": All-time first, then each era laid out in its own colors with its years and its
 * Annalist picture — the same colors and pictures the Play screen switches to.
 */
@Composable
fun EraListIllustration(logos: EraLogos, modifier: Modifier = Modifier) {
    val dark = LocalIsDarkTheme.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EraRow(
            label = StringsEras.label(null),
            range = StringsEras.range(null),
            background = MaterialTheme.colorScheme.surfaceVariant,
            swatches = ERA_GAMES.map { GameKeys.color(it) },
            logo = logos[null to GameKeys.YEAR_GUESS]
        )
        QuizEra.entries.forEach { era ->
            val palette = EraThemes.palette(era, dark)
            EraRow(
                label = StringsEras.label(era),
                range = StringsEras.range(era),
                background = palette.base,
                swatches = listOf(palette.yearGuess, palette.casualtiesMultiplier, palette.blindMapBattle),
                logo = logos[era to GameKeys.YEAR_GUESS]
            )
        }
    }
}

@Composable
private fun EraRow(label: String, range: String, background: Color, swatches: List<Color>, logo: ImageBitmap?) {
    val onBackground = contrastingOn(background)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .faintEdge(RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            swatches.forEach { Swatch(it, ring = onBackground.copy(alpha = 0.35f)) }
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onBackground)
            Text(range, style = MaterialTheme.typography.bodySmall, color = onBackground.dimmedOn(background, 0.8f))
        }
        if (logo != null) {
            Image(
                bitmap = logo,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(40.dp).aspectRatio(logo.width.toFloat() / logo.height)
            )
        }
    }
}

/** A hairline in the page's own text color, so a card as pale as its page (Ancient's cream) still has an edge. */
@Composable
private fun Modifier.faintEdge(shape: RoundedCornerShape): Modifier =
    border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f), shape)

@Composable
private fun Swatch(color: Color, ring: Color, size: Int = 20) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color).border(1.dp, ring, CircleShape))
}

/**
 * "What an era is": a miniature of an era's Play screen — its name and years, its three games in its colors
 * with its pictures, and what it keeps of its own — cycling through the eras while the page is [active].
 */
@Composable
fun EraShowcaseIllustration(active: Boolean, logos: EraLogos, modifier: Modifier = Modifier) {
    val eras = QuizEra.entries
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(active) {
        while (active) {
            delay(SHOWCASE_INTERVAL_MS)
            index = (index + 1) % eras.size
        }
    }
    Crossfade(targetState = eras[index], animationSpec = tween(CROSSFADE_MS), modifier = modifier.fillMaxWidth()) { era ->
        EraMiniScreen(era, logos)
    }
}

@Composable
private fun EraMiniScreen(era: QuizEra, logos: EraLogos) {
    val palette = EraThemes.palette(era, LocalIsDarkTheme.current)
    val onBase = contrastingOn(palette.base)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.base)
            .faintEdge(RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            Text(StringsEras.label(era), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = onBase)
            Text(StringsEras.range(era), style = MaterialTheme.typography.bodyMedium, color = onBase.dimmedOn(palette.base, 0.8f))
        }
        ERA_GAMES.forEach { game -> MiniGameCard(game, palette.forGame(game), logos[era to game]) }
        Text(
            listOf(StringsHomeShared.home_streak_dialog_title, StringsHomeShared.results_rank_label, StringsSocial.profile_statistics)
                .joinToString("  ·  "),
            style = MaterialTheme.typography.labelMedium,
            color = onBase,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(onBase.copy(alpha = 0.14f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MiniGameCard(gameKey: String, color: Color, logo: ImageBitmap?) {
    val onColor = contrastingOn(color)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(width = 64.dp, height = 48.dp), contentAlignment = Alignment.Center) {
            if (logo != null) {
                Image(bitmap = logo, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.height(48.dp).aspectRatio(logo.width.toFloat() / logo.height))
            }
        }
        Column {
            Text(GameKeys.roleName(gameKey), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onColor)
            Text(GameKeys.title(gameKey), style = MaterialTheme.typography.bodySmall, color = onColor.dimmedOn(color, 0.85f))
        }
    }
}

/** A pill under a game that has eras: a dot for each era, and "Also in 5 historical eras". */
@Composable
fun EraBadge(modifier: Modifier = Modifier) {
    val ring = MaterialTheme.colorScheme.outline
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, ring.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            QuizEra.entries.forEach { Swatch(EraThemes.palette(it).base, ring, size = 10) }
        }
        Text(StringsTutorial.tutorial_era_badge(QuizEra.entries.size), style = MaterialTheme.typography.labelLarge)
    }
}
