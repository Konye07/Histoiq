package com.konye.historiq.Features.Play.BlindMapBattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.EraThemes
import com.konye.historiq.Core.Theme.GameBackgroundArt
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.ActiveEra
import com.konye.historiq.Data.Quiz.BattleLocation
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.getDoubleList
import com.konye.historiq.Data.Quiz.haversineKm
import com.konye.historiq.Data.Quiz.loadBattleLocations
import com.konye.historiq.Data.Quiz.mapPinpointScore
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.resumeOrStartQuiz
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.Features.Play.Shared.GameBarTitle
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

private const val QUESTIONS_PER_ROUND = 5
private const val GAME_KEY = GameKeys.BLIND_MAP_BATTLE

/** How close the map zooms on its own to the guess and the real place once the answer shows (the player can go further). */
private const val REVEAL_MAX_SCALE = 12f
private const val OVERVIEW_MAX_SCALE = 12f

private suspend fun persist(
    modeKey: String,
    ids: List<String>,
    guessLats: List<Double>,
    guessLngs: List<Double>,
    scores: List<Double>
) {
    QuizProgressStore.save(modeKey, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("guessLats", JsonArray(guessLats.map { JsonPrimitive(it) }))
        put("guessLngs", JsonArray(guessLngs.map { JsonPrimitive(it) }))
        put("scores", JsonArray(scores.map { JsonPrimitive(it) }))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlindMapBattleScreen(onBack: () -> Unit = {}) {
    val era = remember { ActiveEra.current() }
    val modeKey = remember { GameKeys.modeKey(GAME_KEY, era) }
    var battles by remember { mutableStateOf<List<BattleLocation>>(emptyList()) }
    var worldMap by remember { mutableStateOf<ImageBitmap?>(null) }
    var questionIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var guess by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val guessLats = remember { mutableStateListOf<Double>() }
    val guessLngs = remember { mutableStateListOf<Double>() }
    val scores = remember { mutableStateListOf<Double>() }
    var resumedAlreadyDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        worldMap = withContext(Dispatchers.Default) {
            tryLoadImageBitmap("images/world_map.png")
        }
        val resume = resumeOrStartQuiz(modeKey, QUESTIONS_PER_ROUND, { it.id }, ::loadBattleLocations)
        battles = resume.questions
        resume.resumedState?.let { stored ->
            guessLats.addAll(stored.getDoubleList("guessLats"))
            guessLngs.addAll(stored.getDoubleList("guessLngs"))
            scores.addAll(stored.getDoubleList("scores"))
            questionIndex = scores.size
            resumedAlreadyDone = questionIndex >= battles.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(GameKeys.color(modeKey, LocalIsDarkTheme.current))) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { GameBarTitle(StringsGamesFlagTerritoryBlindmap.blindmap_title, era) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesFlagTerritoryBlindmap.blindmap_back)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            GameBackgroundArt(
                EraThemes.backgroundAsset(era, GAME_KEY),
                MaterialTheme.colorScheme.onBackground,
                fallbackAssetPath = EraThemes.backgroundAsset(null, GAME_KEY)
            )
            val map = worldMap
            if (battles.isEmpty() || map == null) {
                HistoriqLoadingIndicator()
            } else if (questionIndex >= battles.size && resumedAlreadyDone) {
                MapResults(modeKey, battles, map, guessLats, guessLngs, scores, onBack)
            } else if (questionIndex >= battles.size) {
                AdGatedResults { MapResults(modeKey, battles, map, guessLats, guessLngs, scores, onBack) }
            } else {
                val current = battles[questionIndex]
                MapQuestion(
                    battle = current,
                    worldMap = map,
                    questionNumber = questionIndex + 1,
                    totalQuestions = battles.size,
                    revealed = revealed,
                    guess = guess,
                    lastScore = scores.getOrNull(questionIndex),
                    onTap = { if (!revealed) guess = it },
                    onSubmit = {
                        val (lat, lng) = guess ?: return@MapQuestion
                        val distanceKm = haversineKm(lat, lng, current.correctLat, current.correctLng)
                        guessLats.add(lat)
                        guessLngs.add(lng)
                        val score = mapPinpointScore(distanceKm)
                        scores.add(score)
                        if (score >= 550.0) { Haptics.correct(); BubbleSoundPlayer.playCorrectChirp() } else { Haptics.wrong(); BubbleSoundPlayer.playWrongTone() }
                        revealed = true
                    },
                    onNext = {
                        questionIndex += 1
                        guess = null
                        revealed = false
                    }
                )
            }
        }
    }
    }

    LaunchedEffect(scores.size) {
        if (scores.size in 1..battles.size && scores.size == guessLats.size) {
            persist(modeKey, battles.map { it.id }, guessLats.toList(), guessLngs.toList(), scores.toList())
        }
    }
}

