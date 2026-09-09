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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyDistribution
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.RecentlyUsedStore
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Quiz.StatsStore
import com.yourorg.app.Data.Quiz.TrueFalseStatement
import com.yourorg.app.Data.Quiz.loadTrueFalseStatements
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import org.json.JSONArray
import org.json.JSONObject

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TRUE_FALSE

private fun persist(context: android.content.Context, ids: List<String>, answers: List<Boolean>, failed: Boolean) {
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
        val pool = loadTrueFalseStatements(context)
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)?.takeIf { it.has("ids") }
        if (stored != null) {
            val ids = stored.getJSONArray("ids").let { arr -> List(arr.length()) { arr.getString(it) } }
            statements = ids.mapNotNull { id -> pool.find { it.id == id } }
            stored.optJSONArray("answers")?.let { arr -> repeat(arr.length()) { answers.add(arr.getBoolean(it)) } }
            failed = stored.optBoolean("failed", false)
            index = answers.size
            showResults = failed || index >= statements.size
        } else {
            val chosen = RecentlyUsedStore.pickDaily(context, PROGRESS_KEY, pool, ROUNDS_TO_WIN) { it.id }
            statements = chosen
            persist(context, chosen.map { it.id }, emptyList(), false)
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.BrownDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("True or False") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            persist(context, statements.map { it.id }, answers.toList(), failed)
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
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Round $round / $ROUNDS_TO_WIN", style = MaterialTheme.typography.labelLarge)
        Text(statement.statement, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        if (!revealed) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onAnswer(true) }) { Text("True") }
                Button(onClick = { onAnswer(false) }) { Text("False") }
            }
        } else {
            Text(
                if (correct) "Correct!" else "Wrong — game over.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "The statement is ${if (statement.correctAnswer) "TRUE" else "FALSE"}.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onContinue) {
                Text(if (!correct || round == ROUNDS_TO_WIN) "See Results" else "Next Statement")
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
    LaunchedEffect(Unit) {
        ScoreHistoryStore.recordResult(context, PROGRESS_KEY, score.toDouble())
        StatsStore.recordCompletion(context, PROGRESS_KEY, score.toDouble(), score == ROUNDS_TO_WIN)
    }
    val dateKey = remember { DailyClock.todayKey() }
    val distribution = remember { DailyDistribution.finalScores(GameKeys.TRUE_FALSE, dateKey) }
    val (rank, totalPlayers) = remember(score) {
        DailyDistribution.rankAmongPlayers(GameKeys.TRUE_FALSE, dateKey, score.toDouble(), distribution, higherIsBetter = true)
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
            subtitle = if (won) "You survived all $ROUNDS_TO_WIN statements!" else "You made it to statement ${answers.size}."
        )
        HorizontalDivider()
        answers.forEachIndexed { i, answer ->
            val statement = statements[i]
            Column(Modifier.fillMaxWidth()) {
                Text(statement.statement, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Your answer: ${if (answer) "TRUE" else "FALSE"}  ·  Correct: ${if (statement.correctAnswer) "TRUE" else "FALSE"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(onClick = onBack) { Text("Back to Menu") }
    }
}
