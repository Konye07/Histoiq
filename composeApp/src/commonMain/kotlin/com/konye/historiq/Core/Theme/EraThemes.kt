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
}

/**
 * Where each era's look lives: [palette] holds its colors, and its pictures sit in
 * `composeResources/files/images/eras/<era key>/` (the era keys are [QuizEra.key]):
 *  - `logo_year_guess.png`, `logo_casualties_multiplier.png` — the Annalist and Herald cards
 *  - `bg_year_guess.png`, `bg_casualties_multiplier.png`, `bg_blind_map_battle.png` — the faint line-art
 *    watermark behind each game screen, black ink on transparency like `images/backgrounds/`
 *  - `banner.png` — optional header picture at the top of the era's Play screen
 * Chancellor's card uses one picture for every era, `images/eras/06battle_map_all.png`. A picture that
 * isn't there falls back to the all-time artwork (the banner is simply left out), so an era can be filled
 * in one file at a time.
 */
object EraThemes {
    fun palette(era: QuizEra): EraPalette = when (era) {
        QuizEra.ANCIENT -> EraPalette(
            base = Color(0xFFDFBE63),
            yearGuess = Color(0xFF4A7A78),
            casualtiesMultiplier = Color(0xFF5E1C23),
            blindMapBattle = Color(0xFFA0822D)
        )
        QuizEra.MEDIEVAL -> EraPalette(
            base = Color(0xFF5D4733),
            yearGuess = Color(0xFF1C2430),
            casualtiesMultiplier = Color(0xFF461615),
            blindMapBattle = Color(0xFF332D1A)
        )
        QuizEra.EARLY_MODERN -> EraPalette(
            base = Color(0xFFA13E1E),
            yearGuess = Color(0xFF153638),
            casualtiesMultiplier = Color(0xFF831B1A),
            blindMapBattle = Color(0xFF2B160C)
        )
        QuizEra.MODERN -> EraPalette(
            base = Color(0xFF696758),
            yearGuess = Color(0xFF859493),
            casualtiesMultiplier = Color(0xFF8D614D),
            blindMapBattle = Color(0xFF24261A)
        )
        QuizEra.CONTEMPORARY -> EraPalette(
            base = Color(0xFF949789),
            yearGuess = Color(0xFF457472),
            casualtiesMultiplier = Color(0xFF53080E),
            blindMapBattle = Color(0xFF3C4629)
        )
    }

    fun bannerAsset(era: QuizEra) = "images/eras/${era.key}/banner.png"

    fun logoAsset(era: QuizEra, gameKey: String) =
        if (gameKey == GameKeys.BLIND_MAP_BATTLE) "images/eras/06battle_map_all.png" else "images/eras/${era.key}/logo_$gameKey.png"

    /** [era] null is the all-time artwork every era falls back to. */
    fun backgroundAsset(era: QuizEra?, gameKey: String) =
        if (era == null) "images/backgrounds/bg_$gameKey.png" else "images/eras/${era.key}/bg_$gameKey.png"
}
