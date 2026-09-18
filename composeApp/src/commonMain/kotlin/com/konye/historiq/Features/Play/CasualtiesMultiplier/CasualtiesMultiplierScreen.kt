package com.konye.historiq.Features.Play.CasualtiesMultiplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Localization.StringsGamesYearguessCasualties
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.BattleCasualty
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.DailyDistribution
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.casualtyMultiplier
import com.konye.historiq.Data.Quiz.formatEraYear
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.getDoubleList
import com.konye.historiq.Data.Quiz.getLongList
import com.konye.historiq.Data.Quiz.loadBattleCasualties
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.resumeOrStartQuiz
import com.konye.historiq.Features.Play.Shared.GuessDistributionAxis
import com.konye.historiq.Features.Play.Shared.HintAction
import com.konye.historiq.Features.Play.Shared.QuestionResultCard
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.Features.Play.Shared.rankingText
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import kotlin.math.ln
import kotlin.math.max
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

/** "Actual: 20,000" -> guess/answer/landed-vs-answer pieces shared by the reveal and results list. */
@Composable
private fun CasualtiesResult(
    correctCasualties: Long,
    guess: Long,
    guessDist: List<Long>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val logMin = ln((guessDist + correctCasualties + listOf(guess)).min().coerceAtLeast(1).toDouble())
    val logMax = ln((guessDist + correctCasualties + listOf(guess)).max().coerceAtLeast(1).toDouble())
    val logSpan = (logMax - logMin).coerceAtLeast(0.01)
    fun logPos(v: Long): Float = ((ln(v.coerceAtLeast(1).toDouble()) - logMin) / logSpan).toFloat().coerceIn(0f, 1f)

    val multiplier = casualtyMultiplier(guess, correctCasualties)
    val isExact = guess == correctCasualties
    val landedText = when {
        isExact -> StringsHomeShared.results_landed_exact
        guess > correctCasualties -> StringsGamesYearguessCasualties.casualties_landed_too_high("x" + formatFixed(multiplier, 2))
        else -> StringsGamesYearguessCasualties.casualties_landed_too_low("x" + formatFixed(multiplier, 2))
    }
    val otherDistances = guessDist.map { casualtyMultiplier(it, correctCasualties) }
    val closerPct = DailyDistribution.closerThanPercent(multiplier, otherDistances)

    QuestionResultCard(
        accentColor = accentColor,
        guessedLabel = StringsHomeShared.results_you_guessed_label,
        guessedValue = formatThousands(guess),
        answerLabel = StringsHomeShared.results_answer_label,
        answerValue = formatThousands(correctCasualties),
        landedText = landedText,
        landedIsExact = isExact,
        closerThanText = StringsHomeShared.results_closer_than_percent(closerPct),
        distribution = guessDist.map { logPos(it) },
        correctPosition = logPos(correctCasualties),
        guessPosition = logPos(guess),
        minLabel = formatThousands((guessDist + correctCasualties + listOf(guess)).min()),
        maxLabel = formatThousands((guessDist + correctCasualties + listOf(guess)).max()),
        modifier = modifier
    )
}

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.CASUALTIES_MULTIPLIER

