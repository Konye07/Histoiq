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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Locale.findActivity
import com.yourorg.app.Core.Notifications.ReminderScheduler
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Data.Social.formatFriendCode
import com.yourorg.app.R
import kotlinx.coroutines.launch

private data class LanguageOption(val code: String, val label: String, val available: Boolean)

private val LANGUAGES = listOf(
    LanguageOption("en", "English", available = true),
    LanguageOption("es", "Español", available = true),
    LanguageOption("fr", "Français", available = true),
    LanguageOption("de", "Deutsch", available = true),
    LanguageOption("it", "Italiano", available = true)
)

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

    fun selectLanguage(ctx: android.content.Context, code: String) {
        if (code == language) return
        language = code
        SettingsStore.setLanguage(ctx, code)
        ctx.findActivity()?.recreate()
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
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

        // Display Name
        SectionTitle(stringResource(R.string.settings_display_name_title))
        Text(
            stringResource(R.string.settings_display_name_subtitle),
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
                Text(stringResource(R.string.settings_save))
            }
        }
        if (account != null) {
            Text(
                stringResource(R.string.settings_friend_code, formatFriendCode(account!!.friendCode)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        HorizontalDivider()

        // Languages
        SectionTitle(stringResource(R.string.settings_languages_title))
        Column {
            LANGUAGES.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = option.available) { selectLanguage(context, option.code) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = language == option.code,
                        onClick = { selectLanguage(context, option.code) },
                        enabled = option.available
                    )
                    Text(
                        option.label,
                        modifier = Modifier.weight(1f),
                        color = if (option.available) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline
                    )
                    if (!option.available) {
                        Text(stringResource(R.string.settings_coming_soon), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        HorizontalDivider()

        // Preferences
        SectionTitle(stringResource(R.string.settings_preferences_title))
        SettingToggleRow(
            title = stringResource(R.string.settings_haptics_title),
            subtitle = stringResource(R.string.settings_haptics_subtitle),
            checked = hapticsEnabled,
            onCheckedChange = {
                hapticsEnabled = it
                SettingsStore.setHapticsEnabled(context, it)
            }
        )
        SettingToggleRow(
            title = stringResource(R.string.settings_daily_reminder_title),
            subtitle = stringResource(R.string.settings_daily_reminder_subtitle),
            checked = dailyReminderEnabled,
            onCheckedChange = ::onDailyReminderToggle
        )

        HorizontalDivider()

        // Account
        SectionTitle(stringResource(R.string.settings_account_title))
        if (account != null) {
            Text(stringResource(R.string.settings_email_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(account!!.email, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { showPrivacyDialog = true }
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.settings_privacy_policy_title), style = MaterialTheme.typography.bodyMedium)
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
            Text(stringResource(R.string.settings_sign_out))
        }
        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_delete_account))
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.settings_privacy_policy_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.settings_privacy_policy_text), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text(stringResource(R.string.settings_close)) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
            text = {
                Text(stringResource(R.string.settings_delete_confirm_body))
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        AccountRepository.deleteAccount()
                        showDeleteConfirm = false
                        onSignedOut()
                    }
                }) { Text(stringResource(R.string.settings_delete_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.settings_cancel)) }
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
