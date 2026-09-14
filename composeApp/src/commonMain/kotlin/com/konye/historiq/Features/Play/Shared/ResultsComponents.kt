package com.konye.historiq.Features.Play.Shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Share.shareResults
import com.konye.historiq.Core.Share.storeUrl
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_share
import org.jetbrains.compose.resources.painterResource

/**
 * The shared "score, then ranking below it" header every results screen opens with. [rankingText]
 * is expected to already be phrased (e.g. "Better than 82% of today's players").
 */
@Composable
fun ResultsHeader(
    gameKey: String,
    scoreText: String,
    isPerfect: Boolean,
    rankingText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    scoreLabel: String = StringsHomeShared.results_final_score,
    subtitle: String? = null
) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        BubbleSoundPlayer.playBubbleRise()
        Haptics.scoreReveal()
        scale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 900
                0.4f at 0
                1.25f at 650
                1f at 900
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(scoreLabel, style = MaterialTheme.typography.titleMedium)
        Text(
            scoreText,
            style = MaterialTheme.typography.displayMedium,
            color = if (isPerfect) AppColors.Success else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
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

        val appName = StringsHomeShared.home_title
        val roleName = GameKeys.roleName(gameKey)
        val gameTitle = GameKeys.title(gameKey)
        val shareChooserTitle = StringsHomeShared.results_share_chooser_title
        val shareMessage = StringsHomeShared.results_share_message(
            scoreText,
            "$roleName — $gameTitle",
            appName,
            storeUrl()
        )
        val shareFooter = StringsHomeShared.results_share_footer(appName)

        OutlinedButton(
            onClick = {
                shareResults(
                    appName = appName,
                    roleName = roleName,
                    gameTitle = gameTitle,
                    scoreLabel = scoreLabel,
                    scoreText = scoreText,
                    rankingText = rankingText,
                    accentColorArgb = accentColor.toArgb(),
                    shareMessage = shareMessage,
                    chooserTitle = shareChooserTitle,
                    footerText = shareFooter
                )
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Icon(painterResource(Res.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(StringsHomeShared.results_share, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/** Turns a rank and total player count into display copy, e.g. "Rank 1 250 of 14 253 today". */
@Composable
fun rankingText(rank: Int, totalPlayers: Int): String =
    StringsHomeShared.results_rank_of_today(formatThousands(rank.toLong()), formatThousands(totalPlayers.toLong()))

private val LabelLetterSpacing = 0.8.sp

/**
 * Cream or espresso-dark, whichever actually contrasts against [background] — unlike
 * [com.konye.historiq.Core.Theme.contrastingOn] (tuned for a *screen's* base color), the accent/
 * surfaceVariant tints these cards fill with can land close to either extreme's luminance, so
 * picking by contrast ratio rather than a fixed 0.5 threshold keeps text readable either way.
 */
private fun readableOn(background: Color): Color {
    fun contrast(fg: Color, bg: Color): Float {
        val l1 = fg.luminance() + 0.05f
        val l2 = bg.luminance() + 0.05f
        return if (l1 > l2) l1 / l2 else l2 / l1
    }
    return if (contrast(AppColors.Cream, background) >= contrast(AppColors.EspressoDark, background)) {
        AppColors.Cream
    } else {
        AppColors.EspressoDark
    }
}

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = LabelLetterSpacing),
        color = color
    )
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = LabelLetterSpacing),
                color = contentColor.copy(alpha = 0.75f)
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * A single question's result: a "you guessed" vs. "answer" card pair, a headline stat for how far
 * off (or exact) the guess landed with a percentile caption, and a histogram of everyone else's
 * guesses today for the same question. Used by both the per-question reveal (right after
 * submitting) and the end-of-round results list, for YEAR_GUESS and CASUALTIES_MULTIPLIER.
 */
@Composable
fun QuestionResultCard(
    accentColor: Color,
    guessedLabel: String,
    guessedValue: String,
    answerLabel: String,
    answerValue: String,
    landedText: String,
    landedIsExact: Boolean,
    closerThanText: String,
    distribution: List<Float>,
    correctPosition: Float?,
    guessPosition: Float?,
    minLabel: String,
    maxLabel: String,
    modifier: Modifier = Modifier,
    scoreBadgeText: String? = null
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(
                label = guessedLabel,
                value = guessedValue,
                containerColor = accentColor,
                contentColor = readableOn(accentColor),
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = answerLabel,
                value = answerValue,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = readableOn(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(StringsHomeShared.results_you_landed_label)
                scoreBadgeText?.let {
                    Surface(color = accentColor, shape = RoundedCornerShape(50)) {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = readableOn(accentColor),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                landedText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (landedIsExact) AppColors.Success else MaterialTheme.colorScheme.onBackground
            )
            Text(
                closerThanText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel(StringsHomeShared.results_how_everyone_guessed_label)
            GuessDistributionAxis(
                distribution = distribution,
                correctPosition = correctPosition,
                guessPosition = guessPosition,
                accentColor = accentColor,
                correctLabel = null,
                guessLabel = null,
                minLabel = minLabel,
                maxLabel = maxLabel
            )
        }
    }
}

/**
 * A number-line for one question: today's synthetic guess distribution as a light histogram,
 * a dashed marker for the correct answer, and a floating "You" chip for the player's own guess —
 * all as normalized [0,1] positions along the axis, since the real units (years, casualties, a
 * multiplier scale) differ per caller.
 */
@Composable
fun GuessDistributionAxis(
    distribution: List<Float>,
    correctPosition: Float?,
    guessPosition: Float?,
    accentColor: Color,
    correctLabel: String?,
    guessLabel: String?,
    modifier: Modifier = Modifier,
    minLabel: String? = null,
    maxLabel: String? = null
) {
    val barColor = accentColor.copy(alpha = 0.32f)
    val correctColor = AppColors.Success
    val chipTextColor = readableOn(accentColor)
    val trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val chartWidth = maxWidth
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Canvas(Modifier.fillMaxWidth().height(64.dp)) {
                val w = size.width
                val baselineY = size.height * 0.72f
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
                val barGap = barWidth * 0.22f
                counts.forEachIndexed { i, count ->
                    if (count == 0) return@forEachIndexed
                    val h = ((count.toFloat() / maxCount) * maxBarHeight).coerceAtLeast(3.dp.toPx())
                    val radius = (barWidth * 0.28f).coerceAtMost(h / 2f)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(i * barWidth + barGap / 2f, baselineY - h),
                        size = Size((barWidth - barGap).coerceAtLeast(1f), h),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }

                correctPosition?.let { cp ->
                    val correctX = cp.coerceIn(0f, 1f) * w
                    drawLine(
                        color = correctColor,
                        start = Offset(correctX, 0f),
                        end = Offset(correctX, size.height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                    )
                    drawCircle(correctColor, radius = 6f, center = Offset(correctX, baselineY))
                }

                guessPosition?.let { gp ->
                    val guessX = gp.coerceIn(0f, 1f) * w
                    drawLine(accentColor, Offset(guessX, 0f), Offset(guessX, size.height), strokeWidth = 4f)
                    drawCircle(accentColor, radius = 7f, center = Offset(guessX, baselineY))
                }
            }

            if (minLabel != null && maxLabel != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(minLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
                    Text(maxLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                correctLabel?.let { LegendDot(correctColor, it) }
                guessLabel?.let { LegendDot(accentColor, it) }
            }
        }

        guessPosition?.let { gp ->
            val chipWidth = 44.dp
            val targetX = chartWidth * gp.coerceIn(0f, 1f) - chipWidth / 2
            val clampedX = targetX.coerceIn(0.dp, (chartWidth - chipWidth).coerceAtLeast(0.dp))
            Surface(
                color = accentColor,
                contentColor = chipTextColor,
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopStart).offset(x = clampedX)
            ) {
                Text(
                    StringsHomeShared.results_you_chip,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
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
