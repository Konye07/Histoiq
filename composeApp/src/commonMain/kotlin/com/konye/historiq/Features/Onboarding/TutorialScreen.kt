package com.konye.historiq.Features.Onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsTutorial
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.tutorial_blind_map_battle
import com.konye.historiq.generated.resources.tutorial_casualties_multiplier
import com.konye.historiq.generated.resources.tutorial_flag_color_match
import com.konye.historiq.generated.resources.tutorial_territory_compare
import com.konye.historiq.generated.resources.tutorial_true_false
import com.konye.historiq.generated.resources.tutorial_year_guess
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class TutorialPage(
    val image: DrawableResource? = null,
    val accentColor: Color? = null,
    val title: String,
    val subtitle: String? = null,
    val body: String
)

/**
 * First-launch-only walkthrough (see RootApp: gated by SettingsStore.hasSeenTutorial). Each game
 * page uses a real screenshot of that game's first question, captured live on-device rather than
 * a mockup, so what's shown always matches the actual UI. Skippable at any point, not just at the
 * end — a new user who already gets the gist shouldn't be forced through all eight pages.
 */
@Composable
fun TutorialScreen(onFinished: () -> Unit) {
    val pages = listOf(
        TutorialPage(
            title = StringsTutorial.tutorial_welcome_title,
            body = StringsTutorial.tutorial_welcome_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_year_guess,
            accentColor = GameKeys.color(GameKeys.YEAR_GUESS),
            title = GameKeys.roleName(GameKeys.YEAR_GUESS),
            subtitle = GameKeys.title(GameKeys.YEAR_GUESS),
            body = StringsTutorial.tutorial_year_guess_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_casualties_multiplier,
            accentColor = GameKeys.color(GameKeys.CASUALTIES_MULTIPLIER),
            title = GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
            subtitle = GameKeys.title(GameKeys.CASUALTIES_MULTIPLIER),
            body = StringsTutorial.tutorial_casualties_multiplier_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_flag_color_match,
            accentColor = GameKeys.color(GameKeys.FLAG_COLOR_MATCH),
            title = GameKeys.roleName(GameKeys.FLAG_COLOR_MATCH),
            subtitle = GameKeys.title(GameKeys.FLAG_COLOR_MATCH),
            body = StringsTutorial.tutorial_flag_color_match_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_territory_compare,
            accentColor = GameKeys.color(GameKeys.TERRITORY_COMPARE),
            title = GameKeys.roleName(GameKeys.TERRITORY_COMPARE),
            subtitle = GameKeys.title(GameKeys.TERRITORY_COMPARE),
            body = StringsTutorial.tutorial_territory_compare_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_true_false,
            accentColor = GameKeys.color(GameKeys.TRUE_FALSE),
            title = GameKeys.roleName(GameKeys.TRUE_FALSE),
            subtitle = GameKeys.title(GameKeys.TRUE_FALSE),
            body = StringsTutorial.tutorial_true_false_body
        ),
        TutorialPage(
            image = Res.drawable.tutorial_blind_map_battle,
            accentColor = GameKeys.color(GameKeys.BLIND_MAP_BATTLE),
            title = GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE),
            subtitle = GameKeys.title(GameKeys.BLIND_MAP_BATTLE),
            body = StringsTutorial.tutorial_blind_map_battle_body
        ),
        TutorialPage(
            title = StringsTutorial.tutorial_closing_title,
            body = StringsTutorial.tutorial_closing_body
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
                if (!isLastPage) {
                    TextButton(onClick = onFinished) { Text(StringsTutorial.tutorial_skip) }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                TutorialPageContent(pages[index])
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) (pages[i].accentColor ?: MaterialTheme.colorScheme.primary)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                    )
                    if (i != pages.lastIndex) Spacer(Modifier.width(8.dp))
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) { Text(StringsTutorial.tutorial_back) }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Button(onClick = {
                    if (isLastPage) onFinished()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }) {
                    Text(if (isLastPage) StringsTutorial.tutorial_get_started else StringsTutorial.tutorial_next)
                }
            }
        }
    }
}

@Composable
private fun TutorialPageContent(page: TutorialPage) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (page.image != null) Arrangement.Top else Arrangement.Center
    ) {
        if (page.image != null) {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(page.image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter
            )
            Spacer(Modifier.height(20.dp))
        }
        Text(
            page.title,
            style = MaterialTheme.typography.headlineSmall,
            color = page.accentColor ?: MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (page.subtitle != null) {
            Text(
                page.subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (page.image == null) Spacer(Modifier.height(8.dp))
    }
}
