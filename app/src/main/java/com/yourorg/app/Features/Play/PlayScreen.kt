package com.yourorg.app.Features.Play

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private data class QuizMode(
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
    val context = LocalContext.current
    var logos by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }

    val modes = listOf(
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.YEAR_GUESS),
            title = GameKeys.title(GameKeys.YEAR_GUESS),
            description = stringResource(R.string.home_desc_year_guess),
            color = GameKeys.color(GameKeys.YEAR_GUESS),
            logoAsset = "images/menulogos/01guess_year.png",
            enabled = true,
            onClick = onSelectYearGuess
        ),
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
            title = GameKeys.title(GameKeys.CASUALTIES_MULTIPLIER),
            description = stringResource(R.string.home_desc_casualties_multiplier),
            color = GameKeys.color(GameKeys.CASUALTIES_MULTIPLIER),
            logoAsset = "images/menulogos/02casualities.png",
            enabled = true,
            onClick = onSelectCasualtiesMultiplier
        ),
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.FLAG_COLOR_MATCH),
            title = GameKeys.title(GameKeys.FLAG_COLOR_MATCH),
            description = stringResource(R.string.home_desc_flag_color_match),
            color = GameKeys.color(GameKeys.FLAG_COLOR_MATCH),
            logoAsset = "images/menulogos/03paint_flag.png",
            enabled = true,
            onClick = onSelectFlagColorMatch
        ),
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.TERRITORY_COMPARE),
            title = GameKeys.title(GameKeys.TERRITORY_COMPARE),
            description = stringResource(R.string.home_desc_territory_compare),
            color = GameKeys.color(GameKeys.TERRITORY_COMPARE),
            logoAsset = "images/menulogos/04peak_empire_vs.png",
            enabled = true,
            onClick = onSelectTerritoryCompare
        ),
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.TRUE_FALSE),
            title = GameKeys.title(GameKeys.TRUE_FALSE),
            description = stringResource(R.string.home_desc_true_false),
            color = GameKeys.color(GameKeys.TRUE_FALSE),
            logoAsset = "images/menulogos/05hist_statements.png",
            enabled = true,
            onClick = onSelectTrueFalse
        ),
        QuizMode(
            roleName = GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE),
            title = GameKeys.title(GameKeys.BLIND_MAP_BATTLE),
            description = stringResource(R.string.home_desc_blind_map_battle),
            color = GameKeys.color(GameKeys.BLIND_MAP_BATTLE),
            logoAsset = "images/menulogos/06battle_map.png",
            enabled = true,
            onClick = onSelectBlindMapBattle
        )
    )

    LaunchedEffect(Unit) {
        logos = withContext(Dispatchers.Default) {
            modes.associate { mode ->
                mode.logoAsset to context.assets.open(mode.logoAsset).use { BitmapFactory.decodeStream(it) }.asImageBitmap()
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            NextQuizCountdown()
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modes) { mode -> QuizModeCard(mode, logos[mode.logoAsset]) }
        }
    }
}

@Composable
private fun QuizModeCard(mode: QuizMode, logo: ImageBitmap?) {
    var showInfo by remember { mutableStateOf(false) }
    val textColor = AppColors.Cream
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = mode.enabled) {
                Haptics.tick(context)
                mode.onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (mode.enabled) mode.color else mode.color.copy(alpha = 0.45f)
        )
    ) {
        Box(Modifier.fillMaxWidth()) {
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
                        stringResource(R.string.home_coming_soon),
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
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.home_about_content_desc, mode.roleName),
                    tint = textColor
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(stringResource(R.string.home_info_dialog_title, mode.roleName, mode.title)) },
            text = { Text(mode.description) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(stringResource(R.string.home_close)) }
            }
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

    val hours = remaining.toHours()
    val minutes = remaining.toMinutesPart()
    val seconds = remaining.toSecondsPart()
    Column(horizontalAlignment = Alignment.End) {
        Text(
            DailyClock.todayDisplay(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.home_countdown, "%02d:%02d:%02d".format(hours, minutes, seconds)),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
