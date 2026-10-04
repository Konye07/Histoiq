package com.konye.historiq.Features.Play.Shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Question scrolls above; answer controls dock at the bottom, lifted above the keyboard by [imePadding] (edge-to-edge windows don't resize for it). */
@Composable
fun GuessQuestionLayout(
    scrollingContent: @Composable ColumnScope.() -> Unit,
    dock: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = scrollingContent
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = dock
        )
    }
}
