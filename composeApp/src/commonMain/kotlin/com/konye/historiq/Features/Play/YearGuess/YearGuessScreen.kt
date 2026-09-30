package com.konye.historiq.Features.Play.YearGuess

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsGamesYearguessCasualties
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.GameBackgroundArt
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.DailyDistribution
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.HistoricalEvent
import com.konye.historiq.Data.Quiz.formatEraYear
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.getDoubleList
import com.konye.historiq.Data.Quiz.getIntList
import com.konye.historiq.Data.Quiz.loadHistoricalEvents
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.resumeOrStartQuiz
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import com.konye.historiq.Data.Quiz.yearGuessScore
import com.konye.historiq.Features.Play.Shared.HintAction
import com.konye.historiq.Features.Play.Shared.QuestionResultCard
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

/** "Correct: 1969 AD" -> guess/correct/landed-vs-correct pieces shared by the reveal and results list. */
@Composable
private fun YearResult(
    correctYear: Int,
    guess: Int,
    score: Double,
    guessDist: List<Int>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val minV = (guessDist + correctYear + listOf(guess)).min()
    val maxV = (guessDist + correctYear + listOf(guess)).max()
    val span = (maxV - minV).coerceAtLeast(1)
    val diff = guess - correctYear
    val isExact = diff == 0
    val landedText = when {
        isExact -> StringsHomeShared.results_landed_exact
        diff > 0 -> StringsGamesYearguessCasualties.yearguess_landed_too_high(diff)
        else -> StringsGamesYearguessCasualties.yearguess_landed_too_low(-diff)
    }
    val otherDistances = guessDist.map { abs(it - correctYear).toDouble() }
    val closerPct = DailyDistribution.closerThanPercent(abs(diff).toDouble(), otherDistances)

    QuestionResultCard(
        accentColor = accentColor,
        guessedLabel = StringsHomeShared.results_you_guessed_label,
        guessedValue = formatEraYear(guess),
        answerLabel = StringsHomeShared.results_answer_label,
        answerValue = formatEraYear(correctYear),
        landedText = landedText,
        landedIsExact = isExact,
        closerThanText = StringsHomeShared.results_closer_than_percent(closerPct),
        distribution = guessDist.map { (it - minV).toFloat() / span.toFloat() },
        correctPosition = (correctYear - minV).toFloat() / span.toFloat(),
        guessPosition = (guess - minV).toFloat() / span.toFloat(),
        minLabel = formatEraYear(minV),
        maxLabel = formatEraYear(maxV),
        scoreBadgeText = StringsGamesYearguessCasualties.yearguess_points_badge(formatFixed(score, 0)),
        modifier = modifier
    )
}

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.YEAR_GUESS

private suspend fun persist(ids: List<String>, guesses: List<Int>, scores: List<Double>) {
    QuizProgressStore.save(PROGRESS_KEY, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("guesses", JsonArray(guesses.map { JsonPrimitive(it) }))
        put("scores", JsonArray(scores.map { JsonPrimitive(it) }))
    })
}

