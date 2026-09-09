package com.yourorg.app.Features.Friends

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Quiz.DailyClock
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Social.AddFriendResult
import com.yourorg.app.Data.Social.FriendConnection
import com.yourorg.app.Data.Social.FriendsRepository
import com.yourorg.app.Data.Social.FriendsStore
import com.yourorg.app.Data.Social.demoScoreFor
import kotlinx.coroutines.launch

private data class LeaderboardRow(val name: String, val score: Double?, val isMe: Boolean)

@Composable
fun FriendsScreen() {
    val context = LocalContext.current
    var selectedGame by remember { mutableStateOf(GameKeys.YEAR_GUESS) }
    var selectedDayOffset by remember { mutableStateOf(0) }
    var friendsVersion by remember { mutableStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    val myAccount = remember { AccountRepository.currentAccount(context) }
    val acceptedFriends = remember(friendsVersion) { FriendsStore.accepted(context) }
    val incomingRequests = remember(friendsVersion) { FriendsStore.incoming(context) }

    val dateKey = remember(selectedDayOffset) { DailyClock.dateKey(selectedDayOffset) }
    val rows = remember(selectedGame, dateKey, acceptedFriends) {
        val myScore = ScoreHistoryStore.resultFor(context, selectedGame, dateKey)
        val list = mutableListOf(LeaderboardRow(myAccount?.displayName ?: "You", myScore, isMe = true))
        acceptedFriends.forEach { f ->
            list.add(LeaderboardRow(f.displayName, demoScoreFor(f.code, selectedGame, dateKey), isMe = false))
        }
        list.sortedByDescending { it.score ?: -1.0 }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Friends", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GameKeys.ALL) { key ->
                    val color = GameKeys.color(key)
                    FilterChip(
                        selected = selectedGame == key,
                        onClick = { selectedGame = key },
                        label = { Text(GameKeys.roleName(key)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = AppColors.Cream
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
                        label = { Text(if (offset == 0) "Today" else "-$offset d") }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "${GameKeys.roleName(selectedGame)} — ${GameKeys.title(selectedGame)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            LazyColumn(Modifier.weight(1f)) {
                items(rows) { row ->
                    LeaderboardRowItem(row, selectedGame)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        Button(
            onClick = { showAddFriendDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (incomingRequests.isNotEmpty()) AppColors.Maroon else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (incomingRequests.isNotEmpty()) "Add Friend (${incomingRequests.size} pending)" else "Add Friend")
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
                if (row.isMe) "${row.name} (You)" else row.name,
                fontWeight = if (row.isMe) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                row.score?.let { GameKeys.formatScore(gameKey, it) } ?: "Not played",
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var codeInput by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Enter a friend's 9-digit code (find your own in Settings).", style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.filter { c -> c.isDigit() }.take(9) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("123456789") }
                    )
                    Button(
                        onClick = {
                            sending = true
                            feedback = null
                            scope.launch {
                                val result = FriendsRepository.addFriendByCode(context, codeInput)
                                sending = false
                                feedback = when (result) {
                                    AddFriendResult.ADDED -> "Friend added!"
                                    AddFriendResult.ALREADY_CONNECTED -> "Already connected."
                                    AddFriendResult.IS_YOUR_OWN_CODE -> "That's your own code."
                                    AddFriendResult.NOT_FOUND -> "No player found with that code."
                                }
                                if (result == AddFriendResult.ADDED) {
                                    codeInput = ""
                                    onConnectionsChanged()
                                }
                            }
                        },
                        enabled = codeInput.length == 9 && !sending
                    ) {
                        Text(if (sending) "..." else "Send")
                    }
                }
                feedback?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }

                if (incomingRequests.isNotEmpty()) {
                    HorizontalDivider()
                    Text("Incoming Requests", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    incomingRequests.forEach { req ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.displayName, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {
                                    FriendsRepository.acceptIncoming(context, req.code, req.displayName)
                                    onConnectionsChanged()
                                }) { Text("Accept") }
                                TextButton(onClick = {
                                    FriendsRepository.declineIncoming(context, req.code)
                                    onConnectionsChanged()
                                }) { Text("Decline") }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
