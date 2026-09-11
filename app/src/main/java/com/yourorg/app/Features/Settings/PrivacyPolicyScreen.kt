package com.yourorg.app.Features.Settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.R

private const val CONTACT_EMAIL = "mate.konye@gmail.com"

/**
 * A real, full screen rather than a dialog — this content is long enough (13 sections) that a
 * dialog would cram it, and nested-in-Settings navigation (not a deep link, not an exported
 * intent) is the whole point: this is reachable only by tapping the row in SettingsScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_privacy_policy_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.privacy_effective_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    stringResource(R.string.privacy_lede),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Section(stringResource(R.string.privacy_section_overview_title)) {
                BodyText(stringResource(R.string.privacy_overview_body))
                InfoCard(
                    label = stringResource(R.string.privacy_card_signed_in_label),
                    body = stringResource(R.string.privacy_card_signed_in_body),
                    accent = MaterialTheme.colorScheme.primary
                )
                InfoCard(
                    label = stringResource(R.string.privacy_card_guest_label),
                    body = stringResource(R.string.privacy_card_guest_body),
                    accent = AppColors.Success
                )
                BodyText(stringResource(R.string.privacy_overview_footer))
            }

            Section(stringResource(R.string.privacy_section_account_title)) {
                BodyText(stringResource(R.string.privacy_account_intro))
                BulletList(
                    stringResource(R.string.privacy_account_item_name),
                    stringResource(R.string.privacy_account_item_email),
                    stringResource(R.string.privacy_account_item_provider),
                    stringResource(R.string.privacy_account_item_code),
                    stringResource(R.string.privacy_account_item_id)
                )
                InfoCard(
                    label = stringResource(R.string.privacy_account_mock_label),
                    body = stringResource(R.string.privacy_account_mock_body),
                    accent = MaterialTheme.colorScheme.primary
                )
            }

            Section(stringResource(R.string.privacy_section_gameplay_title)) {
                BodyText(stringResource(R.string.privacy_gameplay_intro))
                BulletList(
                    stringResource(R.string.privacy_gameplay_item_progress),
                    stringResource(R.string.privacy_gameplay_item_score),
                    stringResource(R.string.privacy_gameplay_item_stats)
                )
                BodyText(stringResource(R.string.privacy_gameplay_footer))
            }

            Section(stringResource(R.string.privacy_section_friends_title)) {
                BodyText(stringResource(R.string.privacy_friends_body))
            }

            Section(stringResource(R.string.privacy_section_security_title)) {
                BodyText(stringResource(R.string.privacy_security_body))
            }

            Section(stringResource(R.string.privacy_section_device_title)) {
                BodyText(stringResource(R.string.privacy_device_body))
            }

            Section(stringResource(R.string.privacy_section_guest_title)) {
                BodyText(stringResource(R.string.privacy_guest_body))
            }

            Section(stringResource(R.string.privacy_section_sharing_title)) {
                BodyText(stringResource(R.string.privacy_sharing_body1))
                BodyText(stringResource(R.string.privacy_sharing_body2))
            }

            Section(stringResource(R.string.privacy_section_storage_title)) {
                BodyText(stringResource(R.string.privacy_storage_body))
            }

            Section(stringResource(R.string.privacy_section_choices_title)) {
                BulletList(
                    stringResource(R.string.privacy_choices_item_name),
                    stringResource(R.string.privacy_choices_item_toggles),
                    stringResource(R.string.privacy_choices_item_delete),
                    stringResource(R.string.privacy_choices_item_guest)
                )
            }

            Section(stringResource(R.string.privacy_section_children_title)) {
                BodyText(stringResource(R.string.privacy_children_body))
            }

            Section(stringResource(R.string.privacy_section_changes_title)) {
                BodyText(stringResource(R.string.privacy_changes_body))
            }

            val context = LocalContext.current
            Section(stringResource(R.string.privacy_section_contact_title)) {
                BodyText(stringResource(R.string.privacy_contact_body))
                Text(
                    CONTACT_EMAIL,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL"))
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun BulletList(vararg items: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, body: String, accent: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row {
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxSize()
                    .background(accent)
            )
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
