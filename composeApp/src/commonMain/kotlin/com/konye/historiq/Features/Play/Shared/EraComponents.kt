package com.konye.historiq.Features.Play.Shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsEras
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.QuizEra
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_expand_more
import org.jetbrains.compose.resources.painterResource

/**
 * The Play screen's game-mode switcher: All-time first (the default the app opens in), then one entry per
 * era. Picking an era swaps the whole screen into that era's own game mode rather than filtering this one.
 */
@Composable
fun EraModeStrip(selected: QuizEra?, onSelect: (QuizEra?) -> Unit, modifier: Modifier = Modifier) {
    val options = remember { listOf<QuizEra?>(null) + QuizEra.entries }
    val listState = rememberLazyListState()
    LaunchedEffect(selected) { listState.animateScrollToItem(options.indexOf(selected)) }

    LazyRow(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = {
                    Haptics.tick()
                    onSelect(option)
                },
                label = { Text(StringsEras.label(option), maxLines = 1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * The statistics screens' era filter: "Era: All-time" until an era is picked. Unlike [EraModeStrip] this
 * only chooses which mode's numbers to look at, so it holds no state of its own.
 */
@Composable
fun EraFilterChip(era: QuizEra?, onEraChange: (QuizEra?) -> Unit, modifier: Modifier = Modifier) {
    var showPicker by remember { mutableStateOf(false) }

    FilterChip(
        selected = era != null,
        onClick = {
            Haptics.tick()
            showPicker = true
        },
        label = {
            Text(
                StringsEras.era_chip_label(StringsEras.label(era)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingIcon = {
            Icon(painterResource(Res.drawable.ic_expand_more), contentDescription = null, modifier = Modifier.size(18.dp))
        },
        modifier = modifier.widthIn(max = 240.dp)
    )

    if (showPicker) {
        EraPickerDialog(
            selected = era,
            onSelect = {
                onEraChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun EraPickerDialog(selected: QuizEra?, onSelect: (QuizEra?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(StringsEras.era_picker_title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    StringsEras.era_picker_note(
                        GameKeys.roleName(GameKeys.YEAR_GUESS),
                        GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
                        GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
                (listOf<QuizEra?>(null) + QuizEra.entries).forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == option, onClick = { onSelect(option) })
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(StringsEras.label(option), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                StringsEras.range(option),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(StringsHomeShared.home_close) }
        }
    )
}

/** A game screen's app-bar title: the game's name, with the era underneath when the round is an era mode. */
@Composable
fun GameBarTitle(title: String, era: QuizEra?) {
    if (era == null) {
        Text(title)
    } else {
        Column {
            Text(title)
            Text(StringsEras.label(era), style = MaterialTheme.typography.labelMedium)
        }
    }
}
