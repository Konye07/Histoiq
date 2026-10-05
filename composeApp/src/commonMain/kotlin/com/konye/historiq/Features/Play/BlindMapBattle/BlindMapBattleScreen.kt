package com.konye.historiq.Features.Play.BlindMapBattle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Haptics.Haptics
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
import kotlin.math.min
import org.jetbrains.compose.resources.painterResource

/**
 * The bundled map (world_map.png) is a black-land/white-ocean silhouette rather than a colored
 * physical map, so it's tinted here via a duotone ColorMatrix: black -> continent color (ForestDark),
 * white -> water color (SlateBlue), linearly in between. Real coastlines from Natural Earth data,
 * Wikimedia Commons, CC BY-SA 4.0.
 */
private val MAP_COLOR_MATRIX = ColorMatrix(
    floatArrayOf(
        (120f - 30f) / 255f, 0f, 0f, 0f, 30f,
        0f, (140f - 42f) / 255f, 0f, 0f, 42f,
        0f, 0f, (158f - 32f) / 255f, 0f, 32f,
        0f, 0f, 0f, 1f, 0f
    )
)

private const val QUESTIONS_PER_ROUND = 5
private const val GAME_KEY = GameKeys.BLIND_MAP_BATTLE
private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f

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

private fun offsetToLatLng(offset: Offset, size: IntSize): Pair<Double, Double> {
    val lng = (offset.x / size.width) * 360.0 - 180.0
    val lat = 90.0 - (offset.y / size.height) * 180.0
    return lat to lng
}

private fun latLngToOffset(lat: Double, lng: Double, size: IntSize): Offset {
    val x = ((lng + 180.0) / 360.0 * size.width).toFloat()
    val y = ((90.0 - lat) / 180.0 * size.height).toFloat()
    return Offset(x, y)
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
    var guessOffset by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
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
                MapResults(modeKey, battles, guessLats, guessLngs, scores, onBack)
            } else if (questionIndex >= battles.size) {
                AdGatedResults { MapResults(modeKey, battles, guessLats, guessLngs, scores, onBack) }
            } else {
                val current = battles[questionIndex]
                MapQuestion(
                    battle = current,
                    worldMap = map,
                    questionNumber = questionIndex + 1,
                    totalQuestions = battles.size,
                    revealed = revealed,
                    guessOffset = guessOffset,
                    lastScore = scores.getOrNull(questionIndex),
                    onCanvasSized = { canvasSize = it },
                    onTap = { offset -> if (!revealed) guessOffset = offset },
                    onSubmit = {
                        val offset = guessOffset ?: return@MapQuestion
                        if (canvasSize.width == 0 || canvasSize.height == 0) return@MapQuestion
                        val (lat, lng) = offsetToLatLng(offset, canvasSize)
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
                        guessOffset = null
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
    guessOffset: Offset?,
    lastScore: Double?,
    onCanvasSized: (IntSize) -> Unit,
    onTap: (Offset) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(StringsGamesFlagTerritoryBlindmap.blindmap_question_counter(questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
        Text(battle.battle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(StringsGamesFlagTerritoryBlindmap.blindmap_instructions, style = MaterialTheme.typography.bodySmall)
        Text(
            StringsGamesFlagTerritoryBlindmap.blindmap_map_attribution,
            style = MaterialTheme.typography.labelSmall
        )

        var boxSize by remember(questionNumber) { mutableStateOf(IntSize.Zero) }
        var scale by remember(questionNumber) { mutableStateOf(MIN_SCALE) }
        var panOffset by remember(questionNumber) { mutableStateOf(Offset.Zero) }

        // Reset to fully zoomed-out once the answer is revealed, so both the guess and the
        // correct pin are visible even if the player was zoomed in on a small region to place
        // their tap precisely — otherwise the correct location could be off-screen entirely.
        LaunchedEffect(revealed) {
            if (revealed) {
                scale = MIN_SCALE
                panOffset = Offset.Zero
            }
        }

        fun clampPan(pan: Offset, currentScale: Float, size: IntSize): Offset {
            val minX = size.width * (1f - currentScale)
            val minY = size.height * (1f - currentScale)
            return Offset(pan.x.coerceIn(min(minX, 0f), 0f), pan.y.coerceIn(min(minY, 0f), 0f))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .clipToBounds()
                .onGloballyPositioned {
                    boxSize = it.size
                    onCanvasSized(it.size)
                }
                .pointerInput(revealed, boxSize) {
                    if (!revealed) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                            // Keep the content point under the pinch centroid fixed on screen,
                            // instead of always scaling from the (0,0) origin — otherwise zooming
                            // visibly drifts toward the top-left corner instead of where you pinch.
                            val contentAtCentroid = (centroid - panOffset) / scale
                            val zoomAdjustedPan = centroid + pan - contentAtCentroid * newScale
                            scale = newScale
                            panOffset = clampPan(zoomAdjustedPan, newScale, boxSize)
                        }
                    }
                }
                .pointerInput(revealed) {
                    if (!revealed) {
                        detectTapGestures { rawOffset ->
                            val adjusted = Offset(
                                (rawOffset.x - panOffset.x) / scale,
                                (rawOffset.y - panOffset.y) / scale
                            )
                            onTap(adjusted)
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(color = AppColors.SlateBlue)
                if (boxSize.width > 0) {
                    withTransform({
                        translate(left = panOffset.x, top = panOffset.y)
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawImage(
                            worldMap,
                            dstSize = IntSize(boxSize.width, boxSize.height),
                            colorFilter = ColorFilter.colorMatrix(MAP_COLOR_MATRIX)
                        )
                        guessOffset?.let {
                            drawCircle(color = Color(0xFFFFC107), radius = 8f / scale, center = it)
                        }
                        if (revealed) {
                            val correctOffset = latLngToOffset(battle.correctLat, battle.correctLng, boxSize)
                            drawCircle(color = Color(0xFFE53935), radius = 8f / scale, center = correctOffset)
                            guessOffset?.let {
                                drawLine(color = Color.White, start = it, end = correctOffset, strokeWidth = 2f / scale)
                            }
                        }
                    }
                }
            }
        }

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessOffset != null) { Text(StringsGamesFlagTerritoryBlindmap.blindmap_submit) }
        } else {
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

@Composable
private fun MapResults(
    modeKey: String,
    battles: List<BattleLocation>,
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
        battles.forEachIndexed { i, battle ->
            Column(Modifier.fillMaxWidth()) {
                Text(battle.battle, style = MaterialTheme.typography.bodyMedium)
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
