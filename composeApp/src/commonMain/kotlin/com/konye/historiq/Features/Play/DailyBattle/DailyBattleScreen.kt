package com.konye.historiq.Features.Play.DailyBattle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Localization.StringsDailyBattle
import com.konye.historiq.Core.Localization.StringsGamekeys
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Theme.EraThemes
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Data.Quiz.BATTLE_GAME_MAX
import com.konye.historiq.Data.Quiz.BATTLE_MAX_SCORE
import com.konye.historiq.Data.Quiz.BattleLeg
import com.konye.historiq.Data.Quiz.BattleLegs
import com.konye.historiq.Data.Quiz.BattleSession
import com.konye.historiq.Data.Quiz.DailyBattleStore
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.Features.Play.BlindMapBattle.BlindMapBattleScreen
import com.konye.historiq.Features.Play.CasualtiesMultiplier.CasualtiesMultiplierScreen
import com.konye.historiq.Features.Play.FlagColorMatch.FlagColorMatchScreen
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.Features.Play.TerritoryCompare.TerritoryCompareScreen
import com.konye.historiq.Features.Play.TrueFalse.TrueFalseScreen
import com.konye.historiq.Features.Play.YearGuess.YearGuessScreen
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import com.konye.historiq.generated.resources.ic_check
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * The Daily Battle: six questions, one from each of the six games in the Play menu's order (an Annalist event, a Herald
 * battle, an Officer-of-Arms flag, a Cosmographer comparison, a Chronicler statement, a Chancellor battle), added into one
 * score. It draws nothing of its own while a question is on screen, only that game's own screen (its colors, its art, its
 * look), and each answered question leads straight into the next. Its own pages, the overview it opens on and the final
 * results, use the app's plain theme and show each game in the game's own color. Once a day and resumable like every game:
 * the scores of the questions answered so far are kept ([DailyBattleStore]), and the question in progress is kept by its
 * own screen under its battle key.
 */
@Composable
fun DailyBattleScreen(onBack: () -> Unit = {}) {
    var scores by remember { mutableStateOf<List<Double>?>(null) }
    var playing by remember { mutableStateOf<Int?>(null) }
    // True only when the last question was answered during this visit: the ad plays then, not on a later look at the results.
    var finishedHere by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { scores = DailyBattleStore.load() }

    val playingGame = playing?.let { BattleLegs.games[it] }
    DisposableEffect(playingGame) {
        BattleSession.show(playingGame)
        onDispose { BattleSession.show(null) }
    }

    val finished = scores
    val index = playing
    when {
        finished == null -> BattleFrame(onBack) { padding -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { HistoriqLoadingIndicator() } }
        index != null -> {
            val leg = remember(index) {
                BattleLeg(BattleLegs.games[index], index + 1) { score ->
                    val done = scores
                    var answered = done?.size ?: index
                    if (done != null && done.size == index) {
                        val updated = done + score
                        scores = updated
                        answered = updated.size
                        if (answered == BattleLegs.size) finishedHere = true
                        scope.launch { DailyBattleStore.save(updated) }
                    }
                    // Straight on to the next question, or to the results after the last one.
                    playing = if (answered < BattleLegs.size) answered else null
                }
            }
            when (leg.game) {
                GameKeys.YEAR_GUESS -> YearGuessScreen(onBack, leg)
                GameKeys.CASUALTIES_MULTIPLIER -> CasualtiesMultiplierScreen(onBack, leg)
                GameKeys.FLAG_COLOR_MATCH -> FlagColorMatchScreen(onBack, leg)
                GameKeys.TERRITORY_COMPARE -> TerritoryCompareScreen(onBack, leg)
                GameKeys.TRUE_FALSE -> TrueFalseScreen(onBack, leg)
                else -> BlindMapBattleScreen(onBack, leg)
            }
        }
        finished.size >= BattleLegs.size -> BattleFrame(onBack) { padding ->
            if (finishedHere) {
                AdGatedResults { BattleResults(finished, padding, onBack) }
            } else {
                BattleResults(finished, padding, onBack)
            }
        }
        else -> BattleFrame(onBack) { padding -> BattleOverview(finished, padding, onStart = { playing = finished.size }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BattleFrame(onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StringsGamekeys.role_daily_battle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsDailyBattle.battle_back_cd)
                    }
                }
            )
        },
        content = content
    )
}

@Composable
private fun rememberGameLogos(): Map<String, ImageBitmap> {
    var logos by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    LaunchedEffect(Unit) {
        logos = withContext(Dispatchers.Default) {
            BattleLegs.games.mapNotNull { game -> tryLoadImageBitmap(EraThemes.allTimeLogoAsset(game))?.let { game to it } }.toMap()
        }
    }
    return logos
}

