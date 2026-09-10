package com.yourorg.app.Features.Settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import com.yourorg.app.Core.Notifications.ReminderScheduler
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Data.Social.formatFriendCode
import kotlinx.coroutines.launch

private data class LanguageOption(val code: String, val label: String, val available: Boolean)

private val LANGUAGES = listOf(
    LanguageOption("en", "English", available = true),
    LanguageOption("es", "Spanish", available = false),
    LanguageOption("fr", "French", available = false),
    LanguageOption("de", "German", available = false),
    LanguageOption("it", "Italian", available = false),
    LanguageOption("ar", "Arabic", available = false)
)

private const val PRIVACY_POLICY_TEXT = """This app stores your account (display name, email, sign-in provider, friend code), friends, and daily quiz progress/scores in a hosted Supabase database, not just on this device — that's what lets stats and friend leaderboards work across your devices and let real friends see each other's scores. It is not shared with or sold to any third party.

Sign-in is currently a mock — no real Google or Apple account data is read or transmitted — but each device still gets a real, unique, anonymous account identity so your data can't be read or edited by anyone else.

Every sign-in is logged with its IP address and device info in a security audit log, used only to detect abuse; this log is never readable by any client, only for administrative security review.

Deleting your account permanently removes all of the above (profile, friends, stats, progress, history) from the hosted database — this cannot be undone."""

@Composable
fun SettingsScreen(onSignedOut: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var account by remember { mutableStateOf(AccountRepository.currentAccount()) }

    var displayName by remember { mutableStateOf(account?.displayName ?: "") }
    var language by remember { mutableStateOf(SettingsStore.language(context)) }
    var hapticsEnabled by remember { mutableStateOf(SettingsStore.hapticsEnabled(context)) }
    var dailyReminderEnabled by remember { mutableStateOf(SettingsStore.dailyReminderEnabled(context)) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            SettingsStore.setDailyReminderEnabled(context, true)
            dailyReminderEnabled = true
            ReminderScheduler.schedule(context)
        } else {
            dailyReminderEnabled = false
        }
    }

    fun onDailyReminderToggle(enabled: Boolean) {
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                SettingsStore.setDailyReminderEnabled(context, true)
                dailyReminderEnabled = true
                ReminderScheduler.schedule(context)
            }
        } else {
            SettingsStore.setDailyReminderEnabled(context, false)
            dailyReminderEnabled = false
            ReminderScheduler.cancel(context)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        // Display Name
        SectionTitle("Display Name")
        Text(
            "Shown on your profile and friend leaderboards.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (displayName.isNotBlank()) {
                        val newName = displayName.trim()
                        scope.launch { account = AccountRepository.updateDisplayName(newName) }
                    }
                },
                enabled = displayName.isNotBlank() && displayName != account?.displayName
            ) {
                Text("Save")
            }
        }
        if (account != null) {
            Text(
                "Your friend code: ${formatFriendCode(account!!.friendCode)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        HorizontalDivider()

        // Languages
        SectionTitle("Languages")
        Column {
            LANGUAGES.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = option.available) {
                            language = option.code
                            SettingsStore.setLanguage(context, option.code)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = language == option.code,
                        onClick = { if (option.available) { language = option.code; SettingsStore.setLanguage(context, option.code) } },
                        enabled = option.available
                    )
                    Text(
                        option.label,
                        modifier = Modifier.weight(1f),
                        color = if (option.available) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline
                    )
                    if (!option.available) {
                        Text("Coming soon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        HorizontalDivider()

        // Preferences
        SectionTitle("Preferences")
        SettingToggleRow(
            title = "Haptics",
            subtitle = "Buzz on right/wrong answers, perfect scores, and menu navigation.",
            checked = hapticsEnabled,
            onCheckedChange = {
                hapticsEnabled = it
                SettingsStore.setHapticsEnabled(context, it)
            }
        )
        SettingToggleRow(
            title = "Daily Reminder",
            subtitle = "A once-a-day nudge to play today's quizzes.",
            checked = dailyReminderEnabled,
            onCheckedChange = ::onDailyReminderToggle
        )

        HorizontalDivider()

        // Account
        SectionTitle("Account")
        if (account != null) {
            Text("Email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(account!!.email, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { showPrivacyDialog = true }
                .padding(vertical = 8.dp)
        ) {
            Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                scope.launch {
                    AccountRepository.signOut()
                    onSignedOut()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Account")
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(PRIVACY_POLICY_TEXT, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete account?") },
            text = {
                Text(
                    "This permanently deletes your account and ALL of your game data — every quiz's progress and " +
                        "score history. This cannot be undone and none of it can be recovered afterward."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        AccountRepository.deleteAccount()
                        showDeleteConfirm = false
                        onSignedOut()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
