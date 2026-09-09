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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Data.Quiz.DailyClock
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
            roleName = "Annalist",
            title = "Guess the Year",
            description = "An event is given — guess the year it happened. The closer you are, the more points you score.",
            color = AppColors.SlateBlue,
            logoAsset = "images/menulogos/01guess_year.png",
            enabled = true,
            onClick = onSelectYearGuess
        ),
        QuizMode(
            roleName = "Herald",
            title = "Casualty Count",
            description = "A battle is given — guess the casualties. The closer your guess, the bigger your multiplier.",
            color = AppColors.Maroon,
            logoAsset = "images/menulogos/02casualities.png",
            enabled = true,
            onClick = onSelectCasualtiesMultiplier
        ),
        QuizMode(
            roleName = "Officer-of-Arms",
            title = "Match the Flag Color",
            description = "Match a historical flag's true color using hue, saturation and brightness sliders.",
            color = AppColors.OliveDark,
            logoAsset = "images/menulogos/03paint_flag.png",
            enabled = true,
            onClick = onSelectFlagColorMatch
        ),
        QuizMode(
            roleName = "Cosmographer",
            title = "Bigger Territory",
            description = "Pick which empire had the bigger territory at its prime. One wrong pick ends the run.",
            color = AppColors.ForestDark,
            logoAsset = "images/menulogos/04peak_empire_vs.png",
            enabled = true,
            onClick = onSelectTerritoryCompare
        ),
        QuizMode(
            roleName = "Chronicler",
            title = "True or False",
            description = "Historical statements — one wrong answer ends the run.",
            color = AppColors.BrownDark,
            logoAsset = "images/menulogos/05hist_statements.png",
            enabled = true,
            onClick = onSelectTrueFalse
        ),
        QuizMode(
            roleName = "Chancellor",
            title = "Blind Map Battle",
            description = "Place a historical battle on the map with no borders shown. The closer your pin, the more points.",
            color = AppColors.EspressoDark,
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
            Text("Today in History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                        "Coming soon",
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
                    contentDescription = "About ${mode.roleName}",
                    tint = textColor
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("${mode.roleName} — ${mode.title}") },
            text = { Text(mode.description) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Close") }
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
    Text(
        "New quiz in %02d:%02d:%02d".format(hours, minutes, seconds),
        style = MaterialTheme.typography.labelMedium
    )
}
