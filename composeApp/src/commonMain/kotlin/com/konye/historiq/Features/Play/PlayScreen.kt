package com.konye.historiq.Features.Play

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Feedback.RateAppPrompt
import com.konye.historiq.Core.Formatting.formatTwoDigits
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsEras
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.StringsRateApp
import com.konye.historiq.Core.Localization.todayDisplay
import com.konye.historiq.Core.Platform.HISTORIQ_CONTACT_EMAIL
import com.konye.historiq.Core.Platform.openEmailComposer
import com.konye.historiq.Core.Platform.openUrl
import com.konye.historiq.Core.Theme.APP_LOGO_SHARED_KEY
import com.konye.historiq.Core.Theme.AppLogoImage
import com.konye.historiq.Core.Theme.EraThemes
import com.konye.historiq.Core.Theme.LocalSharedTransitionScope
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.ActiveEra
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.GameStreak
import com.konye.historiq.Data.Quiz.QuizEra
import com.konye.historiq.Data.Quiz.StreakStore
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.Features.Play.Shared.EraModeStrip
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

/** The all-time card pictures — also what an era's card falls back to until the era has its own. */
private val ALL_TIME_LOGOS = mapOf(
    GameKeys.YEAR_GUESS to "images/menulogos/01guess_year.png",
    GameKeys.CASUALTIES_MULTIPLIER to "images/menulogos/02casualities.png",
    GameKeys.FLAG_COLOR_MATCH to "images/menulogos/03paint_flag.png",
    GameKeys.TERRITORY_COMPARE to "images/menulogos/04peak_empire_vs.png",
    GameKeys.TRUE_FALSE to "images/menulogos/05hist_statements.png",
    GameKeys.BLIND_MAP_BATTLE to "images/menulogos/06battle_map.png"
)

