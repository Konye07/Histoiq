package com.yourorg.app.Features.Play.FlagColorMatch

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.FlagColor
import com.yourorg.app.Data.Quiz.FlagColorAnalysis
import com.yourorg.app.Data.Quiz.FlagColorCandidate
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.RecentlyUsedStore
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Quiz.StatsStore
import com.yourorg.app.Data.Quiz.buildFlagColorAnalysis
import com.yourorg.app.Data.Quiz.colorMatchScore
import com.yourorg.app.Data.Quiz.findFlagColorCandidates
import com.yourorg.app.Data.Quiz.loadFlagColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = "flag_color_match"

private fun persist(
    context: android.content.Context,
    ids: List<String>,
    targetHues: List<Int>,
    targetSaturations: List<Int>,
    targetBrightnesses: List<Int>,
    hues: List<Int>,
    saturations: List<Int>,
    brightnesses: List<Int>,
    scores: List<Double>
) {
    val obj = JSONObject()
    obj.put("ids", JSONArray(ids))
    obj.put("targetHues", JSONArray(targetHues))
    obj.put("targetSaturations", JSONArray(targetSaturations))
    obj.put("targetBrightnesses", JSONArray(targetBrightnesses))
    obj.put("hues", JSONArray(hues))
    obj.put("saturations", JSONArray(saturations))
    obj.put("brightnesses", JSONArray(brightnesses))
    obj.put("scores", JSONArray(scores))
    QuizProgressStore.save(context, PROGRESS_KEY, obj)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagColorMatchScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
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

    LaunchedEffect(Unit) {
        val pool = loadFlagColors(context)
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("targetHues") }
        val chosen: List<FlagColor>
        val targets: List<FlagColorCandidate>
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            chosen = ids.mapNotNull { id -> pool.find { it.id == id } }
            val targetHueArr = stored.getJSONArray("targetHues")
            val targetSatArr = stored.getJSONArray("targetSaturations")
            val targetBrightArr = stored.getJSONArray("targetBrightnesses")
            targets = List(targetHueArr.length()) { i ->
                FlagColorCandidate(targetHueArr.getInt(i), targetSatArr.getInt(i), targetBrightArr.getInt(i))
            }
            stored.optJSONArray("hues")?.let { arr -> repeat(arr.length()) { guessHues.add(arr.getInt(it)) } }
            stored.optJSONArray("saturations")?.let { arr -> repeat(arr.length()) { guessSaturations.add(arr.getInt(it)) } }
            stored.optJSONArray("brightnesses")?.let { arr -> repeat(arr.length()) { guessBrightnesses.add(arr.getInt(it)) } }
            stored.optJSONArray("scores")?.let { arr -> repeat(arr.length()) { scores.add(arr.getDouble(it)) } }
            questionIndex = scores.size
        } else {
            val newlyChosen = RecentlyUsedStore.pickDaily(context, PROGRESS_KEY, pool, QUESTIONS_PER_ROUND) { it.id }
            chosen = newlyChosen
            targets = withContext(Dispatchers.Default) {
                newlyChosen.map { flag -> findFlagColorCandidates(context, flag.imageAsset).random() }
            }
            persist(
                context,
                chosen.map { it.id },
                targets.map { it.hue },
                targets.map { it.saturation },
                targets.map { it.brightness },
                emptyList(), emptyList(), emptyList(), emptyList()
            )
        }
        flags = chosen
        analysisByFlagId = withContext(Dispatchers.Default) {
            chosen.indices.associate { i -> chosen[i].id to buildFlagColorAnalysis(context, chosen[i].imageAsset, targets[i]) }
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.OliveDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match the Flag Color") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                flags.isEmpty() || analysisByFlagId.size < flags.size -> CircularProgressIndicator()
                questionIndex >= flags.size -> FlagColorResults(
                    flags, analysisByFlagId, guessHues, guessSaturations, guessBrightnesses, scores, onBack
                )
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
                            persist(
                                context,
                                flags.map { it.id },
                                flags.map { analysisByFlagId.getValue(it.id).targetHue },
                                flags.map { analysisByFlagId.getValue(it.id).targetSaturation },
                                flags.map { analysisByFlagId.getValue(it.id).targetBrightness },
                                guessHues.toList(),
                                guessSaturations.toList(),
                                guessBrightnesses.toList(),
                                scores.toList()
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
        Text("Question $questionNumber / $totalQuestions", style = MaterialTheme.typography.labelLarge)
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
                    Text("Your Guess", style = MaterialTheme.typography.labelSmall)
                    RecoloredFlag(
                        analysis = analysis,
                        sliderColor = sliderColor,
                        aspectRatio = aspect,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Original", style = MaterialTheme.typography.labelSmall)
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
            label = "Hue",
            value = hue / 360f,
            enabled = !revealed,
            brush = Brush.horizontalGradient(
                (0..12).map { Color.hsv(it * 30f, 1f, 1f) }
            ),
            onValueChange = { onHueChange(it * 360f) }
        )
        LabeledGradientSlider(
            label = "Saturation",
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
            label = "Brightness",
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
            Button(onClick = onSubmit) { Text("Lock Color ✓") }
        } else {
            Text(
                "Target: H${analysis.targetHue} S${analysis.targetSaturation} B${analysis.targetBrightness}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "You scored %.2f points".format(lastScore ?: 0.0),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) "See Results" else "Next Question")
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
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ScoreHistoryStore.recordResult(context, PROGRESS_KEY, total)
        StatsStore.recordCompletion(context, PROGRESS_KEY, total, isPerfect)
        if (isPerfect) Haptics.perfect(context)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Final Score", style = MaterialTheme.typography.titleLarge)
        Text(
            "%.2f".format(total),
            style = MaterialTheme.typography.displayMedium,
            color = if (isPerfect) AppColors.Success else MaterialTheme.colorScheme.onBackground
        )
        Text("Ranking against today's participants isn't available yet.", style = MaterialTheme.typography.bodySmall)
        HorizontalDivider()
        flags.forEachIndexed { i, flag ->
            val target = analysisByFlagId[flag.id]
            Column(Modifier.fillMaxWidth()) {
                Text(flag.flagName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Your guess: H${guessHues.getOrNull(i) ?: "-"} S${guessSaturations.getOrNull(i) ?: "-"} " +
                        "B${guessBrightnesses.getOrNull(i) ?: "-"}  ·  " +
                        "Correct: H${target?.targetHue ?: "-"} S${target?.targetSaturation ?: "-"} B${target?.targetBrightness ?: "-"}  ·  " +
                        "%.2f pts".format(scores.getOrNull(i) ?: 0.0),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(onClick = onBack) { Text("Back to Menu") }
    }
}
