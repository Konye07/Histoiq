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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Ads.AdGatedResults
import com.yourorg.app.Core.Formatting.formatWithCommas
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.gameColorScheme
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.DailyQuizRepository
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.TerritoryCountry
import com.yourorg.app.Data.Quiz.loadTerritories
import com.yourorg.app.Data.Quiz.recordAndRank
import com.yourorg.app.Features.Play.Shared.ResultsHeader
import com.yourorg.app.Features.Play.Shared.rankingText
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_arrow_back
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TERRITORY_COMPARE

/** Big enough to comfortably outlast a 10-round run (plus tie-skip retries) drawn from one day's shared pool. */
private const val DAILY_POOL_SIZE = 60

@Serializable
private data class HistoryEntry(
    val championId: String,
    val challengerId: String,
    val pickedChampionSide: Boolean,
    val correct: Boolean
)

@Serializable
private data class TerritoryProgressState(
    val championId: String,
    val challengerId: String,
    val queueIds: List<String>,
    val round: Int,
    val finished: Boolean,
    val won: Boolean,
    val history: List<HistoryEntry> = emptyList()
)

/**
 * Orders [pool] into runs of 3-4 similarly-sized countries (by area) separated by 1-2 "big jump"
 * countries pulled from far away in the size ranking. Keeps most rounds a close call while
 * occasionally throwing in a lopsided pair, instead of a pure shuffle. [random] is seeded by the
 * day's date so every player who gets the same [pool] (from get_or_create_daily_quiz) sees the
 * exact same match order — a pure function of (pool, date), not per-device randomness.
 */
