package com.konye.historiq.Features.Play.Shared

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_share
import org.jetbrains.compose.resources.painterResource

/**
 * The shared "score, rank, and how you stack up" header every results screen opens with: a
 * two-column score/rank stat pair, "Better than X%" always paired with the exact rank (never
 * just one or the other), an optional today's-final-score distribution with a "You" marker, and
 * the share button. [distribution]/[yourPosition] are left empty to skip the chart entirely
 * (e.g. while it's still loading).
 */
@Composable
fun ResultsHeader(
    gameKey: String,
    scoreText: String,
    isPerfect: Boolean,
    rank: Int,
    totalPlayers: Int,
    topPercent: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    scoreLabel: String = StringsHomeShared.results_final_score,
    maxScoreText: String? = null,
    subtitle: String? = null,
    distribution: List<Float> = emptyList(),
    yourPosition: Float? = null,
    distributionMinLabel: String? = null,
    distributionMaxLabel: String? = null
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
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreStat(
                label = scoreLabel,
                value = scoreText,
                caption = maxScoreText,
                valueColor = animateColorAsState(if (isPerfect) AppColors.Success else MaterialTheme.colorScheme.onBackground).value,
                valueScale = scale.value,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(modifier = Modifier.height(56.dp))
            ScoreStat(
                label = StringsHomeShared.results_rank_label,
                value = "#" + formatThousands(rank.toLong()),
                caption = StringsHomeShared.results_of_players_total(formatThousands(totalPlayers.toLong())),
                valueColor = MaterialTheme.colorScheme.onBackground,
                valueScale = 1f,
                modifier = Modifier.weight(1f)
            )
        }

        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            StringsHomeShared.results_better_than_percent(topPercent),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp)
        )

        if (distribution.isNotEmpty()) {
            GuessDistributionAxis(
                distribution = distribution,
                correctPosition = null,
                guessPosition = yourPosition,
                accentColor = accentColor,
                correctLabel = null,
                guessLabel = null,
                minLabel = distributionMinLabel,
                maxLabel = distributionMaxLabel,
                modifier = Modifier.padding(top = 16.dp)
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
        val shareRankingText = StringsHomeShared.results_better_than_percent(topPercent)

        OutlinedButton(
            onClick = {
                shareResults(
                    appName = appName,
                    roleName = roleName,
                    gameTitle = gameTitle,
                    scoreLabel = scoreLabel,
                    scoreText = scoreText,
                    rankingText = shareRankingText,
                    accentColorArgb = accentColor.toArgb(),
                    shareMessage = shareMessage,
                    chooserTitle = shareChooserTitle,
                    footerText = shareFooter
                )
            },
            modifier = Modifier.padding(top = 12.dp),
            // The default content color (primary, i.e. the accent) sits too close to the screen's own base color to read.
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(painterResource(Res.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(StringsHomeShared.results_share, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ScoreStat(
    label: String,
    value: String,
    caption: String?,
    valueColor: Color,
    valueScale: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(
            value,
            style = MaterialTheme.typography.headlineLarge,
            color = valueColor,
            modifier = Modifier.graphicsLayer {
                scaleX = valueScale
                scaleY = valueScale
            }
        )
        caption?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.65f)
            )
        }
    }
}

private val LabelLetterSpacing = 0.8.sp

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.6f)) {
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
                color = contentColor.dimmedOn(containerColor, 0.75f)
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
                contentColor = contrastingOn(accentColor),
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = answerLabel,
                value = answerValue,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = contrastingOn(MaterialTheme.colorScheme.surfaceVariant),
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
                            color = contrastingOn(accentColor),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                landedText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = animateColorAsState(if (landedIsExact) AppColors.Success else MaterialTheme.colorScheme.onBackground).value
            )
            Text(
                closerThanText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.7f)
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
    val chipTextColor = contrastingOn(accentColor)
    val trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
    val axisLabelColor = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.55f)

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
                    Text(minLabel, style = MaterialTheme.typography.labelSmall, color = axisLabelColor)
                    Text(maxLabel, style = MaterialTheme.typography.labelSmall, color = axisLabelColor)
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
