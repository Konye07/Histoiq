package com.yourorg.app.Features.Play.Shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Theme.AppColors

/**
 * The shared "score, then ranking below it" header every results screen opens with. [rankingText]
 * is expected to already be phrased (e.g. "Better than 82% of today's players").
 */
@Composable
fun ResultsHeader(
    scoreText: String,
    isPerfect: Boolean,
    rankingText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    scoreLabel: String = "Final Score",
    subtitle: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(scoreLabel, style = MaterialTheme.typography.titleMedium)
        Text(
            scoreText,
            style = MaterialTheme.typography.displayMedium,
            color = if (isPerfect) AppColors.Success else MaterialTheme.colorScheme.onBackground
        )
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
        Surface(
            color = accentColor.copy(alpha = 0.22f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                rankingText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/** Turns a percentile (0-100, "beats this % of today's players") into display copy. */
fun rankingText(percentile: Int): String = when {
    percentile >= 99 -> "Top 1% today"
    percentile >= 90 -> "Top ${100 - percentile}% today"
    else -> "Better than $percentile% of today's players"
}

/**
 * A number-line for one question: today's synthetic guess distribution as a light histogram,
 * a marker for the correct answer, and a marker for the player's own guess — all as normalized
 * [0,1] positions along the axis, since the real units (years, casualties, a multiplier scale)
 * differ per caller.
 */
@Composable
fun GuessDistributionAxis(
    distribution: List<Float>,
    correctPosition: Float?,
    guessPosition: Float?,
    accentColor: Color,
    correctLabel: String?,
    guessLabel: String?,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
    val correctColor = AppColors.Success

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(56.dp)) {
            val w = size.width
            val baselineY = size.height * 0.68f
            val maxBarHeight = baselineY - 6.dp.toPx()

            drawLine(trackColor, Offset(0f, baselineY), Offset(w, baselineY), strokeWidth = 3f)

            val bins = 24
            val counts = IntArray(bins)
            distribution.forEach { p ->
                val idx = (p.coerceIn(0f, 1f) * (bins - 1)).toInt().coerceIn(0, bins - 1)
                counts[idx]++
            }
            val maxCount = (counts.maxOrNull() ?: 1).coerceAtLeast(1)
            val barWidth = w / bins
            counts.forEachIndexed { i, count ->
                if (count == 0) return@forEachIndexed
                val h = (count.toFloat() / maxCount) * maxBarHeight
                drawRect(
                    color = trackColor,
                    topLeft = Offset(i * barWidth, baselineY - h),
                    size = Size(barWidth * 0.75f, h)
                )
            }

            correctPosition?.let { cp ->
                val correctX = cp.coerceIn(0f, 1f) * w
                drawLine(correctColor, Offset(correctX, 0f), Offset(correctX, size.height), strokeWidth = 4f)
                drawCircle(correctColor, radius = 7f, center = Offset(correctX, baselineY))
            }

            guessPosition?.let { gp ->
                val guessX = gp.coerceIn(0f, 1f) * w
                drawLine(accentColor, Offset(guessX, 0f), Offset(guessX, size.height), strokeWidth = 4f)
                drawCircle(accentColor, radius = 7f, center = Offset(guessX, baselineY))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            correctLabel?.let { LegendDot(correctColor, it) }
            guessLabel?.let { LegendDot(accentColor, it) }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
