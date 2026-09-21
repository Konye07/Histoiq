package com.konye.historiq.Features.Play

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Formatting.formatTwoDigits
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.todayDisplay
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.GameStreak
import com.konye.historiq.Data.Quiz.StreakStore
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

private data class QuizMode(
    val key: String,
    val roleName: String,
    val title: String,
    val description: String,
    val color: Color,
    val logoAsset: String,
    val enabled: Boolean,
    val onClick: () -> Unit = {}
)

@Composable
fun PlayScreen(
    onSelectYearGuess: () -> Unit = {},
    onSelectCasualtiesMultiplier: () -> Unit = {},
    onSelectFlagColorMatch: () -> Unit = {},
    onSelectTerritoryCompare: () -> Unit = {},
    onSelectTrueFalse: () -> Unit = {},
    onSelectBlindMapBattle: () -> Unit = {}
) {
    var logos by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    var torch by remember { mutableStateOf<ImageBitmap?>(null) }
    var streaks by remember { mutableStateOf<Map<String, GameStreak>>(emptyMap()) }

    val modes = listOf(
        QuizMode(
            key = GameKeys.YEAR_GUESS,
            roleName = GameKeys.roleName(GameKeys.YEAR_GUESS),
            title = GameKeys.title(GameKeys.YEAR_GUESS),
            description = StringsHomeShared.home_desc_year_guess,
            color = GameKeys.color(GameKeys.YEAR_GUESS),
            logoAsset = "images/menulogos/01guess_year.png",
            enabled = true,
            onClick = onSelectYearGuess
        ),
        QuizMode(
            key = GameKeys.CASUALTIES_MULTIPLIER,
            roleName = GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
            title = GameKeys.title(GameKeys.CASUALTIES_MULTIPLIER),
            description = StringsHomeShared.home_desc_casualties_multiplier,
            color = GameKeys.color(GameKeys.CASUALTIES_MULTIPLIER),
            logoAsset = "images/menulogos/02casualities.png",
            enabled = true,
            onClick = onSelectCasualtiesMultiplier
        ),
        QuizMode(
            key = GameKeys.FLAG_COLOR_MATCH,
            roleName = GameKeys.roleName(GameKeys.FLAG_COLOR_MATCH),
            title = GameKeys.title(GameKeys.FLAG_COLOR_MATCH),
            description = StringsHomeShared.home_desc_flag_color_match,
            color = GameKeys.color(GameKeys.FLAG_COLOR_MATCH),
            logoAsset = "images/menulogos/03paint_flag.png",
            enabled = true,
            onClick = onSelectFlagColorMatch
        ),
        QuizMode(
            key = GameKeys.TERRITORY_COMPARE,
            roleName = GameKeys.roleName(GameKeys.TERRITORY_COMPARE),
            title = GameKeys.title(GameKeys.TERRITORY_COMPARE),
            description = StringsHomeShared.home_desc_territory_compare,
            color = GameKeys.color(GameKeys.TERRITORY_COMPARE),
            logoAsset = "images/menulogos/04peak_empire_vs.png",
            enabled = true,
            onClick = onSelectTerritoryCompare
        ),
        QuizMode(
            key = GameKeys.TRUE_FALSE,
            roleName = GameKeys.roleName(GameKeys.TRUE_FALSE),
            title = GameKeys.title(GameKeys.TRUE_FALSE),
            description = StringsHomeShared.home_desc_true_false,
            color = GameKeys.color(GameKeys.TRUE_FALSE),
            logoAsset = "images/menulogos/05hist_statements.png",
            enabled = true,
            onClick = onSelectTrueFalse
        ),
        QuizMode(
            key = GameKeys.BLIND_MAP_BATTLE,
            roleName = GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE),
            title = GameKeys.title(GameKeys.BLIND_MAP_BATTLE),
            description = StringsHomeShared.home_desc_blind_map_battle,
            color = GameKeys.color(GameKeys.BLIND_MAP_BATTLE),
            logoAsset = "images/menulogos/06battle_map.png",
            enabled = true,
            onClick = onSelectBlindMapBattle
        )
    )

    LaunchedEffect(Unit) {
        val (loadedLogos, loadedTorch) = withContext(Dispatchers.Default) {
            val loadedLogos = modes.mapNotNull { mode ->
                tryLoadImageBitmap(mode.logoAsset)?.let { mode.logoAsset to it }
            }.toMap()
            loadedLogos to tryLoadImageBitmap("images/streak_torch.png")
        }
        logos = loadedLogos
        torch = loadedTorch
    }

    LaunchedEffect(Unit) {
        streaks = runCatching { StreakStore.computeAll() }.getOrDefault(emptyMap())
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(StringsHomeShared.home_title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            NextQuizCountdown()
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modes) { mode -> QuizModeCard(mode, logos[mode.logoAsset], torch, streaks[mode.key]) }
        }
    }
}