/** Data stores signed years: negative is BCE, positive (or zero) is AD. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearGuessScreen(onBack: () -> Unit = {}) {
    var events by remember { mutableStateOf<List<HistoricalEvent>>(emptyList()) }
    var questionIndex by remember { mutableStateOf(0) }
    var guessText by remember { mutableStateOf("") }
    var isBce by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var hintRevealed by remember { mutableStateOf(false) }
    var resumedAlreadyDone by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Int>() }
    val scores = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val resume = resumeOrStartQuiz(PROGRESS_KEY, QUESTIONS_PER_ROUND, { it.id }, ::loadHistoricalEvents)
        events = resume.questions
        resume.resumedState?.let { stored ->
            guesses.addAll(stored.getIntList("guesses"))
            scores.addAll(stored.getDoubleList("scores"))
            questionIndex = scores.size
            resumedAlreadyDone = questionIndex >= events.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.SlateBlue)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(StringsGamesYearguessCasualties.yearguess_title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesYearguessCasualties.yearguess_back_cd)
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
                GameBackgroundArt("images/backgrounds/bg_year_guess.png", MaterialTheme.colorScheme.onBackground)
                when {
                    events.isEmpty() -> HistoriqLoadingIndicator()
                    questionIndex >= events.size -> {
                        if (resumedAlreadyDone) {
                            YearGuessResults(events, guesses, scores, onBack)
                        } else {
                            AdGatedResults { YearGuessResults(events, guesses, scores, onBack) }
                        }
                    }
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
                                val score = yearGuessScore(guess, current.correctYear, current.difficulty)
                                scores.add(score)
                                if (score >= 550.0) { Haptics.correct(); BubbleSoundPlayer.playCorrectChirp() } else { Haptics.wrong(); BubbleSoundPlayer.playWrongTone() }
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
            persist(events.map { it.id }, guesses.toList(), scores.toList())
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
    var image by remember(event.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(event.id) {
        image = withContext(Dispatchers.Default) { tryLoadImageBitmap(event.imageAsset) }
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
            StringsGamesYearguessCasualties.yearguess_question_counter(questionNumber, totalQuestions),
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
                color = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    StringsGamesYearguessCasualties.yearguess_hint_prefix(event.hint ?: StringsGamesYearguessCasualties.yearguess_hint_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text(StringsGamesYearguessCasualties.yearguess_guess_label) },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        EraToggle(isBce = isBce, enabled = !revealed, onIsBceChange = onEraChange)

        AnimatedContent(
            targetState = revealed,
            transitionSpec = { (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.96f)) togetherWith fadeOut(tween(120)) }
        ) { isRevealed ->
            if (!isRevealed) {
                Button(onClick = onSubmit, enabled = guessText.toIntOrNull() != null) {
                    Text(StringsGamesYearguessCasualties.yearguess_submit)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    var guessDist by remember(event.id) { mutableStateOf<List<Int>>(emptyList()) }
                    LaunchedEffect(event.id) {
                        guessDist = DailyDistribution.yearGuesses(GameKeys.YEAR_GUESS, DailyClock.todayKey(), event.id)
                    }
                    if (!hintRevealed) {
                        event.hint?.let {
                            Text(
                                StringsGamesYearguessCasualties.yearguess_hint_prefix(it),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.7f)
                            )
                        }
                    }
                    lastGuess?.let { guess ->
                        YearResult(
                            correctYear = event.correctYear,
                            guess = guess,
                            score = lastScore ?: 0.0,
                            guessDist = guessDist,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(onClick = onNext) {
                        Text(if (questionNumber == totalQuestions) StringsGamesYearguessCasualties.yearguess_see_results else StringsGamesYearguessCasualties.yearguess_next_question)
                    }
                }
            }
        }
    }
}

@Composable
private fun EraToggle(isBce: Boolean, enabled: Boolean, onIsBceChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(false to StringsGamesYearguessCasualties.yearguess_era_ad, true to StringsGamesYearguessCasualties.yearguess_era_bce).forEach { (bce, label) ->
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
    val (rank, totalPlayers, finalDistribution, topPercent) = rememberLiveRank(PROGRESS_KEY, GameKeys.YEAR_GUESS, total, isPerfect)
    var guessDistByEventId by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) }

    val maxPossible = scores.size * 1000.0
    fun scorePos(v: Double): Float = (v / maxPossible).toFloat().coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
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
        events.forEachIndexed { i, event ->
            val guessDist = guessDistByEventId[event.id] ?: emptyList()
            guesses.getOrNull(i)?.let { guessValue ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.event, style = MaterialTheme.typography.bodyMedium)
                    event.hint?.let {
                        Text(
                            StringsGamesYearguessCasualties.yearguess_hint_prefix(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.7f)
                        )
                    }
                    YearResult(
                        correctYear = event.correctYear,
                        guess = guessValue,
                        score = scores.getOrNull(i) ?: 0.0,
                        guessDist = guessDist,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesYearguessCasualties.yearguess_back_to_menu) }
    }
}
