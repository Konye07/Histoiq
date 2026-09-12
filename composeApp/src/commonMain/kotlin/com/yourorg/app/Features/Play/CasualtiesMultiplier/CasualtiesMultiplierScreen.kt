package com.yourorg.app.Features.Play.CasualtiesMultiplier

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
import com.yourorg.app.Core.Formatting.formatFixed
import com.yourorg.app.Core.Localization.StringsGamesYearguessCasualties
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.BattleCasualty
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.casualtyMultiplier
import com.yourorg.app.Data.Quiz.formatEraYear
import com.yourorg.app.Data.Quiz.formatThousands
import com.yourorg.app.Data.Quiz.getDoubleList
import com.yourorg.app.Data.Quiz.getLongList
import com.yourorg.app.Data.Quiz.loadBattleCasualties
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Data.Quiz.resumeOrStartQuiz
import com.yourorg.app.Features.Play.Shared.GuessDistributionAxis
import com.yourorg.app.Features.Play.Shared.HintAction
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_arrow_back
import kotlin.math.ln
import kotlin.math.max
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

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
    val guesses = remember { mutableStateListOf<Long>() }
    val multipliers = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val resume = resumeOrStartQuiz(PROGRESS_KEY, QUESTIONS_PER_ROUND, { it.id }, ::loadBattleCasualties)
        battles = resume.questions
        resume.resumedState?.let { stored ->
            guesses.addAll(stored.getLongList("guesses"))
            multipliers.addAll(stored.getDoubleList("multipliers"))
            questionIndex = multipliers.size
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
            Text(StringsGamesYearguessCasualties.casualties_actual_casualties(formatThousands(battle.correctCasualties)), style = MaterialTheme.typography.bodyLarge)
            Text(
                StringsGamesYearguessCasualties.casualties_earned_multiplier("x" + formatFixed(lastMultiplier ?: 1.0, 2)),
                style = MaterialTheme.typography.bodyLarge
            )
            var guessDist by remember(battle.id) { mutableStateOf<List<Long>>(emptyList()) }
            LaunchedEffect(battle.id) {
                guessDist = DailyDistribution.casualtyGuesses(GameKeys.CASUALTIES_MULTIPLIER, DailyClock.todayKey(), battle.id)
            }
            val logMin = ln((guessDist + battle.correctCasualties + listOfNotNull(lastGuess)).min().coerceAtLeast(1).toDouble())
            val logMax = ln((guessDist + battle.correctCasualties + listOfNotNull(lastGuess)).max().coerceAtLeast(1).toDouble())
            val logSpan = (logMax - logMin).coerceAtLeast(0.01)
            fun logPos(v: Long): Float = ((ln(v.coerceAtLeast(1).toDouble()) - logMin) / logSpan).toFloat().coerceIn(0f, 1f)
            GuessDistributionAxis(
                distribution = guessDist.map { logPos(it) },
                correctPosition = logPos(battle.correctCasualties),
                guessPosition = lastGuess?.let { logPos(it) },
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = StringsGamesYearguessCasualties.casualties_actual_label(formatThousands(battle.correctCasualties)),
                guessLabel = lastGuess?.let { StringsGamesYearguessCasualties.casualties_you_label(formatThousands(it)) },
                modifier = Modifier.fillMaxWidth()
            )
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
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    var finalDistribution by remember { mutableStateOf<List<Double>>(emptyList()) }
    var guessDistByBattleId by remember { mutableStateOf<Map<String, List<Long>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val result = recordAndRank(PROGRESS_KEY, GameKeys.CASUALTIES_MULTIPLIER, total, isPerfect, higherIsBetter = false)
        rank = result.rank
        totalPlayers = result.totalPlayers
        finalDistribution = result.distribution

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
            val guessValue = guesses.getOrNull(i)
            val guessDist = guessDistByBattleId[battle.id] ?: emptyList()
            val logMin = ln((guessDist + battle.correctCasualties + listOfNotNull(guessValue)).min().coerceAtLeast(1).toDouble())
            val logMax = ln((guessDist + battle.correctCasualties + listOfNotNull(guessValue)).max().coerceAtLeast(1).toDouble())
            val logSpan = (logMax - logMin).coerceAtLeast(0.01)
            fun logPos(v: Long): Float = ((ln(v.coerceAtLeast(1).toDouble()) - logMin) / logSpan).toFloat().coerceIn(0f, 1f)

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(battle.battle, style = MaterialTheme.typography.bodyMedium)
                Text("x" + formatFixed(multipliers.getOrNull(i) ?: 1.0, 2), style = MaterialTheme.typography.bodySmall)
                GuessDistributionAxis(
                    distribution = guessDist.map { logPos(it) },
                    correctPosition = logPos(battle.correctCasualties),
                    guessPosition = guessValue?.let { logPos(it) },
                    accentColor = MaterialTheme.colorScheme.primary,
                    correctLabel = StringsGamesYearguessCasualties.casualties_actual_label(formatThousands(battle.correctCasualties)),
                    guessLabel = guessValue?.let { StringsGamesYearguessCasualties.casualties_you_label(formatThousands(it)) }
                )
            }
        }
        Button(onClick = onBack) { Text(StringsGamesYearguessCasualties.casualties_back_to_menu) }
    }
}
