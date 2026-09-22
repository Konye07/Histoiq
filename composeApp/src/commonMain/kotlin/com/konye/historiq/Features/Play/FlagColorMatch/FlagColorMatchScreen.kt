package com.konye.historiq.Features.Play.FlagColorMatch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.GameBackgroundArt
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.DailyQuizRepository
import com.konye.historiq.Data.Quiz.FlagColor
import com.konye.historiq.Data.Quiz.FlagColorAnalysis
import com.konye.historiq.Data.Quiz.FlagColorCandidate
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.buildFlagColorAnalysis
import com.konye.historiq.Data.Quiz.colorMatchScore
import com.konye.historiq.Data.Quiz.findFlagColorCandidates
import com.konye.historiq.Data.Quiz.getDoubleList
import com.konye.historiq.Data.Quiz.getIntList
import com.konye.historiq.Data.Quiz.getStringList
import com.konye.historiq.Data.Quiz.loadFlagColors
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.Features.Play.Shared.rankingText
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.FLAG_COLOR_MATCH

private suspend fun persist(
    ids: List<String>,
    targetHues: List<Int>,
    targetSaturations: List<Int>,
    targetBrightnesses: List<Int>,
    hues: List<Int>,
    saturations: List<Int>,
    brightnesses: List<Int>,
    scores: List<Double>
) {
    QuizProgressStore.save(PROGRESS_KEY, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("targetHues", JsonArray(targetHues.map { JsonPrimitive(it) }))
        put("targetSaturations", JsonArray(targetSaturations.map { JsonPrimitive(it) }))
        put("targetBrightnesses", JsonArray(targetBrightnesses.map { JsonPrimitive(it) }))
        put("hues", JsonArray(hues.map { JsonPrimitive(it) }))
        put("saturations", JsonArray(saturations.map { JsonPrimitive(it) }))
        put("brightnesses", JsonArray(brightnesses.map { JsonPrimitive(it) }))
        put("scores", JsonArray(scores.map { JsonPrimitive(it) }))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagColorMatchScreen(onBack: () -> Unit = {}) {
    var flags by remember { mutableStateOf<List<FlagColor>>(emptyList()) }
    var analysisByFlagId by remember { mutableStateOf<Map<String, FlagColorAnalysis>>(emptyMap()) }
    var questionIndex by remember { mutableStateOf(0) }
    var hue by remember { mutableStateOf(180f) }
    var saturation by remember { mutableStateOf(50f) }
    var brightness by remember { mutableStateOf(50f) }
    var revealed by remember { mutableStateOf(false) }
    val guessHues = remember { mutableStateListOf<Int>() }
    val guessSaturations = remember { mutableStateListOf<Int>() }
    val guessBrightnesses = remember { mutableStateListOf<Int>() }
    val scores = remember { mutableStateListOf<Double>() }
    var resumedAlreadyDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = QuizProgressStore.load(PROGRESS_KEY)?.takeIf { "targetHues" in it }
        val chosen: List<FlagColor>
        val targets: List<FlagColorCandidate>
        if (stored != null) {
            val ids = stored.getStringList("ids")
            val rows = loadFlagColors(ids)
            chosen = ids.mapNotNull { id -> rows.find { it.id == id } }
            val targetHues = stored.getIntList("targetHues")
            val targetSats = stored.getIntList("targetSaturations")
            val targetBrights = stored.getIntList("targetBrightnesses")
            targets = targetHues.indices.map { i -> FlagColorCandidate(targetHues[i], targetSats[i], targetBrights[i]) }
            guessHues.addAll(stored.getIntList("hues"))
            guessSaturations.addAll(stored.getIntList("saturations"))
            guessBrightnesses.addAll(stored.getIntList("brightnesses"))
            scores.addAll(stored.getDoubleList("scores"))
            questionIndex = scores.size
            resumedAlreadyDone = questionIndex >= chosen.size
        } else {
            val todayKey = DailyClock.todayKey()
            val ids = DailyQuizRepository.getOrCreate(PROGRESS_KEY, todayKey, QUESTIONS_PER_ROUND)
            val rows = loadFlagColors(ids)
            val newlyChosen = ids.mapNotNull { id -> rows.find { it.id == id } }
            chosen = newlyChosen
            targets = withContext(Dispatchers.Default) {
                newlyChosen.map { flag ->
                    val bitmap = tryLoadImageBitmap(flag.imageAsset)
                    requireNotNull(bitmap) { "Could not decode flag asset: ${flag.imageAsset}" }
                    val candidates = findFlagColorCandidates(bitmap)
                    // Seeded by (date, flag id) rather than plain .random(), so every player gets
                    // the same target color for the same flag on the same day — an unseeded pick
                    // here previously let two devices land on different candidate colors (e.g. one
                    // player asked to match red, another green) for the identical daily flag.
                    val seed = "$todayKey:${flag.id}".hashCode().toLong()
                    candidates[Random(seed).nextInt(candidates.size)]
                }
            }
            persist(
                chosen.map { it.id },
                targets.map { it.hue },
                targets.map { it.saturation },
                targets.map { it.brightness },
                emptyList(), emptyList(), emptyList(), emptyList()
            )
        }
        flags = chosen
        analysisByFlagId = withContext(Dispatchers.Default) {
            chosen.indices.associate { i ->
                val bitmap = tryLoadImageBitmap(chosen[i].imageAsset)
                requireNotNull(bitmap) { "Could not decode flag asset: ${chosen[i].imageAsset}" }
                chosen[i].id to buildFlagColorAnalysis(bitmap, targets[i])
            }
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.OliveDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StringsGamesFlagTerritoryBlindmap.flag_title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesFlagTerritoryBlindmap.flag_back)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            GameBackgroundArt("images/backgrounds/bg_flag_color_match.png", MaterialTheme.colorScheme.onBackground)
            when {
                flags.isEmpty() || analysisByFlagId.size < flags.size -> CircularProgressIndicator()
                questionIndex >= flags.size -> {
                    if (resumedAlreadyDone) {
                        FlagColorResults(
                            flags, analysisByFlagId, guessHues, guessSaturations, guessBrightnesses, scores, onBack
                        )
                    } else {
                        AdGatedResults {
                            FlagColorResults(
                                flags, analysisByFlagId, guessHues, guessSaturations, guessBrightnesses, scores, onBack
                            )
                        }
                    }
                }
                else -> {
                    val current = flags[questionIndex]
                    val analysis = analysisByFlagId.getValue(current.id)
                    FlagColorQuestion(
                        flag = current,
                        analysis = analysis,
                        questionNumber = questionIndex + 1,
                        totalQuestions = flags.size,
                        hue = hue,
                        saturation = saturation,
                        brightness = brightness,
                        revealed = revealed,
                        lastScore = scores.getOrNull(questionIndex),
                        onHueChange = { hue = it },
                        onSaturationChange = { saturation = it },
                        onBrightnessChange = { brightness = it },
                        onSubmit = {
                            val h = hue.toInt()
                            val s = saturation.toInt()
                            val b = brightness.toInt()
                            guessHues.add(h)
                            guessSaturations.add(s)
                            guessBrightnesses.add(b)
                            scores.add(
                                colorMatchScore(
                                    guessHue = h,
                                    guessSaturation = s,
                                    guessBrightness = b,
                                    correctHue = analysis.targetHue,
                                    correctSaturation = analysis.targetSaturation,
                                    correctBrightness = analysis.targetBrightness
                                )
                            )
                            revealed = true
                        },
                        onNext = {
                            questionIndex += 1
                            hue = 180f
                            saturation = 50f
                            brightness = 50f
                            revealed = false
                        }
                    )
                }
            }
        }
    }
    }

    LaunchedEffect(scores.size) {
        if (scores.size in 1..flags.size && analysisByFlagId.size >= flags.size) {
            persist(
                flags.map { it.id },
                flags.map { analysisByFlagId.getValue(it.id).targetHue },
                flags.map { analysisByFlagId.getValue(it.id).targetSaturation },
                flags.map { analysisByFlagId.getValue(it.id).targetBrightness },
                guessHues.toList(),
                guessSaturations.toList(),
                guessBrightnesses.toList(),
                scores.toList()
            )
        }
    }
}

