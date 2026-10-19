package com.konye.historiq.Features.Play.TerritoryCompare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.konye.historiq.Core.Ads.AdGatedResults
import com.konye.historiq.Core.Formatting.formatWithCommas
import com.konye.historiq.Core.Audio.BubbleSoundPlayer
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.StringsDailyBattle
import com.konye.historiq.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Network.retryNetwork
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.GameBackgroundArt
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.errorText
import com.konye.historiq.Core.Theme.gameColorScheme
import com.konye.historiq.Data.Quiz.BattleLeg
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.DailyQuizRepository
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizProgressStore
import com.konye.historiq.Data.Quiz.TerritoryCountry
import com.konye.historiq.Data.Quiz.battleSurvivalScore
import com.konye.historiq.Data.Quiz.formatThousands
import com.konye.historiq.Data.Quiz.loadTerritories
import com.konye.historiq.Data.Quiz.rememberLiveRank
import com.konye.historiq.Features.Play.Shared.BattleLegFinished
import com.konye.historiq.Features.Play.Shared.GameBarTitle
import com.konye.historiq.Features.Play.Shared.ResultsHeader
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource

private const val ROUNDS_TO_WIN = 10
private const val PROGRESS_KEY = GameKeys.TERRITORY_COMPARE

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
 * Plain shuffle — no size-based ordering, so there's no trend to notice from one round to the
 * next. [random] is seeded by the day's date so every player who gets the same [pool] (from
 * get_or_create_daily_quiz) sees the exact same match order — a pure function of (pool, date),
 * not per-device randomness. An earlier version deliberately clustered similarly-sized countries
 * together, which in practice meant several consecutive rounds walked monotonically up a
 * size-sorted run — a real, noticeable trend, not "random."
 */
private fun buildShuffledQueue(pool: List<TerritoryCountry>, random: Random): MutableList<TerritoryCountry> =
    pool.shuffled(random).toMutableList()

/** True when two countries would make an unwinnable, ambiguous "which is bigger" round. */
private fun isTie(a: TerritoryCountry, exclude: TerritoryCountry?): Boolean =
    exclude != null && (a.id == exclude.id || a.areaKm2 == exclude.areaKm2)

private fun drawNext(
    fullList: List<TerritoryCountry>,
    queue: MutableList<TerritoryCountry>,
    exclude: TerritoryCountry?,
    random: Random
): TerritoryCountry {
    if (queue.isEmpty()) queue.addAll(buildShuffledQueue(fullList, random))
    var next = queue.removeAt(0)
    var attempts = 0
    while (isTie(next, exclude) && attempts < fullList.size) {
        if (queue.isEmpty()) queue.addAll(buildShuffledQueue(fullList, random))
        if (queue.isEmpty()) break
        queue.add(next)
        next = queue.removeAt(0)
        attempts++
    }
    return next
}

/**
 * Mirrors the live game's own progression rule exactly (see the `onPick` correct-answer branch in
 * [TerritoryCompareScreen]): the champion is always retired after one round *regardless of area*,
 * and the challenger it just faced advances — not "whichever side is actually bigger survives".
 * Getting this wrong previously let a genuinely huge empire (e.g. the Umayyad Caliphate) stay
 * champion for many consecutive projected rounds, since it really is bigger than most opponents;
 * the always-retire rule is what caps any country at two consecutive appearances in real play, and
 * this preview of "what would have come next" on a failed run must honor the same cap.
 */
private fun buildUnplayedRounds(
    lastChallenger: TerritoryCountry,
    remainingQueue: List<TerritoryCountry>,
    startRound: Int,
    roundsToWin: Int
): List<Pair<TerritoryCountry, TerritoryCountry>> {
    val result = mutableListOf<Pair<TerritoryCountry, TerritoryCountry>>()
    var champion = lastChallenger
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
        champion = next
        round++
    }
    return result
}

private suspend fun persist(
    progressKey: String,
    championId: String,
    challengerId: String,
    queueIds: List<String>,
    round: Int,
    finished: Boolean,
    won: Boolean,
    history: List<HistoryEntry>
) {
    val state = TerritoryProgressState(championId, challengerId, queueIds, round, finished, won, history)
    QuizProgressStore.save(progressKey, Json.encodeToJsonElement(state).jsonObject)
}

