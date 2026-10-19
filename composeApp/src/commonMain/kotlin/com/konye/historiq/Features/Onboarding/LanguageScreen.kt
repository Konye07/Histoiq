package com.konye.historiq.Features.Onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.AppLanguage
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.LocalOnLanguageChange
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.StringsTutorial
import com.konye.historiq.Core.Theme.AppLogoImage

/**
 * The first thing a fresh install shows (see RootApp), before sign-in and the tutorial: which language to use. The
 * screen starts on the device's own language, and tapping another one switches the whole app on the spot, this screen
 * included, so the question and the tutorial after it are already in the language the player picked.
 */
@Composable
fun LanguageScreen(onContinue: () -> Unit) {
    val current = LocalAppLanguage.current
    val onLanguageChange = LocalOnLanguageChange.current

    // Nothing above this screen paints a background (same as AuthScreen), so it brings its own surface.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrolls, so a large text size or a short screen never pushes the last language or the button out of reach.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                // On a short screen (or at a large text size) the logo gives way, so more of the languages show without scrolling.
                val compact = maxHeight < 560.dp
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!compact) {
                        AppLogoImage(Modifier.size(96.dp))
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(
                        StringsTutorial.language_title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        StringsTutorial.language_subtitle(StringsHomeShared.nav_settings),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(if (compact) 16.dp else 24.dp))
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppLanguage.entries.forEach { language ->
                            LanguageRow(language, selected = language == current, onSelect = { onLanguageChange(language) })
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                // Picking nothing keeps the language the screen opened on, which still has to be stored as the player's.
                onClick = {
                    onLanguageChange(current)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(StringsTutorial.language_continue)
            }
        }
    }
}

@Composable
private fun LanguageRow(language: AppLanguage, selected: Boolean, onSelect: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.5f))
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            language.nativeName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        // The selected row is the primary container, so the dot takes the color made to be read on it.
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = colors.onPrimaryContainer, unselectedColor = colors.onSurfaceVariant)
        )
    }
}