@Composable
private fun FlagColorQuestion(
    flag: FlagColor,
    analysis: FlagColorAnalysis,
    questionNumber: Int,
    totalQuestions: Int,
    hue: Float,
    saturation: Float,
    brightness: Float,
    revealed: Boolean,
    lastScore: Double?,
    onHueChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val sliderColor = Color.hsv(hue, saturation / 100f, brightness / 100f)
    val aspect = analysis.baseBitmap.width.toFloat() / analysis.baseBitmap.height.toFloat()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(StringsGamesFlagTerritoryBlindmap.flag_question_counter(questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
        Text(flag.flagName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        if (!revealed) {
            RecoloredFlag(
                analysis = analysis,
                sliderColor = sliderColor,
                aspectRatio = aspect,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(StringsGamesFlagTerritoryBlindmap.flag_your_guess, style = MaterialTheme.typography.labelSmall)
                    RecoloredFlag(
                        analysis = analysis,
                        sliderColor = sliderColor,
                        aspectRatio = aspect,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(StringsGamesFlagTerritoryBlindmap.flag_original, style = MaterialTheme.typography.labelSmall)
                    Image(
                        bitmap = analysis.originalBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        LabeledGradientSlider(
            label = StringsGamesFlagTerritoryBlindmap.flag_hue,
            value = hue / 360f,
            enabled = !revealed,
            brush = Brush.horizontalGradient(
                (0..12).map { Color.hsv(it * 30f, 1f, 1f) }
            ),
            onValueChange = { onHueChange(it * 360f) }
        )
        LabeledGradientSlider(
            label = StringsGamesFlagTerritoryBlindmap.flag_saturation,
            value = saturation / 100f,
            enabled = !revealed,
            brush = Brush.horizontalGradient(
                listOf(
                    Color.hsv(hue, 0f, brightness / 100f),
                    Color.hsv(hue, 1f, brightness / 100f)
                )
            ),
            onValueChange = { onSaturationChange(it * 100f) }
        )
        LabeledGradientSlider(
            label = StringsGamesFlagTerritoryBlindmap.flag_brightness,
            value = brightness / 100f,
            enabled = !revealed,
            brush = Brush.horizontalGradient(
                listOf(
                    Color.hsv(hue, saturation / 100f, 0f),
                    Color.hsv(hue, saturation / 100f, 1f)
                )
            ),
            onValueChange = { onBrightnessChange(it * 100f) }
        )

        if (!revealed) {
            Button(onClick = onSubmit) { Text(StringsGamesFlagTerritoryBlindmap.flag_lock_color) }
        } else {
            Text(
                StringsGamesFlagTerritoryBlindmap.flag_target_values(analysis.targetHue, analysis.targetSaturation, analysis.targetBrightness),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                StringsGamesFlagTerritoryBlindmap.flag_score_points(formatFixed(lastScore ?: 0.0, 2)),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) StringsGamesFlagTerritoryBlindmap.flag_see_results else StringsGamesFlagTerritoryBlindmap.flag_next_question)
            }
        }
    }
}

@Composable
private fun RecoloredFlag(
    analysis: FlagColorAnalysis,
    sliderColor: Color,
    aspectRatio: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawImage(analysis.maskBitmap, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
            drawRect(color = sliderColor, blendMode = BlendMode.SrcIn)
        }
        Image(
            bitmap = analysis.baseBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LabeledGradientSlider(
    label: String,
    value: Float,
    enabled: Boolean,
    brush: Brush,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        GradientSlider(value = value, enabled = enabled, brush = brush, onValueChange = onValueChange)
    }
}

@Composable
private fun GradientSlider(
    value: Float,
    brush: Brush,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth by remember { mutableStateOf(0f) }
    val thumbSizePx = with(LocalDensity.current) { 28.dp.toPx() }
    val clamped = value.coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .onGloballyPositioned { trackWidth = it.size.width.toFloat() }
            .pointerInput(enabled, trackWidth) {
                if (enabled && trackWidth > 0f) {
                    detectTapGestures { offset ->
                        onValueChange((offset.x / trackWidth).coerceIn(0f, 1f))
                    }
                }
            }
            .pointerInput(enabled, trackWidth) {
                if (enabled && trackWidth > 0f) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onValueChange((change.position.x / trackWidth).coerceIn(0f, 1f))
                    }
                }
            }
    ) {
        val maxThumbX = (trackWidth - thumbSizePx).coerceAtLeast(0f)
        val thumbX = (clamped * trackWidth - thumbSizePx / 2f).coerceIn(-thumbSizePx / 2f, maxThumbX + thumbSizePx / 2f)
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(thumbX.roundToInt(), 0) }
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
        )
    }
}

