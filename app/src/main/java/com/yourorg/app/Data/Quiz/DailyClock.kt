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
    private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

    fun todayKey(): String = LocalDate.now(zone).toString()

    /** Today's date for display, e.g. "Wednesday, September 10". */
    fun todayDisplay(): String = LocalDate.now(zone).format(displayFormatter)

    /** ISO date key for N days ago (0 = today), CET-based to match the daily reset boundary. */
    fun dateKey(daysAgo: Int): String = LocalDate.now(zone).minusDays(daysAgo.toLong()).toString()

    fun timeUntilNextMidnight(): Duration {
        val now = ZonedDateTime.now(zone)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        return Duration.between(now, nextMidnight)
    }
}