@Composable
private fun MapQuestion(
    battle: BattleLocation,
    worldMap: ImageBitmap,
    questionNumber: Int,
    totalQuestions: Int,
    revealed: Boolean,
    guess: Pair<Double, Double>?,
    lastScore: Double?,
    onTap: (Pair<Double, Double>) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewport = remember { MapViewport(scope) }

    LaunchedEffect(questionNumber) { viewport.reset(animated = false) }

    // Zoom in on the guess and the real place together, so how far off it was can be read. Taps stop
    // counting once the answer shows, but the map stays free to pinch and drag, to look closer still.
    LaunchedEffect(revealed) {
        val placed = guess
        if (revealed && placed != null) {
            viewport.frame(
                listOf(
                    latLngToMapPoint(placed.first, placed.second, viewport.size),
                    latLngToMapPoint(battle.correctLat, battle.correctLng, viewport.size)
                ),
                maxScale = REVEAL_MAX_SCALE,
                animated = true
            )
        }
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(StringsGamesFlagTerritoryBlindmap.blindmap_question_counter(questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
        Text(battle.localizedBattle(LocalAppLanguage.current.code), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            if (revealed) StringsGamesFlagTerritoryBlindmap.blindmap_zoom_hint else StringsGamesFlagTerritoryBlindmap.blindmap_instructions,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            StringsGamesFlagTerritoryBlindmap.blindmap_map_attribution,
            style = MaterialTheme.typography.labelSmall
        )

        val placePin: (Offset) -> Unit = { point -> onTap(mapPointToLatLng(point, viewport.size)) }
        ZoomableWorldMap(
            worldMap = worldMap,
            viewport = viewport,
            onTap = if (revealed) null else placePin,
            zoomFocus = if (revealed) null else guess?.let { latLngToMapPoint(it.first, it.second, viewport.size) }
        ) {
            val guessOnScreen = guess?.let { viewport.toScreen(latLngToMapPoint(it.first, it.second, viewport.size)) }
            if (revealed) {
                val answerOnScreen = viewport.toScreen(latLngToMapPoint(battle.correctLat, battle.correctLng, viewport.size))
                if (guessOnScreen != null) {
                    drawLine(Color.White, guessOnScreen, answerOnScreen, strokeWidth = 2.dp.toPx())
                }
                drawPin(answerOnScreen, AnswerPinColor, extraRadius = 3.dp.toPx())
            }
            guessOnScreen?.let { drawPin(it, GuessPinColor) }
        }

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guess != null) { Text(StringsGamesFlagTerritoryBlindmap.blindmap_submit) }
        } else {
            MapLegend()
            Text(
                StringsGamesFlagTerritoryBlindmap.blindmap_score_points(formatFixed(lastScore ?: 0.0, 2)),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) StringsGamesFlagTerritoryBlindmap.blindmap_see_results else StringsGamesFlagTerritoryBlindmap.blindmap_next_question)
            }
        }
    }
}

/** One answered question as a pair of places on the final map, numbered like its row in the list. */
private data class PinPair(val number: Int, val guess: Pair<Double, Double>, val answer: Pair<Double, Double>)

/**
 * Every question on one map: the player's pin and the real place for each, joined by a line and carrying the
 * same number. It opens framed on all of them and zooms freely from there.
 */
