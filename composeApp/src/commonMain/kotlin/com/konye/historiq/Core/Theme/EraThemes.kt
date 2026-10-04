package com.konye.historiq.Core.Theme

import androidx.compose.ui.graphics.Color
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizEra

/**
 * An era's own look. [base] is the era's Play screen and bottom bar; each of its three games gets a color
 * of its own, used for the game's card on that screen and as the game screen's background — all built
 * through [gameColorScheme], like every other screen in the app.
 */
class EraPalette(
    val base: Color,
    val yearGuess: Color,
    val casualtiesMultiplier: Color,
    val blindMapBattle: Color
) {
    fun forGame(gameKey: String): Color = when (gameKey) {
        GameKeys.YEAR_GUESS -> yearGuess
        GameKeys.CASUALTIES_MULTIPLIER -> casualtiesMultiplier
        GameKeys.BLIND_MAP_BATTLE -> blindMapBattle
        else -> base
    }

    companion object {
        /** Three tonal variants of one hue — a stand-in until an era is given a palette of its own. */
        fun shadesOf(base: Color) = EraPalette(
            base = base,
            yearGuess = base.darken(0.25f),
            casualtiesMultiplier = base.lighten(0.22f),
            blindMapBattle = base.darken(0.45f)
        )
    }
}

/**
 * Where each era's look lives: edit [palette] to give an era its colors, and drop its pictures into
 * `composeResources/files/images/eras/<era key>/` (the era keys are [QuizEra.key]):
 *  - `banner.png` — optional header picture at the top of the era's Play screen
 *  - `logo_year_guess.png`, `logo_casualties_multiplier.png`, `logo_blind_map_battle.png` — the three cards
 *  - `bg_year_guess.png`, `bg_casualties_multiplier.png`, `bg_blind_map_battle.png` — the faint line-art
 *    watermark behind each game screen, same transparent-PNG style as `images/backgrounds/`
 * A picture that isn't there yet falls back to the all-time artwork (the banner is simply left out), so an
 * era can be filled in one file at a time.
 */
object EraThemes {
    // Placeholder palettes: shades of existing AppColors until the real ones arrive.
    fun palette(era: QuizEra): EraPalette = when (era) {
        QuizEra.ANCIENT -> EraPalette.shadesOf(AppColors.Tan)
        QuizEra.MEDIEVAL -> EraPalette.shadesOf(AppColors.Rust)
        QuizEra.EARLY_MODERN -> EraPalette.shadesOf(AppColors.Mauve)
        QuizEra.MODERN -> EraPalette.shadesOf(AppColors.SlateBlue)
        QuizEra.CONTEMPORARY -> EraPalette.shadesOf(AppColors.OliveDark)
    }

    fun bannerAsset(era: QuizEra) = "images/eras/${era.key}/banner.png"

    fun logoAsset(era: QuizEra, gameKey: String) = "images/eras/${era.key}/logo_$gameKey.png"

    /** [era] null is the all-time artwork every era falls back to. */
    fun backgroundAsset(era: QuizEra?, gameKey: String) =
        if (era == null) "images/backgrounds/bg_$gameKey.png" else "images/eras/${era.key}/bg_$gameKey.png"
}
