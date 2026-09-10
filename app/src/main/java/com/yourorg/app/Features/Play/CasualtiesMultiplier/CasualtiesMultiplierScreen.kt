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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.R
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.BattleCasualty
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.DailyQuizRepository
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.casualtyMultiplier
import com.yourorg.app.Data.Quiz.formatEraYear
import com.yourorg.app.Data.Quiz.formatThousands
import com.yourorg.app.Data.Quiz.loadBattleCasualties
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Features.Play.Shared.GuessDistributionAxis
import com.yourorg.app.Features.Play.Shared.HintAction
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import kotlin.math.ln
import kotlin.math.max
import org.json.JSONArray
import org.json.JSONObject

private const val QUESTIONS_PER_ROUND = 5
private const val PROGRESS_KEY = GameKeys.CASUALTIES_MULTIPLIER

private suspend fun persist(context: android.content.Context, ids: List<String>, guesses: List<Long>, multipliers: List<Double>) {
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
    var hintRevealed by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Long>() }
    val multipliers = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            val rows = loadBattleCasualties(ids)
            battles = ids.mapNotNull { id -> rows.find { it.id == id } }
            val storedGuesses = stored.optJSONArray("guesses")
            if (storedGuesses != null) repeat(storedGuesses.length()) { guesses.add(storedGuesses.getLong(it)) }
            val storedMultipliers = stored.optJSONArray("multipliers")
            if (storedMultipliers != null) repeat(storedMultipliers.length()) { multipliers.add(storedMultipliers.getDouble(it)) }
            questionIndex = multipliers.size
        } else {
            val ids = DailyQuizRepository.getOrCreate(PROGRESS_KEY, DailyClock.todayKey(), QUESTIONS_PER_ROUND)
            val rows = loadBattleCasualties(ids)
            battles = ids.mapNotNull { id -> rows.find { it.id == id } }
            persist(context, ids, emptyList(), emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.Maroon)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.casualties_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.casualties_back_cd))
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
            persist(context, battles.map { it.id }, guesses.toList(), multipliers.toList())
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
        Text(stringResource(R.string.casualties_question_counter, questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
        Text(battle.battle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(formatEraYear(battle.year), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (hintRevealed) {
            Surface(
                color = AppColors.Cream,
                contentColor = AppColors.Maroon,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.casualties_hint_prefix, battle.hint ?: stringResource(R.string.casualties_hint_unavailable)),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = onGuessChange,
            label = { Text(stringResource(R.string.casualties_guess_label)) },
            enabled = !revealed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (!revealed) {
            Button(onClick = onSubmit, enabled = guessText.toLongOrNull() != null) {
                Text(stringResource(R.string.casualties_submit))
            }
        } else {
            Text(stringResource(R.string.casualties_actual_casualties, formatThousands(battle.correctCasualties)), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.casualties_earned_multiplier, "x%.2f".format(lastMultiplier ?: 1.0)),
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
                correctLabel = stringResource(R.string.casualties_actual_label, formatThousands(battle.correctCasualties)),
                guessLabel = lastGuess?.let { stringResource(R.string.casualties_you_label, formatThousands(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onNext) {
                Text(if (questionNumber == totalQuestions) stringResource(R.string.casualties_see_results) else stringResource(R.string.casualties_next_question))
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
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    var finalDistribution by remember { mutableStateOf<List<Double>>(emptyList()) }
    var guessDistByBattleId by remember { mutableStateOf<Map<String, List<Long>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val result = recordAndRank(context, PROGRESS_KEY, GameKeys.CASUALTIES_MULTIPLIER, total, isPerfect, higherIsBetter = false)
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
            scoreText = "x%.2f".format(total),
            isPerfect = isPerfect,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary
        )
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.casualties_final_multiplier_caption), style = MaterialTheme.typography.bodyMedium)
            GuessDistributionAxis(
                distribution = finalDistribution.map { multiplierPos(it) },
                correctPosition = null,
                guessPosition = multiplierPos(total),
                accentColor = MaterialTheme.colorScheme.primary,
                correctLabel = null,
                guessLabel = stringResource(R.string.casualties_you_label, "x%.2f".format(total))
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
                Text("x%.2f".format(multipliers.getOrNull(i) ?: 1.0), style = MaterialTheme.typography.bodySmall)
                GuessDistributionAxis(
                    distribution = guessDist.map { logPos(it) },
                    correctPosition = logPos(battle.correctCasualties),
                    guessPosition = guessValue?.let { logPos(it) },
                    accentColor = MaterialTheme.colorScheme.primary,
                    correctLabel = stringResource(R.string.casualties_actual_label, formatThousands(battle.correctCasualties)),
                    guessLabel = guessValue?.let { stringResource(R.string.casualties_you_label, formatThousands(it)) }
                )
            }
        }
        Button(onClick = onBack) { Text(stringResource(R.string.casualties_back_to_menu)) }
    }
}
