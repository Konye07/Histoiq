package com.yourorg.app.Features.Play.Shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Audio.BubbleSoundPlayer
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Localization.StringsHomeShared
import com.yourorg.app.Core.Share.shareResults
import com.yourorg.app.Core.Share.storeUrl
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.formatThousands
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_share
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