private fun buildClusteredQueue(pool: List<TerritoryCountry>, random: Random): MutableList<TerritoryCountry> {
    if (pool.size < 6) return pool.shuffled(random).toMutableList()
    val sorted = pool.sortedBy { it.areaKm2 }
    val used = BooleanArray(sorted.size)
    val result = mutableListOf<TerritoryCountry>()
    var pointer = 0
    var remaining = sorted.size

    while (remaining > 0) {
        while (pointer < sorted.size && used[pointer]) pointer++

        val clusterSize = random.nextInt(3, 5).coerceAtMost(remaining)
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

        val jumpCount = random.nextInt(1, 3).coerceAtMost(remaining)
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
    exclude: TerritoryCountry?,
    random: Random
): TerritoryCountry {
    if (queue.isEmpty()) queue.addAll(buildClusteredQueue(fullList, random))
    var next = queue.removeAt(0)
    var attempts = 0
    while (isTie(next, exclude) && attempts < fullList.size) {
        if (queue.isEmpty()) queue.addAll(buildClusteredQueue(fullList, random))
        if (queue.isEmpty()) break
        queue.add(next)
        next = queue.removeAt(0)
        attempts++
    }
    return next
}

/**
 * Which side wins a round is ground truth (territory size), independent of what the player
 * guessed — so the matchups that would have followed a loss are fully determined: the actually
 * bigger side of the failed round advances as champion, paired against the next challenger already
 * queued in the shared daily order. Used to show "what would have come next" on a failed run.
 */
private fun buildUnplayedRounds(
    lastChampion: TerritoryCountry,
    lastChallenger: TerritoryCountry,
    remainingQueue: List<TerritoryCountry>,
    startRound: Int,
    roundsToWin: Int
): List<Pair<TerritoryCountry, TerritoryCountry>> {
    val result = mutableListOf<Pair<TerritoryCountry, TerritoryCountry>>()
    var champion = if (lastChampion.areaKm2 >= lastChallenger.areaKm2) lastChampion else lastChallenger
    val queue = remainingQueue.toMutableList()
    var round = startRound
    while (round <= roundsToWin && queue.isNotEmpty()) {
        var next = queue.removeAt(0)
        var attempts = 0
        while (isTie(next, champion) && queue.isNotEmpty() && attempts < remainingQueue.size) {
            queue.add(next)
            next = queue.removeAt(0)
            attempts++
        }
        if (isTie(next, champion)) break
        result.add(champion to next)
        champion = if (champion.areaKm2 >= next.areaKm2) champion else next
        round++
    }
    return result
}

private suspend fun persist(
    championId: String,
    challengerId: String,
    queueIds: List<String>,
    round: Int,
    finished: Boolean,
    won: Boolean,
    history: List<HistoryEntry>
) {
    val state = TerritoryProgressState(championId, challengerId, queueIds, round, finished, won, history)
    QuizProgressStore.save(PROGRESS_KEY, Json.encodeToJsonElement(state).jsonObject)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerritoryCompareScreen(onBack: () -> Unit = {}) {
    var allCountries by remember { mutableStateOf<List<TerritoryCountry>>(emptyList()) }
    val queue = remember { mutableListOf<TerritoryCountry>() }
    val history = remember { mutableStateListOf<HistoryEntry>() }
    val dailyRandom = remember { Random(DailyClock.todayKey().hashCode().toLong()) }

    var champion by remember { mutableStateOf<TerritoryCountry?>(null) }
    var challenger by remember { mutableStateOf<TerritoryCountry?>(null) }
    var round by remember { mutableStateOf(1) }
    var revealed by remember { mutableStateOf(false) }
    var pickedChampionSide by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var resumedAlreadyDone by remember { mutableStateOf(false) }

    var pendingChampion by remember { mutableStateOf<TerritoryCountry?>(null) }
    var pendingChallenger by remember { mutableStateOf<TerritoryCountry?>(null) }
    var pendingRound by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        val dateKey = DailyClock.todayKey()
        val ids = DailyQuizRepository.getOrCreate(PROGRESS_KEY, dateKey, DAILY_POOL_SIZE)
        val pool = loadTerritories(ids)
        allCountries = pool
        if (pool.isEmpty()) return@LaunchedEffect

        val stored = QuizProgressStore.load(PROGRESS_KEY)
        if (stored != null) {
            val state = Json.decodeFromJsonElement<TerritoryProgressState>(stored)
            queue.addAll(state.queueIds.mapNotNull { id -> pool.find { it.id == id } })
            champion = pool.find { it.id == state.championId }
            challenger = pool.find { it.id == state.challengerId }
            round = state.round
            won = state.won
            showResults = state.finished
            resumedAlreadyDone = state.finished
            history.addAll(state.history)
        } else {
            queue.addAll(buildClusteredQueue(pool, dailyRandom))
            val champ = queue.removeAt(0)
            val chall = drawNext(pool, queue, champ, dailyRandom)
            champion = champ
            challenger = chall
            round = 1
            persist(champ.id, chall.id, queue.map { it.id }, 1, false, false, emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.ForestDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StringsGamesFlagTerritoryBlindmap.territory_title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = StringsGamesFlagTerritoryBlindmap.territory_back)
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
                showResults && resumedAlreadyDone -> TerritoryResults(
                    won = won,
                    history = history,
                    allCountries = allCountries,
                    lastChampion = c,
                    lastChallenger = ch,
                    remainingQueue = queue.toList(),
                    onBack = onBack
                )
                showResults -> AdGatedResults {
                    TerritoryResults(
                        won = won,
                        history = history,
                        allCountries = allCountries,
                        lastChampion = c,
                        lastChallenger = ch,
                        remainingQueue = queue.toList(),
                        onBack = onBack
                    )
                }
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
                        if (isCorrect) Haptics.correct() else Haptics.wrong()

                        if (!isCorrect) {
                            won = false
                        } else if (round == ROUNDS_TO_WIN) {
                            won = true
                        } else {
                            // The older country (champion) is always retired; the challenger it just faced
                            // is promoted to champion for the next round, paired with a brand new challenger.
                            val newChampion = ch
                            val newChallenger = drawNext(allCountries, queue, newChampion, dailyRandom)
                            pendingChampion = newChampion
                            pendingChallenger = newChallenger
                            pendingRound = round + 1
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

    LaunchedEffect(round, showResults) {
        val c = champion
        val ch = challenger
        if (c != null && ch != null) {
            persist(c.id, ch.id, queue.map { it.id }, round, showResults, won, history.toList())
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
        Text(StringsGamesFlagTerritoryBlindmap.territory_round_counter(round, ROUNDS_TO_WIN), style = MaterialTheme.typography.titleMedium)
        Text(StringsGamesFlagTerritoryBlindmap.territory_question_prompt, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)

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
                if (correct) StringsGamesFlagTerritoryBlindmap.territory_correct else StringsGamesFlagTerritoryBlindmap.territory_wrong_game_over,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onContinue) {
                Text(if (!correct || round == ROUNDS_TO_WIN) StringsGamesFlagTerritoryBlindmap.territory_see_results else StringsGamesFlagTerritoryBlindmap.territory_next_round)
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
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                country.era,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (revealed) {
                Text(
                    StringsGamesFlagTerritoryBlindmap.territory_area_km2(formatWithCommas(country.areaKm2)),
                    style = MaterialTheme.typography.titleMedium,
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
    lastChampion: TerritoryCountry,
    lastChallenger: TerritoryCountry,
    remainingQueue: List<TerritoryCountry>,
    onBack: () -> Unit
) {
    val score = history.count { it.correct }
    var rank by remember { mutableStateOf(1) }
    var totalPlayers by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val result = recordAndRank(PROGRESS_KEY, GameKeys.TERRITORY_COMPARE, score.toDouble(), score == ROUNDS_TO_WIN)
        rank = result.rank
        totalPlayers = result.totalPlayers
    }
    val unplayedMatchups = remember(won, lastChampion, lastChallenger, remainingQueue) {
        if (won) emptyList() else buildUnplayedRounds(lastChampion, lastChallenger, remainingQueue, history.size + 1, ROUNDS_TO_WIN)
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
            subtitle = if (won) StringsGamesFlagTerritoryBlindmap.territory_survived_all(ROUNDS_TO_WIN) else StringsGamesFlagTerritoryBlindmap.territory_made_it_to_round(history.size)
        )
        HorizontalDivider()
        history.forEachIndexed { i, h ->
            val champ = allCountries.find { it.id == h.championId }
            val chall = allCountries.find { it.id == h.challengerId }
            Column(Modifier.fillMaxWidth()) {
                Text(
                    StringsGamesFlagTerritoryBlindmap.territory_round_matchup(i + 1, champ?.country ?: "?", chall?.country ?: "?"),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (h.correct) StringsGamesFlagTerritoryBlindmap.territory_result_correct else StringsGamesFlagTerritoryBlindmap.territory_result_wrong,
                    style = MaterialTheme.typography.bodySmall
                )
                if (!won && i == history.lastIndex) {
                    Text(
                        StringsGamesFlagTerritoryBlindmap.territory_failed_here,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (unplayedMatchups.isNotEmpty()) {
            HorizontalDivider()
            Text(StringsGamesFlagTerritoryBlindmap.territory_unplayed_header, style = MaterialTheme.typography.titleMedium)
            val dimmed = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            unplayedMatchups.forEachIndexed { offset, (champ, chall) ->
                val bigger = if (champ.areaKm2 >= chall.areaKm2) champ else chall
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        StringsGamesFlagTerritoryBlindmap.territory_round_matchup(history.size + 1 + offset, champ.country, chall.country),
                        style = MaterialTheme.typography.bodyMedium,
                        color = dimmed
                    )
                    Text(
                        StringsGamesFlagTerritoryBlindmap.territory_unplayed_bigger(bigger.country),
                        style = MaterialTheme.typography.bodySmall,
                        color = dimmed
                    )
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesFlagTerritoryBlindmap.territory_back_to_menu) }
    }
}