private suspend fun persist(ids: List<String>, guesses: List<Long>, multipliers: List<Double>) {
    QuizProgressStore.save(PROGRESS_KEY, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("guesses", JsonArray(guesses.map { JsonPrimitive(it) }))
        put("multipliers", JsonArray(multipliers.map { JsonPrimitive(it) }))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasualtiesMultiplierScreen(onBack: () -> Unit = {}) {
    var battles by remember { mutableStateOf<List<BattleCasualty>>(emptyList()) }
    var questionIndex by remember { mutableStateOf(0) }
    var guessText by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }
    var hintRevealed by remember { mutableStateOf(false) }
    var resumedAlreadyDone by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Long>() }
    val multipliers = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val resume = resumeOrStartQuiz(PROGRESS_KEY, QUESTIONS_PER_ROUND, { it.id }, ::loadBattleCasualties)
        battles = resume.questions
        resume.resumedState?.let { stored ->
            guesses.addAll(stored.getLongList("guesses"))
            multipliers.addAll(stored.getDoubleList("multipliers"))
            questionIndex = multipliers.size
            resumedAlreadyDone = questionIndex >= battles.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.Maroon)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(StringsGamesYearguessCasualties.casualties_title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesYearguessCasualties.casualties_back_cd)
                        }
                    },
                    actions = {
                        if (questionIndex < battles.size && !hintRevealed) {
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
                    battles.isEmpty() -> CircularProgressIndicator()
                    questionIndex >= battles.size -> {
                        if (resumedAlreadyDone) {
                            CasualtiesResults(battles, guesses, multipliers, onBack)
                        } else {
                            AdGatedResults { CasualtiesResults(battles, guesses, multipliers, onBack) }
                        }
                    }
                    else -> {
                        val current = battles[questionIndex]
                        CasualtiesQuestion(
                            battle = current,
                            questionNumber = questionIndex + 1,
                            totalQuestions = battles.size,
                            guessText = guessText,
                            revealed = revealed,
                            lastMultiplier = multipliers.getOrNull(questionIndex),
                            lastGuess = guesses.getOrNull(questionIndex),
                            hintRevealed = hintRevealed,
                            onGuessChange = { guessText = it },
                            onSubmit = {
                                val guess = guessText.toLongOrNull() ?: return@CasualtiesQuestion
                                guesses.add(guess)
                                multipliers.add(casualtyMultiplier(guess, current.correctCasualties))
                                revealed = true
                            },
                            onNext = {
                                questionIndex += 1
                                guessText = ""
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
        if (guesses.size in 1..battles.size && guesses.size == multipliers.size) {
            persist(battles.map { it.id }, guesses.toList(), multipliers.toList())
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
    lastGuess: Long?,
    hintRevealed: Boolean,
    onGuessChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(StringsGamesYearguessCasualties.casualties_question_counter(questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
        Text(battle.battle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(formatEraYear(battle.year), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (hintRevealed) {
            Surface(
                color = AppColors.Cream,
                contentColor = AppColors.Maroon,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    StringsGamesYearguessCasualties.casualties_hint_prefix(battle.hint ?: StringsGamesYearguessCasualties.casualties_hint_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text(StringsGamesYearguessCasualties.casualties_guess_label) },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessText.toLongOrNull() != null) {
                Text(StringsGamesYearguessCasualties.casualties_submit)
            }
        } else {
            var guessDist by remember(battle.id) { mutableStateOf<List<Long>>(emptyList()) }
            LaunchedEffect(battle.id) {
                guessDist = DailyDistribution.casualtyGuesses(GameKeys.CASUALTIES_MULTIPLIER, DailyClock.todayKey(), battle.id)
            }
            if (!hintRevealed) {
                battle.hint?.let {
                    Text(
                        StringsGamesYearguessCasualties.casualties_hint_prefix(it),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            lastGuess?.let { guess ->
                CasualtiesResult(
                    correctCasualties = battle.correctCasualties,
                    guess = guess,
                    guessDist = guessDist,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) StringsGamesYearguessCasualties.casualties_see_results else StringsGamesYearguessCasualties.casualties_next_question)
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
    val (rank, totalPlayers, finalDistribution) = rememberLiveRank(PROGRESS_KEY, GameKeys.CASUALTIES_MULTIPLIER, total, isPerfect, higherIsBetter = false)
    var guessDistByBattleId by remember { mutableStateOf<Map<String, List<Long>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val dateKey = DailyClock.todayKey()
        guessDistByBattleId = battles.associate {
            it.id to DailyDistribution.casualtyGuesses(GameKeys.CASUALTIES_MULTIPLIER, dateKey, it.id)
        }
    }

    val multiplierMax = max(finalDistribution.maxOrNull() ?: 1.0, total) * 1.05
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
            gameKey = PROGRESS_KEY,
            scoreText = "x" + formatFixed(total, 2),
            isPerfect = isPerfect,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary
        )
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(StringsGamesYearguessCasualties.casualties_final_multiplier_caption, style = MaterialTheme.typography.bodyMedium)
            GuessDistributionAxis(
                distribution = finalDistribution.map { multiplierPos(it) },
                correctPosition = null,
                guessPosition = multiplierPos(total),
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = null,
                guessLabel = StringsGamesYearguessCasualties.casualties_you_label("x" + formatFixed(total, 2))
            )
        }
        HorizontalDivider()
        battles.forEachIndexed { i, battle ->
            val guessDist = guessDistByBattleId[battle.id] ?: emptyList()
            guesses.getOrNull(i)?.let { guessValue ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(battle.battle, style = MaterialTheme.typography.bodyMedium)
                    battle.hint?.let {
                        Text(
                            StringsGamesYearguessCasualties.casualties_hint_prefix(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    CasualtiesResult(
                        correctCasualties = battle.correctCasualties,
                        guess = guessValue,
                        guessDist = guessDist,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesYearguessCasualties.casualties_back_to_menu) }
    }
}
