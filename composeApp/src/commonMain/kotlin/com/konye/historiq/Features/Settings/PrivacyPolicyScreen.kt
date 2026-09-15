package com.konye.historiq.Features.Settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsAuthSettings
import com.konye.historiq.Core.Localization.StringsPrivacyPolicy
import com.konye.historiq.Core.Platform.openEmailComposer
import com.konye.historiq.Core.Platform.openUrl
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource

private const val CONTACT_EMAIL = "mate.konye@gmail.com"
private const val PRIVACY_POLICY_URL = "https://historiq-privacy.mate-konye.workers.dev/privacy-policy.html"

/**
 * A real, full screen rather than a dialog — this content is long enough (15 sections) that a
 * dialog would cram it, and nested-in-Settings navigation (not a deep link, not an exported
 * intent) is the whole point: this is reachable only by tapping the row in SettingsScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StringsAuthSettings.settings_privacy_policy_title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = null)
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
                    StringsPrivacyPolicy.privacy_effective_date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    StringsPrivacyPolicy.privacy_lede,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Section(StringsPrivacyPolicy.privacy_section_overview_title) {
                BodyText(StringsPrivacyPolicy.privacy_overview_body)
                InfoCard(
                    label = StringsPrivacyPolicy.privacy_card_signed_in_label,
                    body = StringsPrivacyPolicy.privacy_card_signed_in_body,
                    accent = MaterialTheme.colorScheme.primary
                )
                InfoCard(
                    label = StringsPrivacyPolicy.privacy_card_guest_label,
                    body = StringsPrivacyPolicy.privacy_card_guest_body,
                    accent = AppColors.Success
                )
                BodyText(StringsPrivacyPolicy.privacy_overview_footer)
            }

            Section(StringsPrivacyPolicy.privacy_section_account_title) {
                BodyText(StringsPrivacyPolicy.privacy_account_intro)
                BulletList(
                    StringsPrivacyPolicy.privacy_account_item_name,
                    StringsPrivacyPolicy.privacy_account_item_email,
                    StringsPrivacyPolicy.privacy_account_item_avatar,
                    StringsPrivacyPolicy.privacy_account_item_provider,
                    StringsPrivacyPolicy.privacy_account_item_code,
                    StringsPrivacyPolicy.privacy_account_item_id
                )
                InfoCard(
                    label = StringsPrivacyPolicy.privacy_account_google_label,
                    body = StringsPrivacyPolicy.privacy_account_google_body,
                    accent = AppColors.Success
                )
                InfoCard(
                    label = StringsPrivacyPolicy.privacy_account_mock_label,
                    body = StringsPrivacyPolicy.privacy_account_mock_body,
                    accent = MaterialTheme.colorScheme.primary
                )
            }

            Section(StringsPrivacyPolicy.privacy_section_gameplay_title) {
                BodyText(StringsPrivacyPolicy.privacy_gameplay_intro)
                BulletList(
                    StringsPrivacyPolicy.privacy_gameplay_item_progress,
                    StringsPrivacyPolicy.privacy_gameplay_item_score
                )
                BodyText(StringsPrivacyPolicy.privacy_gameplay_footer)
            }

            Section(StringsPrivacyPolicy.privacy_section_friends_title) {
                BodyText(StringsPrivacyPolicy.privacy_friends_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_advertising_title) {
                BodyText(StringsPrivacyPolicy.privacy_advertising_body1)
                BodyText(StringsPrivacyPolicy.privacy_advertising_body2)
                BodyText(StringsPrivacyPolicy.privacy_advertising_body3)
            }

            Section(StringsPrivacyPolicy.privacy_section_subscription_title) {
                BodyText(StringsPrivacyPolicy.privacy_subscription_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_security_title) {
                BodyText(StringsPrivacyPolicy.privacy_security_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_device_title) {
                BodyText(StringsPrivacyPolicy.privacy_device_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_guest_title) {
                BodyText(StringsPrivacyPolicy.privacy_guest_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_sharing_title) {
                BodyText(StringsPrivacyPolicy.privacy_sharing_body1)
                BodyText(StringsPrivacyPolicy.privacy_sharing_body2)
                BodyText(StringsPrivacyPolicy.privacy_sharing_body3)
            }

            Section(StringsPrivacyPolicy.privacy_section_storage_title) {
                BodyText(StringsPrivacyPolicy.privacy_storage_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_choices_title) {
                BulletList(
                    StringsPrivacyPolicy.privacy_choices_item_name,
                    StringsPrivacyPolicy.privacy_choices_item_toggles,
                    StringsPrivacyPolicy.privacy_choices_item_subscription,
                    StringsPrivacyPolicy.privacy_choices_item_delete,
                    StringsPrivacyPolicy.privacy_choices_item_guest
                )
            }

            Section(StringsPrivacyPolicy.privacy_section_children_title) {
                BodyText(StringsPrivacyPolicy.privacy_children_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_changes_title) {
                BodyText(StringsPrivacyPolicy.privacy_changes_body)
            }

            Section(StringsPrivacyPolicy.privacy_section_contact_title) {
                BodyText(StringsPrivacyPolicy.privacy_contact_body)
                Text(
                    CONTACT_EMAIL,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        openEmailComposer(CONTACT_EMAIL)
                    }
                )
                BodyText(StringsPrivacyPolicy.privacy_view_online_label)
                Text(
                    PRIVACY_POLICY_URL,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        openUrl(PRIVACY_POLICY_URL)
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
