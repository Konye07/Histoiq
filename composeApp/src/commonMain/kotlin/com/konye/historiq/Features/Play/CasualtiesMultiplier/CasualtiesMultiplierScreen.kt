package com.konye.historiq.Features.Play.CasualtiesMultiplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Localization.StringsGamesYearguessCasualties
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Theme.EraThemes
import com.konye.historiq.Core.Theme.GameBackgroundArt
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.dimmedOn
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.ActiveEra
import com.konye.historiq.Data.Quiz.BattleCasualty
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.DailyDistribution
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.casualtyMultiplier
import com.konye.historiq.Data.Quiz.formatEraYear
import com.konye.historiq.Data.Quiz.geometricMeanMultiplier
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.getDoubleList
import com.konye.historiq.Data.Quiz.getLongList
import com.konye.historiq.Data.Quiz.loadBattleCasualties
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Data.Quiz.resumeOrStartQuiz
import com.konye.historiq.Features.Play.Shared.GameBarTitle
import com.konye.historiq.Features.Play.Shared.GuessQuestionLayout
import com.konye.historiq.Features.Play.Shared.HintAction
import com.konye.historiq.Features.Play.Shared.QuestionResultCard
import com.konye.historiq.Features.Play.Shared.ResultsHeader
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
private const val GAME_KEY = GameKeys.CASUALTIES_MULTIPLIER

private suspend fun persist(modeKey: String, ids: List<String>, guesses: List<Long>, multipliers: List<Double>) {
    QuizProgressStore.save(modeKey, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("guesses", JsonArray(guesses.map { JsonPrimitive(it) }))
        put("multipliers", JsonArray(multipliers.map { JsonPrimitive(it) }))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasualtiesMultiplierScreen(onBack: () -> Unit = {}) {
    val era = remember { ActiveEra.current() }
    val modeKey = remember { GameKeys.modeKey(GAME_KEY, era) }
    var battles by remember { mutableStateOf<List<BattleCasualty>>(emptyList()) }
    var questionIndex by remember { mutableStateOf(0) }
    var guessText by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }
    var hintRevealed by remember { mutableStateOf(false) }
    var resumedAlreadyDone by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Long>() }
    val multipliers = remember { mutableStateListOf<Double>() }

    LaunchedEffect(Unit) {
        val resume = resumeOrStartQuiz(modeKey, QUESTIONS_PER_ROUND, { it.id }, ::loadBattleCasualties)
        battles = resume.questions
        resume.resumedState?.let { stored ->
            guesses.addAll(stored.getLongList("guesses"))
            multipliers.addAll(stored.getDoubleList("multipliers"))
            questionIndex = multipliers.size
            resumedAlreadyDone = questionIndex >= battles.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(GameKeys.color(modeKey))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { GameBarTitle(StringsGamesYearguessCasualties.casualties_title, era) },
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
                GameBackgroundArt(
                    EraThemes.backgroundAsset(era, GAME_KEY),
                    MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.offset(y = (-90).dp),
                    fallbackAssetPath = EraThemes.backgroundAsset(null, GAME_KEY)
                )
                when {
                    battles.isEmpty() -> HistoriqLoadingIndicator()
                    questionIndex >= battles.size -> {
                        if (resumedAlreadyDone) {
                            CasualtiesResults(modeKey, battles, guesses, multipliers, onBack)
                        } else {
                            AdGatedResults { CasualtiesResults(modeKey, battles, guesses, multipliers, onBack) }
                        }
                    }
                    else -> {
                        val current = battles[questionIndex]
                        CasualtiesQuestion(
                            modeKey = modeKey,
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
                                val multiplier = casualtyMultiplier(guess, current.correctCasualties)
                                multipliers.add(multiplier)
                                if (multiplier <= 1.5) { Haptics.correct(); BubbleSoundPlayer.playCorrectChirp() } else { Haptics.wrong(); BubbleSoundPlayer.playWrongTone() }
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
            persist(modeKey, battles.map { it.id }, guesses.toList(), multipliers.toList())
        }
    }
}

@Composable
private fun CasualtiesQuestion(
    modeKey: String,
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
    val focusManager = LocalFocusManager.current
    val canSubmit = guessText.toLongOrNull() != null
    val submit = {
        if (canSubmit) {
            focusManager.clearFocus()
            onSubmit()
        }
    }

    GuessQuestionLayout(
        scrollingContent = {
            Text(StringsGamesYearguessCasualties.casualties_question_counter(questionNumber, totalQuestions), style = MaterialTheme.typography.labelLarge)
            Text(battle.battle, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(formatEraYear(battle.year), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            if (hintRevealed) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
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
            if (revealed) {
                var guessDist by remember(battle.id) { mutableStateOf<List<Long>>(emptyList()) }
                LaunchedEffect(battle.id) {
                    guessDist = DailyDistribution.casualtyGuesses(modeKey, DailyClock.todayKey(), battle.id)
                }
                if (!hintRevealed) {
                    battle.hint?.let {
                        Text(
                            StringsGamesYearguessCasualties.casualties_hint_prefix(it),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.7f)
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
            }
        },
        dock = {
            if (!revealed) {
                OutlinedTextField(
                    value = guessText,
                    onValueChange = onGuessChange,
                    label = { Text(StringsGamesYearguessCasualties.casualties_guess_label) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { submit() }, enabled = canSubmit) {
                    Text(StringsGamesYearguessCasualties.casualties_submit)
                }
            } else {
                Button(onClick = onNext) {
                    Text(if (questionNumber == totalQuestions) StringsGamesYearguessCasualties.casualties_see_results else StringsGamesYearguessCasualties.casualties_next_question)
                }
            }
        }
    )
}

@Composable
private fun CasualtiesResults(
    modeKey: String,
    battles: List<BattleCasualty>,
    guesses: List<Long>,
    multipliers: List<Double>,
    onBack: () -> Unit
) {
    val total = geometricMeanMultiplier(multipliers)
    val isPerfect = multipliers.isNotEmpty() && multipliers.all { it == 1.0 }
    val (rank, totalPlayers, finalDistribution, topPercent) = rememberLiveRank(modeKey, modeKey, total, isPerfect, higherIsBetter = false)
    var guessDistByBattleId by remember { mutableStateOf<Map<String, List<Long>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val dateKey = DailyClock.todayKey()
        guessDistByBattleId = battles.associate {
            it.id to DailyDistribution.casualtyGuesses(modeKey, dateKey, it.id)
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
            gameKey = modeKey,
            scoreText = "x" + formatFixed(total, 2),
            isPerfect = isPerfect,
            rank = rank,
            totalPlayers = totalPlayers,
            topPercent = topPercent,
            accentColor = MaterialTheme.colorScheme.primary,
            subtitle = StringsGamesYearguessCasualties.casualties_final_multiplier_caption,
            distribution = finalDistribution.map { multiplierPos(it) },
            yourPosition = multiplierPos(total),
            distributionMinLabel = "x1.00",
            distributionMaxLabel = "x" + formatFixed(multiplierMax, 2)
        )
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
                            color = MaterialTheme.colorScheme.onBackground.dimmedOn(MaterialTheme.colorScheme.background, 0.7f)
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
