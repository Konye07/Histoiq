package com.yourorg.app.Features.Play.TerritoryCompare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.yourorg.app.Data.Quiz.TerritoryCountry
import com.yourorg.app.Data.Quiz.loadTerritories
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TERRITORY_COMPARE
private const val MIN_ELIGIBLE_POOL = 20

private data class HistoryEntry(
    val championId: String,
    val challengerId: String,
    val pickedChampionSide: Boolean,
    val correct: Boolean
)

/**
 * Orders [pool] into runs of 3-4 similarly-sized countries (by area) separated by 1-2 "big jump"
 * countries pulled from far away in the size ranking. Keeps most rounds a close call while
 * occasionally throwing in a lopsided pair, instead of a pure shuffle.
 */
private fun buildClusteredQueue(pool: List<TerritoryCountry>): MutableList<TerritoryCountry> {
    if (pool.size < 6) return pool.shuffled().toMutableList()
    val sorted = pool.sortedBy { it.areaKm2 }
    val used = BooleanArray(sorted.size)
    val result = mutableListOf<TerritoryCountry>()
    var pointer = 0
    var remaining = sorted.size

    while (remaining > 0) {
        while (pointer < sorted.size && used[pointer]) pointer++

        val clusterSize = Random.nextInt(3, 5).coerceAtMost(remaining)
        var added = 0
        var idx = pointer
        while (added < clusterSize && idx < sorted.size) {
            if (!used[idx]) {
                result.add(sorted[idx])
                used[idx] = true
                added++
                remaining--
            }
            idx++
        }
        pointer = idx
        if (remaining == 0) break

        val jumpCount = Random.nextInt(1, 3).coerceAtMost(remaining)
        val farOrder = sorted.indices.filter { !used[it] }.sortedByDescending { kotlin.math.abs(it - pointer) }
        var jumps = 0
        for (cand in farOrder) {
            if (jumps >= jumpCount) break
            result.add(sorted[cand])
            used[cand] = true
            jumps++
            remaining--
        }
    }
    return result
}

/** True when two countries would make an unwinnable, ambiguous "which is bigger" round. */
private fun isTie(a: TerritoryCountry, exclude: TerritoryCountry?): Boolean =
    exclude != null && (a.id == exclude.id || a.areaKm2 == exclude.areaKm2)

private fun drawNext(
    fullList: List<TerritoryCountry>,
    queue: MutableList<TerritoryCountry>,
    exclude: TerritoryCountry?
): TerritoryCountry {
    if (queue.isEmpty()) queue.addAll(buildClusteredQueue(fullList))
    var next = queue.removeAt(0)
    var attempts = 0
    while (isTie(next, exclude) && attempts < fullList.size) {
        if (queue.isEmpty()) queue.addAll(buildClusteredQueue(fullList))
        if (queue.isEmpty()) break
        queue.add(next)
        next = queue.removeAt(0)
        attempts++
    }
    return next
}

private fun persist(
    context: android.content.Context,
    championId: String,
    challengerId: String,
    queueIds: List<String>,
    round: Int,
    finished: Boolean,
    won: Boolean,
    history: List<HistoryEntry>
) {
    val obj = JSONObject()
    obj.put("championId", championId)
    obj.put("challengerId", challengerId)
    obj.put("queueIds", JSONArray(queueIds))
    obj.put("round", round)
    obj.put("finished", finished)
    obj.put("won", won)
    val historyArray = JSONArray()
    history.forEach { h ->
        val o = JSONObject()
        o.put("championId", h.championId)
        o.put("challengerId", h.challengerId)
        o.put("pickedChampionSide", h.pickedChampionSide)
        o.put("correct", h.correct)
        historyArray.put(o)
    }
    obj.put("history", historyArray)
    QuizProgressStore.save(context, PROGRESS_KEY, obj)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerritoryCompareScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var allCountries by remember { mutableStateOf<List<TerritoryCountry>>(emptyList()) }
    val queue = remember { mutableListOf<TerritoryCountry>() }
    val history = remember { mutableStateListOf<HistoryEntry>() }

    var champion by remember { mutableStateOf<TerritoryCountry?>(null) }
    var challenger by remember { mutableStateOf<TerritoryCountry?>(null) }
    var round by remember { mutableStateOf(1) }
    var revealed by remember { mutableStateOf(false) }
    var pickedChampionSide by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

    var pendingChampion by remember { mutableStateOf<TerritoryCountry?>(null) }
    var pendingChallenger by remember { mutableStateOf<TerritoryCountry?>(null) }
    var pendingRound by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        val pool = loadTerritories(context)
        val stored = QuizProgressStore.load(context, PROGRESS_KEY)
        if (stored != null) {
            allCountries = RecentlyUsedStore.eligiblePool(context, PROGRESS_KEY, pool, MIN_ELIGIBLE_POOL) { it.id }
            val queueIds = stored.getJSONArray("queueIds").let { arr -> List(arr.length()) { arr.getString(it) } }
            queue.addAll(queueIds.mapNotNull { id -> pool.find { it.id == id } })
            champion = pool.find { it.id == stored.getString("championId") }
            challenger = pool.find { it.id == stored.getString("challengerId") }
            round = stored.getInt("round")
            won = stored.optBoolean("won", false)
            showResults = stored.optBoolean("finished", false)
            stored.optJSONArray("history")?.let { arr ->
                repeat(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    history.add(
                        HistoryEntry(
                            o.getString("championId"),
                            o.getString("challengerId"),
                            o.getBoolean("pickedChampionSide"),
                            o.getBoolean("correct")
                        )
                    )
                }
            }
        } else {
            val eligible = RecentlyUsedStore.eligiblePool(context, PROGRESS_KEY, pool, MIN_ELIGIBLE_POOL) { it.id }
            if (eligible.isEmpty()) return@LaunchedEffect
            allCountries = eligible
            queue.addAll(buildClusteredQueue(eligible))
            val champ = queue.removeAt(0)
            val chall = drawNext(eligible, queue, champ)
            champion = champ
            challenger = chall
            round = 1
            RecentlyUsedStore.record(context, PROGRESS_KEY, listOf(champ.id, chall.id))
            persist(context, champ.id, chall.id, queue.map { it.id }, 1, false, false, emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.ForestDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bigger Territory") },
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
            val c = champion
            val ch = challenger
            when {
                c == null || ch == null -> CircularProgressIndicator()
                showResults -> TerritoryResults(won = won, history = history, allCountries = allCountries, onBack = onBack)
                else -> TerritoryQuestion(
                    champion = c,
                    challenger = ch,
                    round = round,
                    revealed = revealed,
                    pickedChampionSide = pickedChampionSide,
                    correct = correct,
                    onPick = { isChampion ->
                        val picked = if (isChampion) c else ch
                        val bigger = if (c.areaKm2 >= ch.areaKm2) c else ch
                        val isCorrect = picked.id == bigger.id
                        pickedChampionSide = isChampion
                        correct = isCorrect
                        revealed = true
                        history.add(HistoryEntry(c.id, ch.id, isChampion, isCorrect))
                        if (isCorrect) Haptics.correct(context) else Haptics.wrong(context)

                        if (!isCorrect) {
                            won = false
                            persist(context, c.id, ch.id, queue.map { it.id }, round, true, false, history.toList())
                        } else if (round == ROUNDS_TO_WIN) {
                            won = true
                            persist(context, c.id, ch.id, queue.map { it.id }, round, true, true, history.toList())
                        } else {
                            // The older country (champion) is always retired; the challenger it just faced
                            // is promoted to champion for the next round, paired with a brand new challenger.
                            val newChampion = ch
                            val newChallenger = drawNext(allCountries, queue, newChampion)
                            RecentlyUsedStore.record(context, PROGRESS_KEY, listOf(newChallenger.id))
                            pendingChampion = newChampion
                            pendingChallenger = newChallenger
                            pendingRound = round + 1
                            persist(
                                context, newChampion.id, newChallenger.id, queue.map { it.id },
                                round + 1, false, false, history.toList()
                            )
                        }
                    },
                    onContinue = {
                        if (!correct || round == ROUNDS_TO_WIN) {
                            showResults = true
                        } else {
                            champion = pendingChampion
                            challenger = pendingChallenger
                            round = pendingRound
                            revealed = false
                        }
                    }
                )
            }
        }
    }
    }
}

