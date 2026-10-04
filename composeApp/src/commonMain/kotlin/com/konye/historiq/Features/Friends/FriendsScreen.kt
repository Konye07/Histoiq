package com.konye.historiq.Features.Friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.StringsSocial
import com.konye.historiq.Core.Network.networkOrElse
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.HistoriqLoadingIndicator
import com.konye.historiq.Core.Theme.MascotEmptyState
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Quiz.ActiveEra
import com.konye.historiq.Data.Quiz.DailyClock
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.ScoreHistoryStore
import com.konye.historiq.Data.Social.AddFriendResult
import com.konye.historiq.Data.Social.FriendConnection
import com.konye.historiq.Data.Social.FriendsRepository
import com.konye.historiq.Data.Social.FriendsStore
import com.konye.historiq.Features.Play.Shared.EraFilterChip
import kotlinx.coroutines.launch

private data class LeaderboardRow(val name: String, val score: Double?, val isMe: Boolean)

@Composable
fun FriendsScreen() {
    var era by remember { mutableStateOf(ActiveEra.current()) }
    var selectedGame by remember { mutableStateOf(GameKeys.YEAR_GUESS) }
    // An era game mode has only Annalist, Herald and Chancellor, so picking an era drops the other games from the list.
    val games = if (era == null) GameKeys.ALL else GameKeys.ALL.filter { it in GameKeys.ERA_FILTERABLE }
    val activeGame = if (selectedGame in games) selectedGame else GameKeys.YEAR_GUESS
    val selectedModeKey = GameKeys.modeKey(activeGame, era)
    var selectedDayOffset by remember { mutableStateOf(0) }
    var friendsVersion by remember { mutableStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    val myAccount = remember { AccountRepository.currentAccount() }
    var acceptedFriends by remember { mutableStateOf<List<FriendConnection>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<FriendConnection>>(emptyList()) }
    var rows by remember { mutableStateOf<List<LeaderboardRow>>(emptyList()) }
    val youFallback = StringsSocial.friends_you_fallback

    LaunchedEffect(friendsVersion) {
        if (myAccount != null) {
            networkOrElse(Unit) {
                acceptedFriends = FriendsStore.accepted(myAccount.id)
                incomingRequests = FriendsStore.incoming(myAccount.id)
            }
        }
    }

    LaunchedEffect(selectedModeKey, selectedDayOffset, acceptedFriends) {
        loading = true
        val dateKey = DailyClock.dateKey(selectedDayOffset)
        val myScore = networkOrElse<Double?>(null) { ScoreHistoryStore.resultFor(selectedModeKey, dateKey) }
        val list = mutableListOf(LeaderboardRow(myAccount?.displayName ?: youFallback, myScore, isMe = true))
        acceptedFriends.forEach { f ->
            val friendScore = networkOrElse<Double?>(null) { ScoreHistoryStore.resultFor(f.otherId, selectedModeKey, dateKey) }
            list.add(LeaderboardRow(f.displayName, friendScore, isMe = false))
        }
        rows = if (activeGame == GameKeys.CASUALTIES_MULTIPLIER) {
            // Lower is better for this game's stored score (a penalty multiplier), unlike every other game.
            list.sortedBy { it.score ?: Double.MAX_VALUE }
        } else {
            list.sortedByDescending { it.score ?: -1.0 }
        }
        loading = false
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(StringsSocial.friends_title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                EraFilterChip(era, onEraChange = { era = it })
            }
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(games) { key ->
                    val color = GameKeys.color(GameKeys.modeKey(key, era))
                    FilterChip(
                        selected = activeGame == key,
                        onClick = { selectedGame = key },
                        label = { Text(GameKeys.roleName(key)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = contrastingOn(color)
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2, 3).forEach { offset ->
                    FilterChip(
                        selected = selectedDayOffset == offset,
                        onClick = { selectedDayOffset = offset },
                        label = {
                            Text(
                                if (offset == 0) StringsSocial.friends_filter_today
                                else StringsSocial.friends_filter_days_ago(offset)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                StringsSocial.friends_role_title_format(GameKeys.roleName(activeGame), GameKeys.title(selectedModeKey)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            if (loading) {
                HistoriqLoadingIndicator(Modifier.padding(16.dp))
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(rows) { row ->
                        LeaderboardRowItem(row, selectedModeKey)
                    }
                    if (myAccount != null && acceptedFriends.isEmpty()) {
                        item {
                            MascotEmptyState(
                                StringsSocial.friends_no_friends_yet,
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (myAccount != null) {
            val addFriendColor = if (incomingRequests.isNotEmpty()) AppColors.Maroon else MaterialTheme.colorScheme.primary
            Button(
                onClick = { showAddFriendDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = addFriendColor,
                    contentColor = contrastingOn(addFriendColor)
                )
            ) {
                Text(
                    if (incomingRequests.isNotEmpty()) StringsSocial.friends_add_friend_pending(incomingRequests.size)
                    else StringsSocial.friends_add_friend
                )
            }
        } else {
            Text(
                StringsSocial.friends_guest_notice,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            incomingRequests = incomingRequests,
            onDismiss = {
                showAddFriendDialog = false
                friendsVersion++
            },
            onConnectionsChanged = { friendsVersion++ }
        )
    }
}

@Composable
private fun LeaderboardRowItem(row: LeaderboardRow, gameKey: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isMe) GameKeys.color(gameKey).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (row.isMe) StringsSocial.friends_you_suffix(row.name) else row.name,
                fontWeight = if (row.isMe) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                row.score?.let { GameKeys.formatScore(gameKey, it) } ?: StringsSocial.friends_not_played,
                style = MaterialTheme.typography.bodyLarge,
                color = if (row.score == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AddFriendDialog(
    incomingRequests: List<FriendConnection>,
    onDismiss: () -> Unit,
    onConnectionsChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var codeInput by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val feedbackAdded = StringsSocial.friends_feedback_added
    val feedbackAlreadyConnected = StringsSocial.friends_feedback_already_connected
    val feedbackOwnCode = StringsSocial.friends_feedback_own_code
    val feedbackNotFound = StringsSocial.friends_feedback_not_found
    val feedbackOffline = StringsHomeShared.offline_banner_message

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(StringsSocial.friends_add_friend) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(StringsSocial.friends_add_friend_instructions, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.filter { c -> c.isDigit() }.take(9) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(StringsSocial.friends_code_placeholder) }
                    )
                    Button(
                        onClick = {
                            sending = true
                            feedback = null
                            scope.launch {
                                val result = networkOrElse<AddFriendResult?>(null) { FriendsRepository.addFriendByCode(codeInput) }
                                sending = false
                                feedback = when (result) {
                                    AddFriendResult.ADDED -> feedbackAdded
                                    AddFriendResult.ALREADY_CONNECTED -> feedbackAlreadyConnected
                                    AddFriendResult.IS_YOUR_OWN_CODE -> feedbackOwnCode
                                    AddFriendResult.NOT_FOUND -> feedbackNotFound
                                    null -> feedbackOffline
                                }
                                if (result == AddFriendResult.ADDED) {
                                    codeInput = ""
                                    onConnectionsChanged()
                                }
                            }
                        },
                        enabled = codeInput.length == 9 && !sending
                    ) {
                        Text(if (sending) StringsSocial.friends_sending else StringsSocial.friends_send)
                    }
                }
                feedback?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }

                if (incomingRequests.isNotEmpty()) {
                    HorizontalDivider()
                    Text(StringsSocial.friends_incoming_requests, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    incomingRequests.forEach { req ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.displayName, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {
                                    scope.launch {
                                        networkOrElse(Unit) { FriendsRepository.acceptIncoming(req.friendshipId) }
                                        onConnectionsChanged()
                                    }
                                }) { Text(StringsSocial.friends_accept) }
                                TextButton(onClick = {
                                    scope.launch {
                                        networkOrElse(Unit) { FriendsRepository.declineIncoming(req.friendshipId) }
                                        onConnectionsChanged()
                                    }
                                }) { Text(StringsSocial.friends_decline) }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(StringsSocial.friends_close) }
        }
    )
}