@Composable
private fun FlagColorResults(
    flags: List<FlagColor>,
    analysisByFlagId: Map<String, FlagColorAnalysis>,
    guessHues: List<Int>,
    guessSaturations: List<Int>,
    guessBrightnesses: List<Int>,
    scores: List<Double>,
    onBack: () -> Unit
) {
    val total = scores.sum()
    val isPerfect = scores.isNotEmpty() && scores.all { it == 10.0 }
    val (rank, totalPlayers) = rememberLiveRank(PROGRESS_KEY, GameKeys.FLAG_COLOR_MATCH, total, isPerfect)

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
        flags.forEachIndexed { i, flag ->
            val analysis = analysisByFlagId[flag.id]
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(flag.flagName, style = MaterialTheme.typography.bodyMedium)
                Text(StringsGamesFlagTerritoryBlindmap.flag_score_pts(formatFixed(scores.getOrNull(i) ?: 0.0, 2)), style = MaterialTheme.typography.bodySmall)
                if (analysis != null) {
                    val guessColor = Color.hsv(
                        (guessHues.getOrNull(i) ?: analysis.targetHue).toFloat(),
                        (guessSaturations.getOrNull(i) ?: analysis.targetSaturation) / 100f,
                        (guessBrightnesses.getOrNull(i) ?: analysis.targetBrightness) / 100f
                    )
                    val aspect = analysis.baseBitmap.width.toFloat() / analysis.baseBitmap.height.toFloat()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(StringsGamesFlagTerritoryBlindmap.flag_your_guess, style = MaterialTheme.typography.labelSmall)
                            RecoloredFlag(
                                analysis = analysis,
                                sliderColor = guessColor,
                                aspectRatio = aspect,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(StringsGamesFlagTerritoryBlindmap.flag_original, style = MaterialTheme.typography.labelSmall)
                            Image(
                                bitmap = analysis.originalBitmap,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(aspect)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesFlagTerritoryBlindmap.flag_back_to_menu) }
    }
}