@Composable
private fun AllPinsMap(worldMap: ImageBitmap, pairs: List<PinPair>) {
    val scope = rememberCoroutineScope()
    val viewport = remember { MapViewport(scope) }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    val labels = remember(pairs, labelStyle) { pairs.map { textMeasurer.measure(it.number.toString(), labelStyle) } }

    val mapSize = viewport.size
    LaunchedEffect(mapSize, pairs) {
        if (mapSize != IntSize.Zero) {
            viewport.frame(
                pairs.flatMap {
                    listOf(
                        latLngToMapPoint(it.guess.first, it.guess.second, mapSize),
                        latLngToMapPoint(it.answer.first, it.answer.second, mapSize)
                    )
                },
                maxScale = OVERVIEW_MAX_SCALE,
                animated = false
            )
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(StringsGamesFlagTerritoryBlindmap.blindmap_overview_title, style = MaterialTheme.typography.titleMedium)
        ZoomableWorldMap(worldMap = worldMap, viewport = viewport) {
            val guessesOnScreen = pairs.map { viewport.toScreen(latLngToMapPoint(it.guess.first, it.guess.second, viewport.size)) }
            val answersOnScreen = pairs.map { viewport.toScreen(latLngToMapPoint(it.answer.first, it.answer.second, viewport.size)) }
            pairs.indices.forEach { i ->
                drawLine(Color.White, guessesOnScreen[i], answersOnScreen[i], strokeWidth = 2.dp.toPx())
            }
            pairs.indices.forEach { i ->
                drawNumberedPin(answersOnScreen[i], AnswerPinColor, Color.White, labels[i], extraRadius = 3.dp.toPx())
            }
            pairs.indices.forEach { i -> drawNumberedPin(guessesOnScreen[i], GuessPinColor, AppColors.EspressoDark, labels[i]) }
        }
        MapLegend()
        Text(StringsGamesFlagTerritoryBlindmap.blindmap_zoom_hint, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MapResults(
    modeKey: String,
    battles: List<BattleLocation>,
    worldMap: ImageBitmap,
    guessLats: List<Double>,
    guessLngs: List<Double>,
    scores: List<Double>,
    onBack: () -> Unit
) {
    val total = scores.sum()
    val isPerfect = scores.isNotEmpty() && scores.all { it == 1000.0 }
    val (rank, totalPlayers, finalDistribution, topPercent) = rememberLiveRank(modeKey, modeKey, total, isPerfect)
    val maxPossible = scores.size * 1000.0
    fun scorePos(v: Double): Float = (v / maxPossible).toFloat().coerceIn(0f, 1f)

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ResultsHeader(
            gameKey = modeKey,
            scoreText = formatFixed(total, 2),
            isPerfect = isPerfect,
            rank = rank,
            totalPlayers = totalPlayers,
            topPercent = topPercent,
            accentColor = MaterialTheme.colorScheme.primary,
            maxScoreText = StringsHomeShared.results_of_points_max(formatThousands(maxPossible.toLong())),
            distribution = finalDistribution.map { scorePos(it) },
            yourPosition = scorePos(total),
            distributionMinLabel = "0",
            distributionMaxLabel = formatThousands(maxPossible.toLong())
        )
        HorizontalDivider()
        val pairs = battles.indices.mapNotNull { i ->
            val lat = guessLats.getOrNull(i)
            val lng = guessLngs.getOrNull(i)
            if (lat != null && lng != null) {
                PinPair(i + 1, lat to lng, battles[i].correctLat to battles[i].correctLng)
            } else {
                null
            }
        }
        if (pairs.isNotEmpty()) {
            AllPinsMap(worldMap, pairs)
            HorizontalDivider()
        }
        battles.forEachIndexed { i, battle ->
            Column(Modifier.fillMaxWidth()) {
                Text("${i + 1}. ${battle.localizedBattle(LocalAppLanguage.current.code)}", style = MaterialTheme.typography.bodyMedium)
                val guessLat = guessLats.getOrNull(i)
                val guessLng = guessLngs.getOrNull(i)
                Text(
                    if (guessLat != null && guessLng != null) {
                        StringsGamesFlagTerritoryBlindmap.blindmap_pin_actual_pts(
                            formatFixed(guessLat, 1),
                            formatFixed(guessLng, 1),
                            formatFixed(battle.correctLat, 1),
                            formatFixed(battle.correctLng, 1),
                            formatFixed(scores.getOrNull(i) ?: 0.0, 2)
                        )
                    } else {
                        StringsGamesFlagTerritoryBlindmap.blindmap_score_pts(formatFixed(scores.getOrNull(i) ?: 0.0, 2))
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(onClick = onBack) { Text(StringsGamesFlagTerritoryBlindmap.blindmap_back_to_menu) }
    }
}
