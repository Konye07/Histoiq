package com.yourorg.app.Features.Play.BlindMapBattle

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
import androidx.compose.material3.CircularProgressIndicator
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
import com.yourorg.app.Core.Formatting.formatFixed
import com.yourorg.app.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.BattleLocation
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.getDoubleList
import com.yourorg.app.Data.Quiz.haversineKm
import com.yourorg.app.Data.Quiz.loadBattleLocations
import com.yourorg.app.Data.Quiz.mapPinpointScore
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Data.Quiz.resumeOrStartQuiz
import com.yourorg.app.Data.Quiz.tryLoadImageBitmap
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_arrow_back
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
private const val PROGRESS_KEY = GameKeys.BLIND_MAP_BATTLE
private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f

private suspend fun persist(
    ids: List<String>,
    guessLats: List<Double>,
    guessLngs: List<Double>,
    scores: List<Double>
) {
    QuizProgressStore.save(PROGRESS_KEY, buildJsonObject {
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
    var battles by remember { mutableStateOf<List<BattleLocation>>(emptyList()) }
    var worldMap by remember { mutableStateOf<ImageBitmap?>(null) }
    var questionIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var guessOffset by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val guessLats = remember { mutableStateListOf<Double>() }
    val guessLngs = remember { mutableStateListOf<Double>() }
    val scores = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        worldMap = withContext(Dispatchers.Default) {
            tryLoadImageBitmap("images/world_map.png")
        }
        val resume = resumeOrStartQuiz(PROGRESS_KEY, QUESTIONS_PER_ROUND, { it.id }, ::loadBattleLocations)
        battles = resume.questions
        resume.resumedState?.let { stored ->
            guessLats.addAll(stored.getDoubleList("guessLats"))
            guessLngs.addAll(stored.getDoubleList("guessLngs"))
            scores.addAll(stored.getDoubleList("scores"))
            questionIndex = scores.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.EspressoDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StringsGamesFlagTerritoryBlindmap.blindmap_title) },
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
            val map = worldMap
            if (battles.isEmpty() || map == null) {
                CircularProgressIndicator()
            } else if (questionIndex >= battles.size) {
                MapResults(battles, guessLats, guessLngs, scores, onBack)
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
                        scores.add(mapPinpointScore(distanceKm))
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
            persist(battles.map { it.id }, guessLats.toList(), guessLngs.toList(), scores.toList())
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
        Modifier.fillMaxWidth().padding(24.dp),
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

        fun clampPan(pan: Offset, currentScale: Float, size: IntSize): Offset {
            val minX = size.width * (1f - currentScale)
            val minY = size.height * (1f - currentScale)
            return Offset(pan.x.coerceIn(min(minX, 0f), 0f), pan.y.coerceIn(min(minY, 0f), 0f))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .onGloballyPositioned {
                    boxSize = it.size
                    onCanvasSized(it.size)
                }
                .pointerInput(revealed, boxSize) {
                    if (!revealed) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                            val newPan = clampPan(panOffset + pan, newScale, boxSize)
                            scale = newScale
                            panOffset = newPan
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
    battles: List<BattleLocation>,
    guessLats: List<Double>,
    guessLngs: List<Double>,
    scores: List<Double>,
    onBack: () -> Unit
) {
    val total = scores.sum()
    val isPerfect = scores.isNotEmpty() && scores.all { it == 1000.0 }
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val result = recordAndRank(PROGRESS_KEY, GameKeys.BLIND_MAP_BATTLE, total, isPerfect)
        rank = result.rank
        totalPlayers = result.totalPlayers
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ResultsHeader(
            gameKey = PROGRESS_KEY,
            scoreText = formatFixed(total, 2),
            isPerfect = isPerfect,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary
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
