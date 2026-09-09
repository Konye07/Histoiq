package com.yourorg.app.Features.Play.YearGuess

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.HistoricalEvent
import com.yourorg.app.Data.Quiz.formatEraYear
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.RecentlyUsedStore
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Quiz.StatsStore
import com.yourorg.app.Data.Quiz.loadHistoricalEvents
import com.yourorg.app.Data.Quiz.tryLoadImageBitmap
import com.yourorg.app.Data.Quiz.yearGuessScore
import com.yourorg.app.Features.Play.Shared.GuessDistributionAxis
import com.yourorg.app.Features.Play.Shared.HintAction
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.YEAR_GUESS

private fun persist(context: android.content.Context, ids: List<String>, guesses: List<Int>, scores: List<Double>) {
    val obj = JSONObject()
    obj.put("ids", JSONArray(ids))
    obj.put("guesses", JSONArray(guesses))
    obj.put("scores", JSONArray(scores))
    QuizProgressStore.save(context, PROGRESS_KEY, obj)
}

/** Data stores signed years: negative is BCE, positive (or zero) is AD. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearGuessScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<HistoricalEvent>>(emptyList()) }
    var questionIndex by remember { mutableStateOf(0) }
    var guessText by remember { mutableStateOf("") }
    var isBce by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var hintRevealed by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Int>() }
    val scores = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val pool = loadHistoricalEvents(context)
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            events = ids.mapNotNull { id -> pool.find { it.id == id } }
            val storedGuesses = stored.optJSONArray("guesses")
            if (storedGuesses != null) repeat(storedGuesses.length()) { guesses.add(storedGuesses.getInt(it)) }
            val storedScores = stored.optJSONArray("scores")
            if (storedScores != null) repeat(storedScores.length()) { scores.add(storedScores.getDouble(it)) }
            questionIndex = scores.size
        } else {
            val chosen = RecentlyUsedStore.pickDaily(context, PROGRESS_KEY, pool, QUESTIONS_PER_ROUND) { it.id }
            events = chosen
            persist(context, chosen.map { it.id }, emptyList(), emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.SlateBlue)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Guess the Year") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (questionIndex < events.size && !hintRevealed) {
                            HintAction(onWatchAd = { hintRevealed = true })
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
                    events.isEmpty() -> CircularProgressIndicator()
                    questionIndex >= events.size -> YearGuessResults(events, guesses, scores, onBack)
                    else -> {
                        val current = events[questionIndex]
                        YearGuessQuestion(
                            event = current,
                            questionNumber = questionIndex + 1,
                            totalQuestions = events.size,
                            guessText = guessText,
                            isBce = isBce,
                            revealed = revealed,
                            lastScore = scores.getOrNull(questionIndex),
                            lastGuess = guesses.getOrNull(questionIndex),
                            hintRevealed = hintRevealed,
                            onGuessChange = { guessText = it },
                            onEraChange = { isBce = it },
                            onSubmit = {
                                val magnitude = guessText.toIntOrNull() ?: return@YearGuessQuestion
                                val guess = if (isBce) -magnitude else magnitude
                                guesses.add(guess)
                                scores.add(yearGuessScore(guess, current.correctYear, current.difficulty))
                                persist(context, events.map { it.id }, guesses.toList(), scores.toList())
                                revealed = true
                            },
                            onNext = {
                                questionIndex += 1
                                guessText = ""
                                isBce = false
                                revealed = false
                                hintRevealed = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearGuessQuestion(
    event: HistoricalEvent,
    questionNumber: Int,
    totalQuestions: Int,
    guessText: String,
    isBce: Boolean,
    revealed: Boolean,
    lastScore: Double?,
    lastGuess: Int?,
    hintRevealed: Boolean,
    onGuessChange: (String) -> Unit,
    onEraChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var image by remember(event.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(event.id) {
        image = withContext(Dispatchers.Default) { tryLoadImageBitmap(context, event.imageAsset) }
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
        Text(event.event, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (hintRevealed) {
            Surface(
                color = AppColors.Cream,
                contentColor = AppColors.SlateBlue,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hint: ${event.hint ?: "No hint available for this one."}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text("Your guess (year)") },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        EraToggle(isBce = isBce, enabled = !revealed, onIsBceChange = onEraChange)

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessText.toIntOrNull() != null) {
                Text("Submit")
            }
        } else {
            Text("Correct: ${formatEraYear(event.correctYear)}", style = MaterialTheme.typography.bodyLarge)
            Text(
                "You scored %.2f points".format(lastScore ?: 0.0),
                style = MaterialTheme.typography.bodyLarge
            )
            val dateKey = remember { DailyClock.todayKey() }
            val guessDist = remember(event.id) {
                DailyDistribution.yearGuesses(dateKey, event.id, event.correctYear, event.difficulty)
            }
            val minV = remember(event.id, lastGuess) { (guessDist + event.correctYear + listOfNotNull(lastGuess)).min() }
            val maxV = remember(event.id, lastGuess) { (guessDist + event.correctYear + listOfNotNull(lastGuess)).max() }
            val span = (maxV - minV).coerceAtLeast(1)
            GuessDistributionAxis(
                distribution = guessDist.map { (it - minV).toFloat() / span.toFloat() },
                correctPosition = (event.correctYear - minV).toFloat() / span.toFloat(),
                guessPosition = lastGuess?.let { (it - minV).toFloat() / span.toFloat() },
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = "Correct: ${formatEraYear(event.correctYear)}",
                guessLabel = lastGuess?.let { "You: ${formatEraYear(it)}" },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) "See Results" else "Next Question")
            }
        }
    }
}

@Composable
private fun EraToggle(isBce: Boolean, enabled: Boolean, onIsBceChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(false to "AD", true to "BCE").forEach { (bce, label) ->
            val selected = isBce == bce
            Button(
                onClick = { onIsBceChange(bce) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun YearGuessResults(
    events: List<HistoricalEvent>,
    guesses: List<Int>,
    scores: List<Double>,
    onBack: () -> Unit
) {
    val total = scores.sum()
    val isPerfect = scores.isNotEmpty() && scores.all { it == 1000.0 }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ScoreHistoryStore.recordResult(context, PROGRESS_KEY, total)
        StatsStore.recordCompletion(context, PROGRESS_KEY, total, isPerfect)
        if (isPerfect) Haptics.perfect(context)
    }
    val dateKey = remember { DailyClock.todayKey() }
    val distribution = remember { DailyDistribution.finalScores(GameKeys.YEAR_GUESS, dateKey) }
    val (rank, totalPlayers) = remember(total) {
        DailyDistribution.rankAmongPlayers(GameKeys.YEAR_GUESS, dateKey, total, distribution, higherIsBetter = true)
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
            scoreText = "%.2f".format(total),
            isPerfect = isPerfect,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        events.forEachIndexed { i, event ->
            val guessValue = guesses.getOrNull(i)
            val guessDist = remember(event.id) {
                DailyDistribution.yearGuesses(dateKey, event.id, event.correctYear, event.difficulty)
            }
            val minV = remember(event.id, guessValue) { (guessDist + event.correctYear + listOfNotNull(guessValue)).min() }
            val maxV = remember(event.id, guessValue) { (guessDist + event.correctYear + listOfNotNull(guessValue)).max() }
            val span = (maxV - minV).coerceAtLeast(1)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(event.event, style = MaterialTheme.typography.bodyMedium)
                Text("%.2f pts".format(scores.getOrNull(i) ?: 0.0), style = MaterialTheme.typography.bodySmall)
                GuessDistributionAxis(
                    distribution = guessDist.map { (it - minV).toFloat() / span.toFloat() },
                    correctPosition = (event.correctYear - minV).toFloat() / span.toFloat(),
                    guessPosition = guessValue?.let { (it - minV).toFloat() / span.toFloat() },
                    accentColor = MaterialTheme.colorScheme.primary,
                    correctLabel = "Correct: ${formatEraYear(event.correctYear)}",
                    guessLabel = guessValue?.let { "You: ${formatEraYear(it)}" }
                )
            }
        }
        Button(onClick = onBack) { Text("Back to Menu") }
    }
}
