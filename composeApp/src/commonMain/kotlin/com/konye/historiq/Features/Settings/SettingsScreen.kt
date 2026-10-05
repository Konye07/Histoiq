package com.konye.historiq.Features.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.isPrivacyOptionsRequired
import com.konye.historiq.Core.Ads.showPrivacyOptionsForm
import com.konye.historiq.Core.Localization.AppLanguage
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.LocalOnLanguageChange
import com.konye.historiq.Core.Localization.StringsAuthSettings
import com.konye.historiq.Core.Network.networkOrElse
import com.konye.historiq.Core.Notifications.ReminderScheduler
import com.konye.historiq.Core.Notifications.rememberRequestNotificationPermission
import com.konye.historiq.Core.Theme.AppFontSize
import com.konye.historiq.Core.Theme.LocalAppFontSize
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Core.Theme.LocalOnDarkModeChange
import com.konye.historiq.Core.Theme.LocalOnFontSizeChange
import com.konye.historiq.Core.Theme.scaledBy
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Settings.SettingsStore
import com.konye.historiq.Data.Social.formatFriendCode
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
fun SettingsScreen(onSignedOut: () -> Unit = {}, onOpenPrivacyPolicy: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var account by remember { mutableStateOf(AccountRepository.currentAccount()) }

    var displayName by remember { mutableStateOf(account?.displayName ?: "") }
    val currentLanguage = LocalAppLanguage.current
    val onLanguageChange = LocalOnLanguageChange.current
    val isDarkTheme = LocalIsDarkTheme.current
    val onDarkModeChange = LocalOnDarkModeChange.current
    val currentFontSize = LocalAppFontSize.current
    val onFontSizeChange = LocalOnFontSizeChange.current
    var hapticsEnabled by remember { mutableStateOf(SettingsStore.hapticsEnabled()) }
    var dailyReminderEnabled by remember { mutableStateOf(SettingsStore.dailyReminderEnabled()) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val requestNotificationPermission = rememberRequestNotificationPermission { granted ->
        if (granted) {
            SettingsStore.setDailyReminderEnabled(true)
            dailyReminderEnabled = true
            ReminderScheduler.schedule()
        } else {
            dailyReminderEnabled = false
        }
    }

    fun selectLanguage(code: String) {
        if (code == currentLanguage.code) return
        onLanguageChange(AppLanguage.fromCode(code))
    }

    fun onDailyReminderToggle(enabled: Boolean) {
        if (enabled) {
            requestNotificationPermission()
        } else {
            SettingsStore.setDailyReminderEnabled(false)
            dailyReminderEnabled = false
            ReminderScheduler.cancel()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(StringsAuthSettings.settings_title, style = MaterialTheme.typography.headlineSmall)

        // Display Name
        SectionTitle(StringsAuthSettings.settings_display_name_title)
        Text(
            StringsAuthSettings.settings_display_name_subtitle,
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
                        scope.launch { networkOrElse(Unit) { account = AccountRepository.updateDisplayName(newName) } }
                    }
                },
                enabled = displayName.isNotBlank() && displayName != account?.displayName
            ) {
                Text(StringsAuthSettings.settings_save)
            }
        }
        if (account != null) {
            Text(
                StringsAuthSettings.settings_friend_code(formatFriendCode(account!!.friendCode)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        HorizontalDivider()

        // Languages
        SectionTitle(StringsAuthSettings.settings_languages_title)
        Column {
            LANGUAGES.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = option.available) { selectLanguage(option.code) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentLanguage.code == option.code,
                        onClick = { selectLanguage(option.code) },
                        enabled = option.available
                    )
                    Text(
                        option.label,
                        modifier = Modifier.weight(1f),
                        color = if (option.available) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline
                    )
                    if (!option.available) {
                        Text(StringsAuthSettings.settings_coming_soon, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        HorizontalDivider()

        // Text size
        SectionTitle(StringsAuthSettings.settings_font_size_title)
        Text(
            StringsAuthSettings.settings_font_size_subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFontSize.entries.forEach { option ->
                FontSizeTile(
                    option = option,
                    selected = currentFontSize == option,
                    onClick = { onFontSizeChange(option) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        HorizontalDivider()

        // Preferences
        SectionTitle(StringsAuthSettings.settings_preferences_title)
        SettingToggleRow(
            title = StringsAuthSettings.settings_haptics_title,
            subtitle = StringsAuthSettings.settings_haptics_subtitle,
            checked = hapticsEnabled,
            onCheckedChange = {
                hapticsEnabled = it
                SettingsStore.setHapticsEnabled(it)
            }
        )
        SettingToggleRow(
            title = StringsAuthSettings.settings_daily_reminder_title,
            subtitle = StringsAuthSettings.settings_daily_reminder_subtitle,
            checked = dailyReminderEnabled,
            onCheckedChange = ::onDailyReminderToggle
        )
        SettingToggleRow(
            title = StringsAuthSettings.settings_dark_mode_title,
            subtitle = StringsAuthSettings.settings_dark_mode_subtitle,
            checked = isDarkTheme,
            onCheckedChange = onDarkModeChange
        )

        HorizontalDivider()

        // Account
        SectionTitle(StringsAuthSettings.settings_account_title)
        if (account != null) {
            Text(StringsAuthSettings.settings_email_label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(account!!.email, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onOpenPrivacyPolicy() }
                .padding(vertical = 8.dp)
        ) {
            Text(StringsAuthSettings.settings_privacy_policy_title, style = MaterialTheme.typography.bodyMedium)
        }
        if (isPrivacyOptionsRequired()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyOptionsForm {} }
                    .padding(vertical = 8.dp)
            ) {
                Text(StringsAuthSettings.settings_privacy_choices_title, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (account != null) {
            Button(
                onClick = {
                    scope.launch {
                        AccountRepository.signOut()
                        onSignedOut()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(StringsAuthSettings.settings_sign_out)
            }
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(StringsAuthSettings.settings_delete_account)
            }
        } else {
            Text(
                StringsAuthSettings.settings_guest_notice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Button(
                onClick = { onSignedOut() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(StringsAuthSettings.settings_sign_in)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(StringsAuthSettings.settings_delete_confirm_title) },
            text = {
                Text(StringsAuthSettings.settings_delete_confirm_body)
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val deleted = networkOrElse(false) {
                            AccountRepository.deleteAccount()
                            true
                        }
                        showDeleteConfirm = false
                        if (deleted) onSignedOut()
                    }
                }) { Text(StringsAuthSettings.settings_delete_confirm_button) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(StringsAuthSettings.settings_cancel) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun fontSizeLabel(option: AppFontSize): String = when (option) {
    AppFontSize.SMALL -> StringsAuthSettings.settings_font_size_small
    AppFontSize.MEDIUM -> StringsAuthSettings.settings_font_size_medium
    AppFontSize.LARGE -> StringsAuthSettings.settings_font_size_large
}

/** One text size choice: an "Aa" sample at the size [option] gives, whichever size is active now, over its name. */
@Composable
private fun FontSizeTile(option: AppFontSize, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val active = LocalAppFontSize.current
    val shape = RoundedCornerShape(12.dp)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
    Column(
        modifier
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            // The theme's style is already at the active size, so rescale it to the one this tile stands for.
            Text("Aa", style = MaterialTheme.typography.headlineSmall.scaledBy(option.scale / active.scale), color = contentColor)
        }
        Text(
            fontSizeLabel(option),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
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
