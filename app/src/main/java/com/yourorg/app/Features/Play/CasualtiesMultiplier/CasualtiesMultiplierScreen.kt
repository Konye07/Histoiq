package com.yourorg.app.Features.Play.CasualtiesMultiplier

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.BattleCasualty
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.RecentlyUsedStore
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Quiz.StatsStore
import com.yourorg.app.Data.Quiz.casualtyMultiplier
import com.yourorg.app.Data.Quiz.formatEraYear
import com.yourorg.app.Data.Quiz.loadBattleCasualties
import com.yourorg.app.Data.Quiz.tryLoadImageBitmap
import com.yourorg.app.Features.Play.Shared.GuessDistributionAxis
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ln
import kotlin.math.max
import org.json.JSONArray
import org.json.JSONObject

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.CASUALTIES_MULTIPLIER

private fun persist(context: android.content.Context, ids: List<String>, guesses: List<Long>, multipliers: List<Double>) {
    val obj = JSONObject()
    obj.put("ids", JSONArray(ids))
    obj.put("guesses", JSONArray(guesses))
    obj.put("multipliers", JSONArray(multipliers))
    QuizProgressStore.save(context, PROGRESS_KEY, obj)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasualtiesMultiplierScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var battles by remember { mutableStateOf<List<BattleCasualty>>(emptyList()) }
    var questionIndex by remember { mutableStateOf(0) }
    var guessText by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Long>() }
    val multipliers = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val pool = loadBattleCasualties(context)
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            battles = ids.mapNotNull { id -> pool.find { it.id == id } }
            val storedGuesses = stored.optJSONArray("guesses")
            if (storedGuesses != null) repeat(storedGuesses.length()) { guesses.add(storedGuesses.getLong(it)) }
            val storedMultipliers = stored.optJSONArray("multipliers")
            if (storedMultipliers != null) repeat(storedMultipliers.length()) { multipliers.add(storedMultipliers.getDouble(it)) }
            questionIndex = multipliers.size
        } else {
            val chosen = RecentlyUsedStore.pickDaily(context, PROGRESS_KEY, pool, QUESTIONS_PER_ROUND) { it.id }
            battles = chosen
            persist(context, chosen.map { it.id }, emptyList(), emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.Maroon)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Casualty Count") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                when {
                    battles.isEmpty() -> CircularProgressIndicator()
                    questionIndex >= battles.size -> CasualtiesResults(battles, guesses, multipliers, onBack)
                    else -> {
                        val current = battles[questionIndex]
                        CasualtiesQuestion(
                            battle = current,
                            questionNumber = questionIndex + 1,
                            totalQuestions = battles.size,
                            guessText = guessText,
                            revealed = revealed,
                            lastMultiplier = multipliers.getOrNull(questionIndex),
                            onGuessChange = { guessText = it },
                            onSubmit = {
                                val guess = guessText.toLongOrNull() ?: return@CasualtiesQuestion
                                guesses.add(guess)
                                multipliers.add(casualtyMultiplier(guess, current.correctCasualties))
                                persist(context, battles.map { it.id }, guesses.toList(), multipliers.toList())
                                revealed = true
                            },
                            onNext = {
                                questionIndex += 1
                                guessText = ""
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
private fun CasualtiesQuestion(
    battle: BattleCasualty,
    questionNumber: Int,
    totalQuestions: Int,
    guessText: String,
    revealed: Boolean,
    lastMultiplier: Double?,
    onGuessChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var image by remember(battle.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(battle.id) {
        image = withContext(Dispatchers.Default) { tryLoadImageBitmap(context, battle.imageAsset) }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Question $questionNumber / $totalQuestions", style = MaterialTheme.typography.labelLarge)
        image?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Text(battle.battle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(formatEraYear(battle.year), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text("Your guess (casualties)") },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessText.toLongOrNull() != null) {
                Text("Submit")
            }
        } else {
            Text("Actual casualties: ${battle.correctCasualties}", style = MaterialTheme.typography.bodyLarge)
            Text(
                "You earned x%.2f".format(lastMultiplier ?: 1.0),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) "See Results" else "Next Question")
            }
        }
    }
}

@Composable
private fun CasualtiesResults(
    battles: List<BattleCasualty>,
    guesses: List<Long>,
    multipliers: List<Double>,
    onBack: () -> Unit
) {
    val total = multipliers.fold(1.0) { acc, m -> acc * m }
    val isPerfect = multipliers.isNotEmpty() && multipliers.all { it == 1.0 }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ScoreHistoryStore.recordResult(context, PROGRESS_KEY, total)
        StatsStore.recordCompletion(context, PROGRESS_KEY, total, isPerfect)
        if (isPerfect) Haptics.perfect(context)
    }
    val dateKey = remember { DailyClock.todayKey() }
    val finalDistribution = remember { DailyDistribution.finalScores(GameKeys.CASUALTIES_MULTIPLIER, dateKey) }
    val percentile = remember(total) { DailyDistribution.percentile(total, finalDistribution, higherIsBetter = false) }
    val multiplierDist = remember { DailyDistribution.casualtyMultipliers(dateKey) }
    val multiplierMax = remember(total) { max(multiplierDist.maxOrNull() ?: 1.0, total) * 1.05 }
    fun multiplierPos(v: Double): Float =
        ((v - 1.0) / (multiplierMax - 1.0).coerceAtLeast(0.01)).toFloat().coerceIn(0f, 1f)

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ResultsHeader(
            scoreText = "x%.2f".format(total),
            isPerfect = isPerfect,
            rankingText = rankingText(percentile),
            accentColor = MaterialTheme.colorScheme.primary
        )
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Final multiplier (1.00x is a perfect run)", style = MaterialTheme.typography.bodyMedium)
            GuessDistributionAxis(
                distribution = multiplierDist.map { multiplierPos(it) },
                correctPosition = null,
                guessPosition = multiplierPos(total),
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = null,
                guessLabel = "You: x%.2f".format(total)
            )
        }
        HorizontalDivider()
        battles.forEachIndexed { i, battle ->
            val guessValue = guesses.getOrNull(i)
            val guessDist = remember(battle.id) {
                DailyDistribution.casualtyGuesses(dateKey, battle.id, battle.correctCasualties)
            }
            val logMin = remember(battle.id, guessValue) {
                ln((guessDist + battle.correctCasualties + listOfNotNull(guessValue)).min().coerceAtLeast(1).toDouble())
            }
            val logMax = remember(battle.id, guessValue) {
                ln((guessDist + battle.correctCasualties + listOfNotNull(guessValue)).max().coerceAtLeast(1).toDouble())
            }
            val logSpan = (logMax - logMin).coerceAtLeast(0.01)
            fun logPos(v: Long): Float = ((ln(v.coerceAtLeast(1).toDouble()) - logMin) / logSpan).toFloat().coerceIn(0f, 1f)

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(battle.battle, style = MaterialTheme.typography.bodyMedium)
                Text("x%.2f".format(multipliers.getOrNull(i) ?: 1.0), style = MaterialTheme.typography.bodySmall)
                GuessDistributionAxis(
                    distribution = guessDist.map { logPos(it) },
                    correctPosition = logPos(battle.correctCasualties),
                    guessPosition = guessValue?.let { logPos(it) },
                    accentColor = MaterialTheme.colorScheme.primary,
                    correctLabel = "Actual: ${battle.correctCasualties}",
                    guessLabel = guessValue?.let { "You: $it" }
                )
            }
        }
        Button(onClick = onBack) { Text("Back to Menu") }
    }
}
