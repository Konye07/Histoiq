package com.konye.historiq.Features.Onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.konye.historiq.Core.Localization.StringsEras
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Localization.StringsTutorial
import com.konye.historiq.Core.Theme.legibleOn
import com.konye.historiq.Data.Quiz.BATTLE_MAX_SCORE
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.formatThousands
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

/**
 * One page: a [image] (a screenshot) or an [illustration] drawn live (told whether its page is showing),
 * or neither for the welcome and closing pages. [hasEras] adds the "Also in 5 historical eras" badge.
 */
private data class TutorialPage(
    val image: DrawableResource? = null,
    val illustration: (@Composable (active: Boolean) -> Unit)? = null,
    val accentColor: Color? = null,
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val hasEras: Boolean = false
)

@Composable
private fun gamePage(gameKey: String, image: DrawableResource, body: String, hasEras: Boolean = false) = TutorialPage(
    image = image,
    accentColor = GameKeys.color(gameKey),
    title = GameKeys.roleName(gameKey),
    subtitle = GameKeys.title(gameKey),
    body = body,
    hasEras = hasEras
)

/**
 * First-launch-only walkthrough (see RootApp: gated by SettingsStore.hasSeenTutorial, after the language
 * screen): the welcome, each of the six games, the Daily Battle, the era game modes, and a closing page. Each
 * game page uses a real screenshot of that game's first question, captured live on-device rather than a
 * mockup — screenshots, so the text inside them stays English in every language. The Daily Battle and era
 * pages are drawn live instead, from the same palettes, pictures and strings the app itself uses, so they
 * follow the language, dark mode and text size. Skippable
 * at any point, not just at the end — a new user who already gets the gist shouldn't be forced through
 * every page.
 */
@Composable
fun TutorialScreen(onFinished: () -> Unit) {
    val eraLogos = rememberEraLogos()
    val pages = listOf(
        TutorialPage(
            title = StringsTutorial.tutorial_welcome_title,
            body = StringsTutorial.tutorial_welcome_body
        ),
        gamePage(GameKeys.YEAR_GUESS, Res.drawable.tutorial_year_guess, StringsTutorial.tutorial_year_guess_body, hasEras = true),
        gamePage(GameKeys.CASUALTIES_MULTIPLIER, Res.drawable.tutorial_casualties_multiplier, StringsTutorial.tutorial_casualties_multiplier_body, hasEras = true),
        gamePage(GameKeys.FLAG_COLOR_MATCH, Res.drawable.tutorial_flag_color_match, StringsTutorial.tutorial_flag_color_match_body),
        gamePage(GameKeys.TERRITORY_COMPARE, Res.drawable.tutorial_territory_compare, StringsTutorial.tutorial_territory_compare_body),
        gamePage(GameKeys.TRUE_FALSE, Res.drawable.tutorial_true_false, StringsTutorial.tutorial_true_false_body),
        gamePage(GameKeys.BLIND_MAP_BATTLE, Res.drawable.tutorial_blind_map_battle, StringsTutorial.tutorial_blind_map_battle_body, hasEras = true),
        TutorialPage(
            illustration = { BattleOverviewIllustration() },
            accentColor = GameKeys.color(GameKeys.DAILY_BATTLE),
            title = GameKeys.roleName(GameKeys.DAILY_BATTLE),
            subtitle = GameKeys.title(GameKeys.DAILY_BATTLE),
            body = StringsTutorial.tutorial_battle_body(formatThousands(BATTLE_MAX_SCORE.toLong()), StringsHomeShared.nav_play)
        ),
        TutorialPage(
            illustration = { EraListIllustration(eraLogos) },
            title = StringsTutorial.tutorial_eras_title,
            body = StringsTutorial.tutorial_eras_body(StringsHomeShared.nav_play, StringsEras.era_all_time)
        ),
        TutorialPage(
            illustration = { active -> EraShowcaseIllustration(active, eraLogos) },
            title = StringsTutorial.tutorial_era_games_title,
            body = StringsTutorial.tutorial_era_games_body(
                GameKeys.roleName(GameKeys.YEAR_GUESS),
                GameKeys.roleName(GameKeys.CASUALTIES_MULTIPLIER),
                GameKeys.roleName(GameKeys.BLIND_MAP_BATTLE)
            )
        ),
        TutorialPage(
            title = StringsTutorial.tutorial_closing_title,
            body = StringsTutorial.tutorial_closing_body
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex
    val background = MaterialTheme.colorScheme.background

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
                TutorialPageContent(pages[index], active = pagerState.currentPage == index)
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
                                if (active) (pages[i].accentColor?.legibleOn(background) ?: MaterialTheme.colorScheme.primary)
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
private fun TutorialPageContent(page: TutorialPage, active: Boolean) {
    when {
        page.image != null -> Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            PageTexts(page)
        }
        // Scrolls, so a long text at a large text size (or a short screen) never pushes the picture out of reach.
        page.illustration != null -> BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                page.illustration.invoke(active)
                Spacer(Modifier.height(20.dp))
                PageTexts(page)
            }
        }
        else -> Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PageTexts(page)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PageTexts(page: TutorialPage) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            page.title,
            style = MaterialTheme.typography.headlineSmall,
            color = page.accentColor?.legibleOn(MaterialTheme.colorScheme.background) ?: MaterialTheme.colorScheme.onBackground,
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
        if (page.hasEras) {
            Spacer(Modifier.height(10.dp))
            EraBadge()
        }
        Spacer(Modifier.height(12.dp))
        Text(
            page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
