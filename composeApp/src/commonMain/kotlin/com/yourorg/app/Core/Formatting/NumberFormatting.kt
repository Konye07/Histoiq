package com.yourorg.app.Core.Formatting

import kotlin.math.abs
import kotlin.math.roundToLong

/** Portable "%.<decimals>f" replacement — java.lang.String.format doesn't exist on Kotlin/Native. */
fun formatFixed(value: Double, decimals: Int): String {
    val negative = value < 0
    val factor = (1..decimals).fold(1L) { acc, _ -> acc * 10 }
    val roundedTotal = (abs(value) * factor).roundToLong()
    val whole = roundedTotal / factor
    val frac = roundedTotal % factor
    val fracStr = if (decimals > 0) "." + frac.toString().padStart(decimals, '0') else ""
    return (if (negative) "-" else "") + whole.toString() + fracStr
}

/** Portable "%,d" replacement — comma-grouped thousands, e.g. 150000 -> "150,000". */
fun formatWithCommas(value: Long): String {
    val negative = value < 0
    val digits = abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-$grouped" else grouped
}

/** Portable "%02d" replacement for a single component (e.g. a countdown's hours/minutes/seconds). */
fun formatTwoDigits(value: Int): String = value.toString().padStart(2, '0')