@OptIn(ExperimentalSharedTransitionApi::class)
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
    var banner by remember { mutableStateOf<ImageBitmap?>(null) }
    var streaks by remember { mutableStateOf<Map<String, GameStreak>>(emptyMap()) }
    var showRateDialog by remember { mutableStateOf(false) }
    val era by ActiveEra.selected.collectAsState()

    val modes = era?.let { current ->
        listOf(
            eraQuizMode(current, GameKeys.YEAR_GUESS, StringsHomeShared.home_desc_year_guess, onSelectYearGuess),
            eraQuizMode(current, GameKeys.CASUALTIES_MULTIPLIER, StringsHomeShared.home_desc_casualties_multiplier, onSelectCasualtiesMultiplier),
            eraQuizMode(current, GameKeys.BLIND_MAP_BATTLE, StringsHomeShared.home_desc_blind_map_battle, onSelectBlindMapBattle)
        )
    } ?: listOf(
        QuizMode(
            key = GameKeys.YEAR_GUESS,
            roleName = GameKeys.roleName(GameKeys.YEAR_GUESS),
            title = GameKeys.title(GameKeys.YEAR_GUESS),
            description = StringsHomeShared.home_desc_year_guess,
            color = GameKeys.color(GameKeys.YEAR_GUESS),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.YEAR_GUESS),
            enabled = true,
            onClick = onSelectYearGuess
        ),
        QuizMode(
            key = GameKeys.CASUALTIES_MULTIPLIER,
            roleName = GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
            title = GameKeys.title(GameKeys.CASUALTIES_MULTIPLIER),
            description = StringsHomeShared.home_desc_casualties_multiplier,
            color = GameKeys.color(GameKeys.CASUALTIES_MULTIPLIER),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.CASUALTIES_MULTIPLIER),
            enabled = true,
            onClick = onSelectCasualtiesMultiplier
        ),
        QuizMode(
            key = GameKeys.FLAG_COLOR_MATCH,
            roleName = GameKeys.roleName(GameKeys.FLAG_COLOR_MATCH),
            title = GameKeys.title(GameKeys.FLAG_COLOR_MATCH),
            description = StringsHomeShared.home_desc_flag_color_match,
            color = GameKeys.color(GameKeys.FLAG_COLOR_MATCH),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.FLAG_COLOR_MATCH),
            enabled = true,
            onClick = onSelectFlagColorMatch
        ),
        QuizMode(
            key = GameKeys.TERRITORY_COMPARE,
            roleName = GameKeys.roleName(GameKeys.TERRITORY_COMPARE),
            title = GameKeys.title(GameKeys.TERRITORY_COMPARE),
            description = StringsHomeShared.home_desc_territory_compare,
            color = GameKeys.color(GameKeys.TERRITORY_COMPARE),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.TERRITORY_COMPARE),
            enabled = true,
            onClick = onSelectTerritoryCompare
        ),
        QuizMode(
            key = GameKeys.TRUE_FALSE,
            roleName = GameKeys.roleName(GameKeys.TRUE_FALSE),
            title = GameKeys.title(GameKeys.TRUE_FALSE),
            description = StringsHomeShared.home_desc_true_false,
            color = GameKeys.color(GameKeys.TRUE_FALSE),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.TRUE_FALSE),
            enabled = true,
            onClick = onSelectTrueFalse
        ),
        QuizMode(
            key = GameKeys.BLIND_MAP_BATTLE,
            roleName = GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE),
            title = GameKeys.title(GameKeys.BLIND_MAP_BATTLE),
            description = StringsHomeShared.home_desc_blind_map_battle,
            color = GameKeys.color(GameKeys.BLIND_MAP_BATTLE),
            logoAsset = ALL_TIME_LOGOS.getValue(GameKeys.BLIND_MAP_BATTLE),
            enabled = true,
            onClick = onSelectBlindMapBattle
        )
    )

    LaunchedEffect(Unit) {
        torch = withContext(Dispatchers.Default) { tryLoadImageBitmap("images/streak_torch.png") }
    }

    LaunchedEffect(era) {
        banner = null
        val shownEra = era
        val missing = modes.filter { it.logoAsset !in logos }
        val (loadedLogos, loadedBanner) = withContext(Dispatchers.Default) {
            val loadedLogos = missing.mapNotNull { mode ->
                (tryLoadImageBitmap(mode.logoAsset) ?: tryLoadImageBitmap(ALL_TIME_LOGOS[mode.key]))?.let { mode.logoAsset to it }
            }.toMap()
            loadedLogos to shownEra?.let { tryLoadImageBitmap(EraThemes.bannerAsset(it)) }
        }
        logos = logos + loadedLogos
        banner = loadedBanner
    }

    LaunchedEffect(Unit) {
        streaks = runCatching { StreakStore.computeAll() }.getOrDefault(emptyMap())
    }

    LaunchedEffect(Unit) {
        showRateDialog = RateAppPrompt.shouldShow()
    }

    EraModeTheme(era) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sharedScope = LocalSharedTransitionScope.current
                    val logoModifier = Modifier.size(36.dp).let { base ->
                        if (sharedScope == null) base else with(sharedScope) {
                            base.sharedElementWithCallerManagedVisibility(
                                rememberSharedContentState(key = APP_LOGO_SHARED_KEY),
                                visible = true,
                                clipInOverlayDuringTransition = OverlayClip(RectangleShape)
                            )
                        }
                    }
                    AppLogoImage(modifier = logoModifier)
                    Text(StringsHomeShared.home_title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                NextQuizCountdown()
            }

            EraModeStrip(selected = era, onSelect = ActiveEra::select)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                era?.let { current -> item { EraModeHeader(current, banner) } }
                items(modes) { mode -> QuizModeCard(mode, logos[mode.logoAsset], torch, streaks[GameKeys.modeKey(mode.key, era)]) }
                item { SuggestionPanel() }
            }
        }
    }

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = {
                showRateDialog = false
                RateAppPrompt.onLater()
            },
            title = { Text(StringsRateApp.rate_app_title) },
            text = { Text(StringsRateApp.rate_app_body) },
            confirmButton = {
                TextButton(onClick = {
                    showRateDialog = false
                    RateAppPrompt.onDecided()
                    openUrl(RateAppPrompt.storeUrl())
                }) { Text(StringsRateApp.rate_app_rate_now) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showRateDialog = false
                        RateAppPrompt.onDecided()
                    }) { Text(StringsRateApp.rate_app_never) }
                    TextButton(onClick = {
                        showRateDialog = false
                        RateAppPrompt.onLater()
                    }) { Text(StringsRateApp.rate_app_later) }
                }
            }
        )
    }
}

