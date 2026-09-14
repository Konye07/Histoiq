package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable
import com.konye.historiq.Data.Quiz.DailyClock

/**
 * Weekday/month names and each language's date-display word order — genuinely new content, not
 * transcribed from the old Android string XML: java.time's locale-aware DateTimeFormatter used to
 * produce these for free from the platform's own locale database, but kotlinx-datetime (needed for
 * Kotlin/Native, where java.time doesn't exist) has no such formatter, so the app now owns this
 * content directly.
 */
private object DateNames {
    val weekdaysEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val weekdaysEs = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    val weekdaysFr = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")
    val weekdaysDe = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    val weekdaysIt = listOf("lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica")

    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthsEs = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
    val monthsFr = listOf("janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre")
    val monthsDe = listOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")
    val monthsIt = listOf("gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre")
}

/** Today's date for display, e.g. "Wednesday, September 10", per the current language's own word order. */
@Composable
fun todayDisplay(): String {
    val date = DailyClock.todayComponents()
    val language = LocalAppLanguage.current

    val weekday = when (language) {
        AppLanguage.EN -> DateNames.weekdaysEn
        AppLanguage.ES -> DateNames.weekdaysEs
        AppLanguage.FR -> DateNames.weekdaysFr
        AppLanguage.DE -> DateNames.weekdaysDe
        AppLanguage.IT -> DateNames.weekdaysIt
    }[date.weekdayIndex]

    val month = when (language) {
        AppLanguage.EN -> DateNames.monthsEn
        AppLanguage.ES -> DateNames.monthsEs
        AppLanguage.FR -> DateNames.monthsFr
        AppLanguage.DE -> DateNames.monthsDe
        AppLanguage.IT -> DateNames.monthsIt
    }[date.monthIndex]

    return when (language) {
        AppLanguage.EN -> "$weekday, $month ${date.day}"
        AppLanguage.ES -> "$weekday, ${date.day} de $month"
        AppLanguage.FR -> "$weekday ${date.day} $month"
        AppLanguage.DE -> "$weekday, ${date.day}. $month"
        AppLanguage.IT -> "$weekday ${date.day} $month"
    }
}