/** The page the battle opens on: what it is, the six questions with what each scored so far, and the button for the next one. */
@Composable
private fun BattleOverview(scores: List<Double>, padding: PaddingValues, onStart: () -> Unit) {
    val logos = rememberGameLogos()
    val dark = LocalIsDarkTheme.current
    val next = scores.size
    val nextGame = BattleLegs.games[next]

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(StringsDailyBattle.battle_intro(formatThousands(BATTLE_MAX_SCORE.toLong())), style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                StringsDailyBattle.battle_questions_answered(scores.size, BattleLegs.size),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (scores.isNotEmpty()) {
                Text(
                    StringsDailyBattle.battle_points_so_far(formatThousands(scores.sum().roundToInt().toLong())),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        BattleLegs.games.forEachIndexed { i, game ->
            BattleGameRow(i + 1, game, logos[game], scores.getOrNull(i), isNext = i == next, color = GameKeys.color(game, dark))
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (next == 0) StringsDailyBattle.battle_start_with(GameKeys.roleName(nextGame))
                else StringsDailyBattle.battle_continue_with(GameKeys.roleName(nextGame))
            )
        }
    }
}

/** Today's combined score and rank, and what each of the six answers added to it. */
@Composable
private fun BattleResults(scores: List<Double>, padding: PaddingValues, onBack: () -> Unit) {
    val total = scores.sum()
    val isPerfect = scores.size == BattleLegs.size && scores.all { it >= BATTLE_GAME_MAX }
    val (rank, totalPlayers, finalDistribution, topPercent) = rememberLiveRank(GameKeys.DAILY_BATTLE, GameKeys.DAILY_BATTLE, total, isPerfect)
    val logos = rememberGameLogos()
    val dark = LocalIsDarkTheme.current
    fun position(value: Double): Float = (value / BATTLE_MAX_SCORE).toFloat().coerceIn(0f, 1f)

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ResultsHeader(
            gameKey = GameKeys.DAILY_BATTLE,
            scoreText = formatThousands(total.roundToInt().toLong()),
            isPerfect = isPerfect,
            rank = rank,
            totalPlayers = totalPlayers,
            topPercent = topPercent,
            accentColor = MaterialTheme.colorScheme.primary,
            maxScoreText = StringsHomeShared.results_of_points_max(formatThousands(BATTLE_MAX_SCORE.toLong())),
            distribution = finalDistribution.map { position(it) },
            yourPosition = position(total),
            distributionMinLabel = "0",
            distributionMaxLabel = formatThousands(BATTLE_MAX_SCORE.toLong())
        )
        HorizontalDivider()
        Text(StringsDailyBattle.battle_results_your_answers, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BattleLegs.games.forEachIndexed { i, game ->
                BattleGameRow(i + 1, game, logos[game], scores.getOrNull(i), isNext = false, color = GameKeys.color(game, dark))
            }
        }
        Button(onClick = onBack) { Text(StringsDailyBattle.battle_back_to_menu) }
    }
}

/**
 * One of the six questions as a card in its game's own color, with its number, the game's picture and name and, once
 * answered, its score out of [BATTLE_GAME_MAX]. The question to answer next is marked; the ones after it are dimmed like
 * an unavailable card.
 */
@Composable
private fun BattleGameRow(number: Int, game: String, logo: ImageBitmap?, score: Double?, isNext: Boolean, color: Color) {
    val dimmed = score == null && !isNext
    // A dimmed card is the game's color faded into the page, so its text is chosen against what actually shows.
    val shown = if (dimmed) color.copy(alpha = 0.45f).compositeOver(MaterialTheme.colorScheme.background) else color
    val textColor = contrastingOn(shown)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = shown),
        // Some of the games' colors are as dark as the dark theme's page, so the edge is what keeps the card visible.
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "$number",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor.dimmedOn(shown, 0.85f),
                modifier = Modifier.width(14.dp)
            )
            Box(Modifier.width(64.dp).height(44.dp), contentAlignment = Alignment.Center) {
                if (logo != null) {
                    Image(bitmap = logo, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            }
            Column(Modifier.weight(1f)) {
                Text(GameKeys.roleName(game), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text(GameKeys.title(game), style = MaterialTheme.typography.bodySmall, color = textColor.dimmedOn(shown, 0.85f))
            }
            if (score != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painterResource(Res.drawable.ic_check), contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                        Text(
                            formatThousands(score.roundToInt().toLong()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Text(
                        "/ " + formatThousands(BATTLE_GAME_MAX.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.dimmedOn(shown, 0.85f)
                    )
                }
            } else if (isNext) {
                Surface(color = textColor, contentColor = shown, shape = RoundedCornerShape(50)) {
                    Text(
                        StringsDailyBattle.battle_up_next,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
