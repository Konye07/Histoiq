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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyQuizRepository
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.TrueFalseStatement
import com.yourorg.app.Data.Quiz.loadTrueFalseStatements
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import com.yourorg.app.R
import org.json.JSONArray
import org.json.JSONObject

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TRUE_FALSE

private suspend fun persist(context: android.content.Context, ids: List<String>, answers: List<Boolean>, failed: Boolean) {
    val obj = JSONObject()
    obj.put("ids", JSONArray(ids))
    obj.put("answers", JSONArray(answers))
    obj.put("failed", failed)
    QuizProgressStore.save(context, PROGRESS_KEY, obj)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var statements by remember { mutableStateOf<List<TrueFalseStatement>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var pickedTrue by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val answers = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(Unit) {
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            val rows = loadTrueFalseStatements(ids)
            statements = ids.mapNotNull { id -> rows.find { it.id == id } }
            stored.optJSONArray("answers")?.let { arr -> repeat(arr.length()) { answers.add(arr.getBoolean(it)) } }
            failed = stored.optBoolean("failed", false)
            index = answers.size
            showResults = failed || index >= statements.size
        } else {
            val ids = DailyQuizRepository.getOrCreate(PROGRESS_KEY, DailyClock.todayKey(), ROUNDS_TO_WIN)
            val rows = loadTrueFalseStatements(ids)
            statements = ids.mapNotNull { id -> rows.find { it.id == id } }
            persist(context, ids, emptyList(), false)
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.BrownDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(GameKeys.title(GameKeys.TRUE_FALSE)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.tf_back))
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
                            if (isCorrect) Haptics.correct(context) else Haptics.wrong(context)
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
            persist(context, statements.map { it.id }, answers.toList(), failed)
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
    val language = SettingsStore.language(LocalContext.current)
    val trueCaps = stringResource(R.string.tf_true_caps)
    val falseCaps = stringResource(R.string.tf_false_caps)

    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.tf_round_counter, round, ROUNDS_TO_WIN), style = MaterialTheme.typography.labelLarge)
        Text(statement.localized(language), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        if (!revealed) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onAnswer(true) }) { Text(stringResource(R.string.tf_true_button)) }
                Button(onClick = { onAnswer(false) }) { Text(stringResource(R.string.tf_false_button)) }
            }
        } else {
            Text(
                if (correct) stringResource(R.string.tf_correct) else stringResource(R.string.tf_wrong_game_over),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.tf_statement_is, if (statement.correctAnswer) trueCaps else falseCaps),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onContinue) {
                Text(if (!correct || round == ROUNDS_TO_WIN) stringResource(R.string.tf_see_results) else stringResource(R.string.tf_next_statement))
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
    val context = LocalContext.current
    val language = SettingsStore.language(context)
    val trueCaps = stringResource(R.string.tf_true_caps)
    val falseCaps = stringResource(R.string.tf_false_caps)
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val result = recordAndRank(context, PROGRESS_KEY, GameKeys.TRUE_FALSE, score.toDouble(), score == ROUNDS_TO_WIN)
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
            scoreText = "$score",
            isPerfect = score == ROUNDS_TO_WIN,
            rankingText = rankingText(rank, totalPlayers),
            accentColor = MaterialTheme.colorScheme.primary,
            subtitle = if (won) stringResource(R.string.tf_survived_all, ROUNDS_TO_WIN) else stringResource(R.string.tf_made_it_to, answers.size)
        )
        HorizontalDivider()
        answers.forEachIndexed { i, answer ->
            val statement = statements[i]
            Column(Modifier.fillMaxWidth()) {
                Text(statement.localized(language), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(
                        R.string.tf_answer_correct_line,
                        if (answer) trueCaps else falseCaps,
                        if (statement.correctAnswer) trueCaps else falseCaps
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(onClick = onBack) { Text(stringResource(R.string.tf_back_to_menu)) }
    }
}
