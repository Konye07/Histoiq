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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.R
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.DailyQuizRepository
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.HistoricalEvent
import com.yourorg.app.Data.Quiz.formatEraYear
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.loadHistoricalEvents
import com.yourorg.app.Data.Quiz.recordAndRank
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

private suspend fun persist(context: android.content.Context, ids: List<String>, guesses: List<Int>, scores: List<Double>) {
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
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            val rows = loadHistoricalEvents(ids)
            events = ids.mapNotNull { id -> rows.find { it.id == id } }
            val storedGuesses = stored.optJSONArray("guesses")
            if (storedGuesses != null) repeat(storedGuesses.length()) { guesses.add(storedGuesses.getInt(it)) }
            val storedScores = stored.optJSONArray("scores")
            if (storedScores != null) repeat(storedScores.length()) { scores.add(storedScores.getDouble(it)) }
            questionIndex = scores.size
        } else {
            val ids = DailyQuizRepository.getOrCreate(PROGRESS_KEY, DailyClock.todayKey(), QUESTIONS_PER_ROUND)
            val rows = loadHistoricalEvents(ids)
            events = ids.mapNotNull { id -> rows.find { it.id == id } }
            persist(context, ids, emptyList(), emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.SlateBlue)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.yearguess_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.yearguess_back_cd))
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

    LaunchedEffect(guesses.size) {
        if (guesses.size in 1..events.size && guesses.size == scores.size) {
            persist(context, events.map { it.id }, guesses.toList(), scores.toList())
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
        Text(
            stringResource(R.string.yearguess_question_counter, questionNumber, totalQuestions),
            style = MaterialTheme.typography.labelLarge
        )
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
                    stringResource(R.string.yearguess_hint_prefix, event.hint ?: stringResource(R.string.yearguess_hint_unavailable)),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text(stringResource(R.string.yearguess_guess_label)) },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        EraToggle(isBce = isBce, enabled = !revealed, onIsBceChange = onEraChange)

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessText.toIntOrNull() != null) {
                Text(stringResource(R.string.yearguess_submit))
            }
        } else {
            Text(stringResource(R.string.yearguess_correct, formatEraYear(event.correctYear)), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.yearguess_score_points, "%.2f".format(lastScore ?: 0.0)),
                style = MaterialTheme.typography.bodyLarge
            )
            var guessDist by remember(event.id) { mutableStateOf<List<Int>>(emptyList()) }
            LaunchedEffect(event.id) {
                guessDist = DailyDistribution.yearGuesses(GameKeys.YEAR_GUESS, DailyClock.todayKey(), event.id)
            }
            val minV = (guessDist + event.correctYear + listOfNotNull(lastGuess)).min()
            val maxV = (guessDist + event.correctYear + listOfNotNull(lastGuess)).max()
            val span = (maxV - minV).coerceAtLeast(1)
            GuessDistributionAxis(
                distribution = guessDist.map { (it - minV).toFloat() / span.toFloat() },
                correctPosition = (event.correctYear - minV).toFloat() / span.toFloat(),
                guessPosition = lastGuess?.let { (it - minV).toFloat() / span.toFloat() },
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = stringResource(R.string.yearguess_correct, formatEraYear(event.correctYear)),
                guessLabel = lastGuess?.let { stringResource(R.string.yearguess_you_label, formatEraYear(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) stringResource(R.string.yearguess_see_results) else stringResource(R.string.yearguess_next_question))
            }
        }
    }
}

@Composable
private fun EraToggle(isBce: Boolean, enabled: Boolean, onIsBceChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(false to stringResource(R.string.yearguess_era_ad), true to stringResource(R.string.yearguess_era_bce)).forEach { (bce, label) ->
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
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    var guessDistByEventId by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val result = recordAndRank(context, PROGRESS_KEY, GameKeys.YEAR_GUESS, total, isPerfect)
        rank = result.rank
        totalPlayers = result.totalPlayers

        val dateKey = DailyClock.todayKey()
        guessDistByEventId = events.associate { it.id to DailyDistribution.yearGuesses(GameKeys.YEAR_GUESS, dateKey, it.id) }
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
            scoreText = "%.2f".format(total),
            isPerfect = isPerfect,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        events.forEachIndexed { i, event ->
            val guessValue = guesses.getOrNull(i)
            val guessDist = guessDistByEventId[event.id] ?: emptyList()
            val minV = (guessDist + event.correctYear + listOfNotNull(guessValue)).min()
            val maxV = (guessDist + event.correctYear + listOfNotNull(guessValue)).max()
            val span = (maxV - minV).coerceAtLeast(1)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(event.event, style = MaterialTheme.typography.bodyMedium)
                Text("%.2f pts".format(scores.getOrNull(i) ?: 0.0), style = MaterialTheme.typography.bodySmall)
                GuessDistributionAxis(
                    distribution = guessDist.map { (it - minV).toFloat() / span.toFloat() },
                    correctPosition = (event.correctYear - minV).toFloat() / span.toFloat(),
                    guessPosition = guessValue?.let { (it - minV).toFloat() / span.toFloat() },
                    accentColor = MaterialTheme.colorScheme.primary,
                    correctLabel = stringResource(R.string.yearguess_correct, formatEraYear(event.correctYear)),
                    guessLabel = guessValue?.let { stringResource(R.string.yearguess_you_label, formatEraYear(it)) }
                )
            }
        }
        Button(onClick = onBack) { Text(stringResource(R.string.yearguess_back_to_menu)) }
    }
}
