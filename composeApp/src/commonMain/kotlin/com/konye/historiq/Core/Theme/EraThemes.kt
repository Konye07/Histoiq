package com.konye.historiq.Core.Theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizEra
import kotlin.math.sqrt

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

    /**
     * The same era after dark: each color keeps its hue. The background becomes a deep shade of it, and the
     * game colors are brought into a dark band that sits a little above that background, so a game's card
     * still reads as a surface on it.
     */
    fun forDarkMode() = EraPalette(
        base = base.asDarkBackground(),
        yearGuess = yearGuess.asDarkSurface(),
        casualtiesMultiplier = casualtiesMultiplier.asDarkSurface(),
        blindMapBattle = blindMapBattle.asDarkSurface()
    )

    /**
     * Plain darkening would turn the eras with a pale, grayish background (Ancient, Modern, Contemporary)
     * into the same charcoal. So the era's tint is kept, and an era whose light background is lighter stays
     * a little lighter after dark — each keeps its place in the set.
     */
    private fun Color.asDarkBackground(): Color {
        val target = DARK_BASE_MIN_LUMINANCE + DARK_BASE_LUMINANCE_SPAN * sqrt(luminance())
        return darkenedToLuminance(target).withMinSaturation(DARK_BASE_MIN_SATURATION)
    }

    private fun Color.asDarkSurface() =
        darkenedToLuminance(DARK_SURFACE_MAX_LUMINANCE).brightenedToLuminance(DARK_SURFACE_MIN_LUMINANCE)

    private companion object {
        const val DARK_BASE_MIN_LUMINANCE = 0.012f
        const val DARK_BASE_LUMINANCE_SPAN = 0.028f
        const val DARK_BASE_MIN_SATURATION = 0.3f
        const val DARK_SURFACE_MIN_LUMINANCE = 0.045f
        const val DARK_SURFACE_MAX_LUMINANCE = 0.11f
    }
}

/** Darkened toward black until its luminance is at most [max]; unchanged if it already is. */
private fun Color.darkenedToLuminance(max: Float): Color {
    var amount = 0f
    var result = this
    while (result.luminance() > max && amount < 1f) {
        amount += 0.01f
        result = darken(amount)
    }
    return result
}

/**
 * Its tint made at least [minSaturation] strong (HSV saturation), with the hue and the brightest channel
 * kept: only the weaker channels are pulled down. A gray has no hue to strengthen and stays as it is.
 */
private fun Color.withMinSaturation(minSaturation: Float): Color {
    val brightest = maxOf(red, green, blue)
    val dimmest = minOf(red, green, blue)
    val spread = brightest - dimmest
    if (brightest <= 0f || spread == 0f || spread / brightest >= minSaturation) return this
    val newDimmest = brightest * (1f - minSaturation)
    fun rescaled(channel: Float) = newDimmest + (channel - dimmest) / spread * (brightest - newDimmest)
    return Color(rescaled(red), rescaled(green), rescaled(blue))
}

/** Scaled up (not blended toward white, which would wash it out) until its luminance is at least [min]. */
private fun Color.brightenedToLuminance(min: Float): Color {
    var factor = 1f
    var result = this
    while (result.luminance() < min && factor < 8f) {
        factor += 0.05f
        result = Color((red * factor).coerceAtMost(1f), (green * factor).coerceAtMost(1f), (blue * factor).coerceAtMost(1f))
    }
    return result
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
    /** [dark] is the app's dark mode: the same era in its after-dark colors, see [EraPalette.forDarkMode]. */
    fun palette(era: QuizEra, dark: Boolean = false): EraPalette = lightPalette(era).let { if (dark) it.forDarkMode() else it }

    private fun lightPalette(era: QuizEra): EraPalette = when (era) {
        QuizEra.ANCIENT -> EraPalette(
            base = Color(0xFFF2E7D6),
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

    /** The all-time card pictures — also what an era's card falls back to until the era has its own. */
    fun allTimeLogoAsset(gameKey: String): String = when (gameKey) {
        GameKeys.DAILY_BATTLE -> "images/menulogos/00the_battle.png"
        GameKeys.YEAR_GUESS -> "images/menulogos/01guess_year.png"
        GameKeys.CASUALTIES_MULTIPLIER -> "images/menulogos/02casualities.png"
        GameKeys.FLAG_COLOR_MATCH -> "images/menulogos/03paint_flag.png"
        GameKeys.TERRITORY_COMPARE -> "images/menulogos/04peak_empire_vs.png"
        GameKeys.TRUE_FALSE -> "images/menulogos/05hist_statements.png"
        GameKeys.BLIND_MAP_BATTLE -> "images/menulogos/06battle_map.png"
        else -> error("No card picture for game key $gameKey")
    }

    /** [era] null is the all-time artwork every era falls back to. */
    fun backgroundAsset(era: QuizEra?, gameKey: String) =
        if (era == null) "images/backgrounds/bg_$gameKey.png" else "images/eras/${era.key}/bg_$gameKey.png"
}