@Composable
private fun QuizModeCard(mode: QuizMode, logo: ImageBitmap?, torch: ImageBitmap?, streak: GameStreak?) {
    var showInfo by remember { mutableStateOf(false) }
    val textColor = AppColors.Cream

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = mode.enabled) {
                Haptics.tick()
                mode.onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (mode.enabled) mode.color else mode.color.copy(alpha = 0.45f)
        )
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (streak != null && streak.days > 0) {
                StreakBadge(
                    streak = streak,
                    torch = torch,
                    textColor = textColor,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.height(110.dp), contentAlignment = Alignment.Center) {
                    if (logo != null) {
                        val aspect = logo.width.toFloat() / logo.height.toFloat()
                        Image(
                            bitmap = logo,
                            contentDescription = mode.roleName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(110.dp)
                                .aspectRatio(aspect)
                        )
                    } else {
                        CircularProgressIndicator(color = textColor, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    mode.roleName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    mode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                if (!mode.enabled) {
                    Text(
                        StringsHomeShared.home_coming_soon,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(
                onClick = { showInfo = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    painterResource(Res.drawable.ic_info),
                    contentDescription = StringsHomeShared.home_about_content_desc(mode.roleName),
                    tint = textColor
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(StringsHomeShared.home_info_dialog_title(mode.roleName, mode.title)) },
            text = { Text(mode.description) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(StringsHomeShared.home_close) }
            }
        )
    }
}

/**
 * Lit (original colors) once today's entry for this game is in, grayed out otherwise — the streak
 * count itself keeps showing the still-alive-but-at-risk value from yesterday either way, same as
 * [StreakStore]'s own playedToday/days split.
 */
@Composable
private fun StreakBadge(streak: GameStreak, torch: ImageBitmap?, textColor: Color, modifier: Modifier = Modifier) {
    val contentDesc = StringsHomeShared.home_streak_content_desc(streak.days)
    Row(
        modifier = modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics {
                contentDescription = contentDesc
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (torch != null) {
            val aspect = torch.width.toFloat() / torch.height.toFloat()
            Image(
                bitmap = torch,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (streak.playedToday) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier
                    .height(20.dp)
                    .aspectRatio(aspect)
            )
        }
        Text(
            "${streak.days}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun NextQuizCountdown() {
    var remaining by remember { mutableStateOf(DailyClock.timeUntilNextMidnight()) }

    LaunchedEffect(Unit) {
        while (true) {
            remaining = DailyClock.timeUntilNextMidnight()
            delay(1000)
        }
    }

    val totalSeconds = remaining.inWholeSeconds
    val hours = (totalSeconds / 3600).toInt()
    val minutes = ((totalSeconds % 3600) / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    Column(horizontalAlignment = Alignment.End) {
        Text(
            todayDisplay(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            StringsHomeShared.home_countdown("${formatTwoDigits(hours)}:${formatTwoDigits(minutes)}:${formatTwoDigits(seconds)}"),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