/**
 * [battle] is set when this is the Cosmographer's question of the Daily Battle: one comparison of two countries, with
 * no survival run behind it (see the Annalist screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerritoryCompareScreen(onBack: () -> Unit = {}, battle: BattleLeg? = null) {
    val progressKey = remember { battle?.progressKey ?: PROGRESS_KEY }
    val roundsToWin = if (battle != null) 1 else ROUNDS_TO_WIN
    var allCountries by remember { mutableStateOf<List<TerritoryCountry>>(emptyList()) }
    val queue = remember { mutableListOf<TerritoryCountry>() }
    val history = remember { mutableStateListOf<HistoryEntry>() }
    // The plain game's seed must stay exactly the date's hash (every app version shuffles a day's pool the same way);
    // the battle's own pool needs only a seed of its own.
    val dailyRandom = remember {
        Random((if (battle == null) DailyClock.todayKey() else DailyClock.todayKey() + ":" + progressKey).hashCode().toLong())
    }

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
        // Exactly enough for one perfect run: the initial champion plus one new challenger per round. Tie-skip retries
        // need no extra headroom — [drawNext] reshuffles the same pool back into the queue once it empties. Kept
        // tight (not a large buffer like the old 60) so the picker's no-repeat window is honored: 11 a day is already
        // a tenth of the catalog.
        val ids = retryNetwork { DailyQuizRepository.getOrCreate(progressKey, dateKey, roundsToWin + 1) }
        val pool = retryNetwork { loadTerritories(ids) }
        allCountries = pool
        // Below 2, no non-self matchup is possible at all — bail to the loading state instead of
        // starting a trivially-tied round (this is what a degenerate get_or_create_daily_quiz pool
        // looked like in practice: a real production incident, see the 2026-09-25 fix).
        if (pool.size < 2) return@LaunchedEffect

        val stored = retryNetwork { QuizProgressStore.load(progressKey) }
        val state = stored?.let { Json.decodeFromJsonElement<TerritoryProgressState>(it) }
        val resumedChampion = state?.let { s -> pool.find { it.id == s.championId } }
        val resumedChallenger = state?.let { s -> pool.find { it.id == s.challengerId } }
        // Only trust stored progress if both ids still resolve against today's pool — under
        // normal operation they always do (a day's pool never changes once created), but this
        // guards against a manually-regenerated pool leaving stale progress pointing at ids that
        // no longer exist, which otherwise leaves champion/challenger null forever (an infinite
        // loading spinner, no way out) instead of just starting a fresh round.
        if (state != null && resumedChampion != null && resumedChallenger != null) {
            queue.addAll(state.queueIds.mapNotNull { id -> pool.find { it.id == id } })
            champion = resumedChampion
            challenger = resumedChallenger
            round = state.round
            won = state.won
            showResults = state.finished
            resumedAlreadyDone = state.finished
            history.addAll(state.history)
        } else {
            queue.addAll(buildShuffledQueue(pool, dailyRandom))
            val champ = queue.removeAt(0)
            val chall = drawNext(pool, queue, champ, dailyRandom)
            champion = champ
            challenger = chall
            round = 1
            persist(progressKey, champ.id, chall.id, queue.map { it.id }, 1, false, false, emptyList())
        }
    }

    MaterialTheme(colorScheme = gameColorScheme(AppColors.ForestDark)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { GameBarTitle(StringsGamesFlagTerritoryBlindmap.territory_title, null, battle) },
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
            GameBackgroundArt(
                "images/backgrounds/bg_territory_compare.png",
                MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset(y = (-90).dp)
            )
            val c = champion
            val ch = challenger
            when {
                c == null || ch == null -> HistoriqLoadingIndicator()
                showResults && battle != null -> BattleLegFinished(battle, battleSurvivalScore(history.count { it.correct }, roundsToWin))
                showResults && resumedAlreadyDone -> TerritoryResults(
                    won = won,
                    history = history,
                    allCountries = allCountries,
                    lastChallenger = ch,
                    remainingQueue = queue.toList(),
                    onBack = onBack
                )
                showResults -> AdGatedResults {
                    TerritoryResults(
                        won = won,
                        history = history,
                        allCountries = allCountries,
                        lastChallenger = ch,
                        remainingQueue = queue.toList(),
                        onBack = onBack
                    )
                }
                else -> TerritoryQuestion(
                    champion = c,
                    challenger = ch,
                    round = battle?.number ?: round,
                    totalRounds = battle?.total ?: ROUNDS_TO_WIN,
                    inBattle = battle != null,
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
                        if (isCorrect) { Haptics.correct(); BubbleSoundPlayer.playCorrectChirp() } else { Haptics.wrong(); BubbleSoundPlayer.playWrongTone() }

                        if (!isCorrect) {
                            won = false
                        } else if (round == roundsToWin) {
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
                        if (!correct || round == roundsToWin) {
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
            persist(progressKey, c.id, ch.id, queue.map { it.id }, round, showResults, won, history.toList())
        }
    }
}

@Composable
private fun TerritoryQuestion(
    champion: TerritoryCountry,
    challenger: TerritoryCountry,
    round: Int,
    totalRounds: Int,
    inBattle: Boolean,
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
        Text(StringsGamesFlagTerritoryBlindmap.territory_round_counter(round, totalRounds), style = MaterialTheme.typography.titleMedium)
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
                when {
                    correct -> StringsGamesFlagTerritoryBlindmap.territory_correct
                    inBattle -> StringsDailyBattle.battle_wrong
                    else -> StringsGamesFlagTerritoryBlindmap.territory_wrong_game_over
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onContinue) {
                val last = if (inBattle) round == totalRounds else !correct || round == ROUNDS_TO_WIN
                Text(if (last) StringsGamesFlagTerritoryBlindmap.territory_see_results else StringsGamesFlagTerritoryBlindmap.territory_next_round)
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
            val language = LocalAppLanguage.current.code
            Text(
                country.localizedCountry(language),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                country.localizedEra(language),
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
    lastChallenger: TerritoryCountry,
    remainingQueue: List<TerritoryCountry>,
    onBack: () -> Unit
) {
    val language = LocalAppLanguage.current.code
    val score = history.count { it.correct }
    val (rank, totalPlayers, finalDistribution, topPercent) = rememberLiveRank(PROGRESS_KEY, GameKeys.TERRITORY_COMPARE, score.toDouble(), score == ROUNDS_TO_WIN)
    fun scorePos(v: Double): Float = (v / ROUNDS_TO_WIN.toDouble()).toFloat().coerceIn(0f, 1f)
    val unplayedMatchups = remember(won, lastChallenger, remainingQueue) {
        if (won) emptyList() else buildUnplayedRounds(lastChallenger, remainingQueue, history.size + 1, ROUNDS_TO_WIN)
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
            rank = rank,
            totalPlayers = totalPlayers,
            topPercent = topPercent,
            accentColor = MaterialTheme.colorScheme.primary,
            maxScoreText = StringsHomeShared.results_of_points_max(formatThousands(ROUNDS_TO_WIN.toLong())),
            subtitle = if (won) StringsGamesFlagTerritoryBlindmap.territory_survived_all(ROUNDS_TO_WIN) else StringsGamesFlagTerritoryBlindmap.territory_made_it_to_round(history.size),
            distribution = finalDistribution.map { scorePos(it) },
            yourPosition = scorePos(score.toDouble()),
            distributionMinLabel = "0",
            distributionMaxLabel = formatThousands(ROUNDS_TO_WIN.toLong())
        )
        HorizontalDivider()
        history.forEachIndexed { i, h ->
            val champ = allCountries.find { it.id == h.championId }
            val chall = allCountries.find { it.id == h.challengerId }
            Column(Modifier.fillMaxWidth()) {
                Text(
                    StringsGamesFlagTerritoryBlindmap.territory_round_matchup(i + 1, champ?.localizedCountry(language) ?: "?", chall?.localizedCountry(language) ?: "?"),
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
                        color = MaterialTheme.colorScheme.errorText()
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
                        StringsGamesFlagTerritoryBlindmap.territory_round_matchup(history.size + 1 + offset, champ.localizedCountry(language), chall.localizedCountry(language)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = dimmed
                    )
                    Text(
                        StringsGamesFlagTerritoryBlindmap.territory_unplayed_bigger(bigger.localizedCountry(language)),
                        style = MaterialTheme.typography.bodySmall,
                        color = dimmed
                    )
                }
            }
        }
        Button(onClick = onBack) { Text(StringsGamesFlagTerritoryBlindmap.territory_back_to_menu) }
    }
}
