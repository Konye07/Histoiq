package com.yourorg.app.Features.Play.TrueFalse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Localization.StringsGamesTruefalse
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.TrueFalseStatement
import com.yourorg.app.Data.Quiz.getBooleanList
import com.yourorg.app.Data.Quiz.loadTrueFalseStatements
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Data.Quiz.resumeOrStartQuiz
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_arrow_back
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TRUE_FALSE

private suspend fun persist(ids: List<String>, answers: List<Boolean>, failed: Boolean) {
    QuizProgressStore.save(PROGRESS_KEY, buildJsonObject {
        put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        put("answers", JsonArray(answers.map { JsonPrimitive(it) }))
        put("failed", failed)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseScreen(onBack: () -> Unit = {}) {
    var statements by remember { mutableStateOf<List<TrueFalseStatement>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var pickedTrue by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val answers = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(Unit) {
        val resume = resumeOrStartQuiz(PROGRESS_KEY, ROUNDS_TO_WIN, { it.id }, ::loadTrueFalseStatements)
        statements = resume.questions
        resume.resumedState?.let { stored ->
            answers.addAll(stored.getBooleanList("answers"))
            failed = (stored["failed"] as? JsonPrimitive)?.boolean ?: false
            index = answers.size
            showResults = failed || index >= statements.size
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.BrownDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(GameKeys.title(GameKeys.TRUE_FALSE)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesTruefalse.tf_back)
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
                statements.isEmpty() -> CircularProgressIndicator()
                showResults -> TrueFalseResults(statements, answers, failed, onBack)
                else -> {
                    val current = statements[index]
                    TrueFalseQuestion(
                        statement = current,
                        round = index + 1,
                        revealed = revealed,
                        pickedTrue = pickedTrue,
                        correct = correct,
                        onAnswer = { answerTrue ->
                            pickedTrue = answerTrue
                            val isCorrect = answerTrue == current.correctAnswer
                            correct = isCorrect
                            if (isCorrect) Haptics.correct() else Haptics.wrong()
                            answers.add(answerTrue)
                            if (!isCorrect) failed = true
                            revealed = true
                        },
                        onContinue = {
                            if (!correct || index == statements.size - 1) {
                                showResults = true
                            } else {
                                index += 1
                                revealed = false
                            }
                        }
                    )
                }
            }
        }
    }
    }

    LaunchedEffect(answers.size, failed) {
        if (statements.isNotEmpty()) {
            persist(statements.map { it.id }, answers.toList(), failed)
        }
    }
}

@Composable
private fun TrueFalseQuestion(
    statement: TrueFalseStatement,
    round: Int,
    revealed: Boolean,
    pickedTrue: Boolean,
    correct: Boolean,
    onAnswer: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val language = SettingsStore.language()
    val trueCaps = StringsGamesTruefalse.tf_true_caps
    val falseCaps = StringsGamesTruefalse.tf_false_caps

    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(StringsGamesTruefalse.tf_round_counter(round, ROUNDS_TO_WIN), style = MaterialTheme.typography.labelLarge)
        Text(statement.localized(language), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        if (!revealed) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onAnswer(true) }) { Text(StringsGamesTruefalse.tf_true_button) }
                Button(onClick = { onAnswer(false) }) { Text(StringsGamesTruefalse.tf_false_button) }
            }
        } else {
            Text(
                if (correct) StringsGamesTruefalse.tf_correct else StringsGamesTruefalse.tf_wrong_game_over,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                StringsGamesTruefalse.tf_statement_is(if (statement.correctAnswer) trueCaps else falseCaps),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onContinue) {
                Text(if (!correct || round == ROUNDS_TO_WIN) StringsGamesTruefalse.tf_see_results else StringsGamesTruefalse.tf_next_statement)
            }
        }
    }
}

@Composable
private fun TrueFalseResults(
    statements: List<TrueFalseStatement>,
    answers: List<Boolean>,
    failed: Boolean,
    onBack: () -> Unit
) {
    val won = !failed && answers.size == statements.size
    val score = answers.indices.count { i -> answers[i] == statements[i].correctAnswer }
    val language = SettingsStore.language()
    val trueCaps = StringsGamesTruefalse.tf_true_caps
    val falseCaps = StringsGamesTruefalse.tf_false_caps
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val result = recordAndRank(PROGRESS_KEY, GameKeys.TRUE_FALSE, score.toDouble(), score == ROUNDS_TO_WIN)
        rank = result.rank
        totalPlayers = result.totalPlayers
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
            scoreText = "$score",
            isPerfect = score == ROUNDS_TO_WIN,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary,
            subtitle = if (won) StringsGamesTruefalse.tf_survived_all(ROUNDS_TO_WIN) else StringsGamesTruefalse.tf_made_it_to(answers.size)
        )
        HorizontalDivider()
        answers.forEachIndexed { i, answer ->
            val statement = statements[i]
            Column(Modifier.fillMaxWidth()) {
                Text(statement.localized(language), style = MaterialTheme.typography.bodyMedium)
                Text(
                    StringsGamesTruefalse.tf_answer_correct_line(
                        if (answer) trueCaps else falseCaps,
                        if (statement.correctAnswer) trueCaps else falseCaps
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                if (failed && i == answers.lastIndex) {
                    Text(
                        StringsGamesTruefalse.tf_failed_here,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (failed && answers.size < statements.size) {
            HorizontalDivider()
            Text(StringsGamesTruefalse.tf_unplayed_header, style = MaterialTheme.typography.titleMedium)
            statements.drop(answers.size).forEach { statement ->
                val dimmed = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                Column(Modifier.fillMaxWidth()) {
                    Text(statement.localized(language), style = MaterialTheme.typography.bodyMedium, color = dimmed)
                    Text(
                        StringsGamesTruefalse.tf_unplayed_answer_line(if (statement.correctAnswer) trueCaps else falseCaps),
                        style = MaterialTheme.typography.bodySmall,
                        color = dimmed
                    )
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesTruefalse.tf_back_to_menu) }
    }
}