@Composable
private fun eraQuizMode(era: QuizEra, gameKey: String, description: String, onClick: () -> Unit) = QuizMode(
    key = gameKey,
    roleName = GameKeys.roleName(gameKey),
    title = GameKeys.title(gameKey),
    description = description,
    color = GameKeys.color(GameKeys.modeKey(gameKey, era)),
    logoAsset = EraThemes.logoAsset(era, gameKey),
    enabled = true,
    onClick = onClick
)

/** An era game mode paints the whole Play screen in its own palette; All-time keeps the app's default look. */
@Composable
private fun EraModeTheme(era: QuizEra?, content: @Composable () -> Unit) {
    if (era == null) {
        content()
        return
    }
    val colorScheme = remember(era) { gameColorScheme(EraThemes.palette(era).base) }
    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}

/** What opens an era's list of games: its optional banner picture, its name and years, and what the mode is. */
@Composable
private fun EraModeHeader(era: QuizEra, banner: ImageBitmap?) {
    val background = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (banner != null) {
            Image(
                bitmap = banner,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        Text(StringsEras.label(era), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(StringsEras.range(era), style = MaterialTheme.typography.titleSmall, color = onBackground.dimmedOn(background, 0.8f))
        Text(
            StringsEras.era_mode_blurb(
                GameKeys.roleName(GameKeys.YEAR_GUESS),
                GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
                GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = onBackground.dimmedOn(background, 0.85f)
        )
    }
}

/** A slim footer, not a game card — tapping it opens an email to Historiq's own inbox, pre-addressed. */
@Composable
private fun SuggestionPanel() {
    var showInfo by remember { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = contrastingOn(containerColor)

    Surface(color = containerColor) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { openEmailComposer(HISTORIQ_CONTACT_EMAIL) }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    StringsHomeShared.suggestion_panel_title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    HISTORIQ_CONTACT_EMAIL,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.dimmedOn(containerColor, 0.85f),
                    textDecoration = TextDecoration.Underline
                )
            }
            IconButton(onClick = { showInfo = true }) {
                Icon(
                    painterResource(Res.drawable.ic_info),
                    contentDescription = StringsHomeShared.suggestion_panel_content_desc,
                    tint = textColor
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(StringsHomeShared.suggestion_panel_title) },
            text = { Text(StringsHomeShared.suggestion_panel_info_body) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(StringsHomeShared.home_close) }
            }
        )
    }
}

@Composable
private fun QuizModeCard(mode: QuizMode, logo: ImageBitmap?, torch: ImageBitmap?, streak: GameStreak?) {
    var showInfo by remember { mutableStateOf(false) }
    var showStreakInfo by remember { mutableStateOf(false) }
    val textColor = contrastingOn(mode.color)

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
                    textColor = contrastingOn(StreakBadgeScrim.compositeOver(mode.color)),
                    onClick = { showStreakInfo = true },
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
                    color = textColor.dimmedOn(mode.color, 0.85f),
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

    if (showStreakInfo) {
        AlertDialog(
            onDismissRequest = { showStreakInfo = false },
            title = { Text(StringsHomeShared.home_streak_dialog_title) },
            text = { Text(StringsHomeShared.home_streak_dialog_body) },
            confirmButton = {
                TextButton(onClick = { showStreakInfo = false }) { Text(StringsHomeShared.home_close) }
            }
        )
    }
}

private val StreakBadgeScrim = Color.Black.copy(alpha = 0.28f)

/**
 * Lit (original colors) once today's entry for this game is in, grayed out otherwise — the streak
 * count itself keeps showing the still-alive-but-at-risk value from yesterday either way, same as
 * [StreakStore]'s own playedToday/days split.
 */
@Composable
private fun StreakBadge(
    streak: GameStreak,
    torch: ImageBitmap?,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDesc = StringsHomeShared.home_streak_content_desc(streak.days)
    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(50))
            .background(StreakBadgeScrim)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .semantics {
                contentDescription = contentDesc
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (torch != null) {
            val aspect = torch.width.toFloat() / torch.height.toFloat()
            Image(
                bitmap = torch,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (streak.playedToday) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier
                    .height(32.dp)
                    .aspectRatio(aspect)
            )
        }
        Text(
            "${streak.days}",
            style = MaterialTheme.typography.titleLarge,
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