@Composable
private fun TerritoryQuestion(
    champion: TerritoryCountry,
    challenger: TerritoryCountry,
    round: Int,
    revealed: Boolean,
    pickedChampionSide: Boolean,
    correct: Boolean,
    onPick: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Round $round / $ROUNDS_TO_WIN", style = MaterialTheme.typography.labelLarge)
        Text("Which had the bigger territory at its prime?", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TerritoryCard(
                country = champion,
                isBigger = champion.areaKm2 >= challenger.areaKm2,
                wasPicked = pickedChampionSide,
                revealed = revealed,
                onClick = { onPick(true) },
                modifier = Modifier.weight(1f)
            )
            TerritoryCard(
                country = challenger,
                isBigger = challenger.areaKm2 > champion.areaKm2,
                wasPicked = !pickedChampionSide,
                revealed = revealed,
                onClick = { onPick(false) },
                modifier = Modifier.weight(1f)
            )
        }

        if (revealed) {
            Text(
                if (correct) "Correct!" else "Wrong — game over.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onContinue) {
                Text(if (!correct || round == ROUNDS_TO_WIN) "See Results" else "Next Round")
            }
        }
    }
}

@Composable
private fun TerritoryCard(
    country: TerritoryCountry,
    isBigger: Boolean,
    wasPicked: Boolean,
    revealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = when {
        !revealed -> null
        isBigger -> BorderStroke(2.dp, Color(0xFF2E7D32))
        wasPicked -> BorderStroke(2.dp, Color(0xFFC62828))
        else -> null
    }
    Card(
        modifier = modifier.clickable(enabled = !revealed, onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                country.country,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                country.era,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (revealed) {
                Text(
                    "${"%,d".format(country.areaKm2)} km²",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TerritoryResults(
    won: Boolean,
    history: List<HistoryEntry>,
    allCountries: List<TerritoryCountry>,
    onBack: () -> Unit
) {
    val score = history.count { it.correct }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ScoreHistoryStore.recordResult(context, PROGRESS_KEY, score.toDouble())
        StatsStore.recordCompletion(context, PROGRESS_KEY, score.toDouble(), score == ROUNDS_TO_WIN)
    }
    val dateKey = remember { DailyClock.todayKey() }
    val distribution = remember { DailyDistribution.finalScores(GameKeys.TERRITORY_COMPARE, dateKey) }
    val percentile = remember(score) { DailyDistribution.percentile(score.toDouble(), distribution, higherIsBetter = true) }

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
            rankingText = rankingText(percentile),
            accentColor = MaterialTheme.colorScheme.primary,
            subtitle = if (won) "You survived all $ROUNDS_TO_WIN rounds!" else "You made it to round ${history.size}."
        )
        HorizontalDivider()
        history.forEachIndexed { i, h ->
            val champ = allCountries.find { it.id == h.championId }
            val chall = allCountries.find { it.id == h.challengerId }
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Round ${i + 1}: ${champ?.country ?: "?"} vs ${chall?.country ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (h.correct) "Correct" else "Wrong",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(onClick = onBack) { Text("Back to Menu") }
    }
}
