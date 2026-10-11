package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable
import kotlin.math.abs

/**
 * A signed year (negative = BCE, per this app's data convention) with the active language's era label:
 * "1415 AD", "480 BCE", "480 a.C.". Hungarian puts the label first, "Kr. e. 480".
 */
@Composable
fun formatEraYear(year: Int): String {
    val label = if (year < 0) StringsGamesYearguessCasualties.yearguess_era_bce else StringsGamesYearguessCasualties.yearguess_era_ad
    return if (LocalAppLanguage.current == AppLanguage.HU) "$label ${abs(year)}" else "${abs(year)} $label"
}
