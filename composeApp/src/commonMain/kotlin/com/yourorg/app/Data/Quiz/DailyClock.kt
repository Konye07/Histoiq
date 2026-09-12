package com.yourorg.app.Data.Quiz

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/** All daily resets (new questions, replay locks) turn over at CET midnight. */
@OptIn(ExperimentalTime::class)
object DailyClock {
    // A concrete IANA zone rather than the "CET" alias java.time accepted — resolves identically
    // across every platform's time zone database. Berlin observes the same CET/CEST rules "CET"
    // was shorthand for, so this is the same wall-clock boundary, just a portable spelling of it.
    private val zone = TimeZone.of("Europe/Berlin")

    private fun today(): LocalDate = Clock.System.now().toLocalDateTime(zone).date

    fun todayKey(): String = today().toString()

    /** ISO date key for N days ago (0 = today), CET-based to match the daily reset boundary. */
    fun dateKey(daysAgo: Int): String = today().minus(daysAgo, DateTimeUnit.DAY).toString()

    fun timeUntilNextMidnight(): Duration {
        val now = Clock.System.now()
        val nextMidnight = today().plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)
        return nextMidnight - now
    }

    /** Fraction of today (CET) elapsed so far, in [0, 1] — 0 right at midnight, ~1 just before the next reset. */
    fun fractionOfDayElapsed(): Double {
        val now = Clock.System.now()
        val startOfDay = today().atStartOfDayIn(zone)
        return ((now - startOfDay).inWholeSeconds.toDouble() / 86400.0).coerceIn(0.0, 1.0)
    }

    /** Today's date as raw, presentation-agnostic components — see [com.yourorg.app.Core.Localization.todayDisplay]
     * for the actual per-language display string built from these. */
    fun todayComponents(): DateComponents {
        val date = today()
        // .ordinal rather than a named "number"/"isoDayNumber" accessor: DayOfWeek/Month are plain
        // enums declared Monday-first / January-first, and that ordinal has stayed stable across
        // kotlinx-datetime's own churn on what to name the convenience property (monthNumber ->
        // month.number, etc. between 0.6.x and 0.7.x) — this sidesteps that entirely.
        return DateComponents(weekdayIndex = date.dayOfWeek.ordinal, monthIndex = date.month.ordinal, day = date.day)
    }
}

/** [weekdayIndex] is Monday-first (0-6), [monthIndex] is January-first (0-11). */
data class DateComponents(val weekdayIndex: Int, val monthIndex: Int, val day: Int)
