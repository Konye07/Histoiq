package com.yourorg.app.Data.Quiz

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** All daily resets (new questions, replay locks) turn over at CET midnight. */
object DailyClock {
    private val zone = ZoneId.of("CET")

    fun todayKey(): String = LocalDate.now(zone).toString()

    /**
     * Today's date for display, e.g. "Wednesday, September 10". Built fresh on every call (not
     * cached) since `Locale.getDefault()` changes at runtime when the user switches the app's
     * language — a cached formatter would keep showing the locale active when it was first built.
     */
    fun todayDisplay(): String =
        LocalDate.now(zone).format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    /** ISO date key for N days ago (0 = today), CET-based to match the daily reset boundary. */
    fun dateKey(daysAgo: Int): String = LocalDate.now(zone).minusDays(daysAgo.toLong()).toString()

    fun timeUntilNextMidnight(): Duration {
        val now = ZonedDateTime.now(zone)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        return Duration.between(now, nextMidnight)
    }

    /** Fraction of today (CET) elapsed so far, in [0, 1] — 0 right at midnight, ~1 just before the next reset. */
    fun fractionOfDayElapsed(): Double {
        val now = ZonedDateTime.now(zone)
        val startOfDay = now.toLocalDate().atStartOfDay(zone)
        return (Duration.between(startOfDay, now).seconds.toDouble() / 86400.0).coerceIn(0.0, 1.0)
    }
}
