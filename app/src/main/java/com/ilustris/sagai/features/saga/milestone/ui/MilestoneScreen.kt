package com.ilustris.sagai.features.saga.milestone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneViewModel
import com.ilustris.sagai.features.saga.milestone.ui.skin.MilestoneSkinChrome
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.navigation.SagaActsKey
import com.ilustris.sagai.ui.navigation.SagaChaptersKey
import com.ilustris.sagai.ui.navigation.SagaEventsKey
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Deliberately simple for now (Duolingo-lesson-complete simple: icon, headline, one CTA) — no
 * mascot/character art yet, [R.drawable.ic_spark] is a placeholder until that's designed. The
 * structural piece that matters here is that this screen fully owns the narrative chain: it
 * can't be backed out of, and it drives itself step by step via [MilestoneViewModel].
 */
@Composable
fun MilestoneScreen(
    sagaId: Int,
    onFinished: () -> Unit,
    onNavigate: (NavKey) -> Unit = {},
    viewModel: MilestoneViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId) { viewModel.start(sagaId) }
    LaunchedEffect(Unit) { viewModel.finished.collect { onFinished() } }

    // A narrative chain is mandatory once started — no escaping back into chat mid-generation.
    BackHandler(enabled = true) { }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sagaData by viewModel.sagaData.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val chapterCoverImage by viewModel.chapterCoverImage.collectAsStateWithLifecycle()
    val actChapterCovers by viewModel.actChapterCovers.collectAsStateWithLifecycle()
    val bookGenerationState by viewModel.bookGenerationState.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "milestone_step",
        ) { state ->
            when (state) {
                is MilestoneUiState.Loading -> {
                    MilestoneSkinChrome(genre = genre, stepIndex = null, stepTotal = null) {
                        MilestoneLoadingContent(reasoning = state.reasoning, isAutomaticStep = state.isAutomaticStep)
                    }
                }

                is MilestoneUiState.Error -> {
                    MilestoneErrorContent(
                        message = state.message,
                        canRetry = state.canRetry,
                        onRetry = viewModel::retryFailedStep,
                    )
                }

                is MilestoneUiState.ClosureStep -> {
                    // The terminal skin draws its own char-cell progress bar as chrome, pinned
                    // over the same spot this Column would otherwise reserve for the plain dot
                    // one — so that dot indicator has to stay out of the way here, not render
                    // underneath it. Every other genre keeps today's inline StepIndicator.
                    val isTerminalSkin = genre?.surfaceStyle() == GenreSurfaceStyle.TERMINAL
                    val stepIndicator: @Composable () -> Unit =
                        if (isTerminalSkin) {
                            {}
                        } else {
                            { StepIndicator(stepIndex = state.stepIndex, stepTotal = state.stepTotal) }
                        }
                    MilestoneSkinChrome(
                        genre = genre,
                        stepIndex = state.stepIndex,
                        stepTotal = state.stepTotal,
                    ) {
                        MilestoneClosureContent(
                            state = state,
                            sagaId = sagaId,
                            genre = genre,
                            coverImage = chapterCoverImage,
                            actCoverImages = actChapterCovers,
                            bookGenerationState = bookGenerationState,
                            onContinue = viewModel::onContinue,
                            onNavigate = onNavigate,
                            onGenerateBook = viewModel::generateBook,
                            stepIndicator = stepIndicator,
                        )
                    }
                }

                is MilestoneUiState.IntroductionStep -> {
                    MilestoneSkinChrome(genre = genre, stepIndex = null, stepTotal = null) {
                        MilestoneIntroductionContent(milestone = state.milestone, onContinue = viewModel::onContinue)
                    }
                }
            }
        }

        // Only ever true for a brand-new saga's first act, tutorials on — overlaps with that
        // act's introduction generating underneath instead of gating it (see
        // MilestoneViewModel.showOnboarding / ChatViewModel's progression check).
        if (showOnboarding) {
            OnboardingDialog(
                type = OnboardingType.GAMEPLAY_GUIDE,
                genre = sagaData?.genre,
                saga = sagaData,
                onDismiss = viewModel::dismissOnboarding,
            )
        }
    }
}

/** Genre icon + whatever [ReasoningSynthesizerService][com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService]
 * is currently streaming for this step (falls back to a generic line while it warms up, or to a
 * dedicated "adjusting a few things" line for [isAutomaticStep] — CreateTimeline has no AI call
 * and thus no reasoning to stream, but still deserves its own on-brand beat instead of reusing
 * the generic copy). */
@Composable
internal fun MilestoneLoadingContent(
    reasoning: String?,
    isAutomaticStep: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = themePainter(),
                contentDescription = null,
                modifier =
                    Modifier.size(50.dp).gradientFill(Brush.verticalGradient(morphingGradient())).themeVfx(true).reactiveShimmer(
                        true,
                        shimmerColors = Color.White.shimmerize(),
                        repeatMode = RepeatMode.Restart,
                        duration = 10.seconds,
                    ),
            )

            Text(
                text =
                    reasoning?.takeIf { it.isNotBlank() }
                        ?: stringResource(
                            if (isAutomaticStep) R.string.milestone_adjusting_lore else R.string.milestone_loading_default,
                        ),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        brush = Brush.horizontalGradient(themeBrushColors()),
                        shadow =
                            Shadow(
                                Color.White,
                                blurRadius = 15f,
                            ),
                    ),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .reactiveShimmer(
                            true,
                            shimmerColors = Color.White.shimmerize(),
                            repeatMode = RepeatMode.Restart,
                            duration = 10.seconds,
                        ),
            )
        }
    }
}

/** Surfaced instead of auto-retrying a step that already failed once (see
 * [com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState.Error] doc) — an
 * explicit tap, not a silent background loop that looks identical to a real loading state from
 * the outside. */
@Composable
internal fun MilestoneErrorContent(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(50.dp),
            )
            Text(
                text = stringResource(R.string.milestone_error_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (canRetry) {
                Button(
                    onClick = onRetry,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    Text(stringResource(R.string.try_again))
                }
            }
        }
    }
}

@Composable
internal fun MilestoneClosureContent(
    state: MilestoneUiState.ClosureStep,
    sagaId: Int,
    onContinue: () -> Unit,
    genre: Genre? = null,
    coverImage: String? = null,
    actCoverImages: List<String> = emptyList(),
    bookGenerationState: BookGenerationUiState = BookGenerationUiState.Idle,
    onNavigate: (NavKey) -> Unit = {},
    onGenerateBook: (Act) -> Unit = {},
    // A slot rather than a hardcoded call so a genre skin (see MilestoneSkinChrome) can swap in
    // its own step indicator — e.g. Terminal's char-cell TerminalProgress, rendered as chrome
    // above this whole composable — without this layout needing to know that skin exists. Left at
    // today's plain dot indicator by default so every caller that doesn't opt into a skin (design
    // preview included) renders exactly as before.
    stepIndicator: @Composable () -> Unit = {
        StepIndicator(stepIndex = state.stepIndex, stepTotal = state.stepTotal)
    },
) {
    val milestone = state.milestone
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        if (state.stepTotal > 1) {
            stepIndicator()
        }

        // Reset (and re-fire) per milestone instance — event -> chapter -> act each get their
        // own fade+pop entrance instead of just materializing flat, without stepping on the
        // outer AnimatedContent's own cross-fade between whole ui-state types.
        var contentVisible by remember(milestone) { mutableStateOf(false) }
        LaunchedEffect(milestone) { contentVisible = true }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = themePainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.size(50.dp).gradientFill(Brush.verticalGradient(morphingGradient())).themeVfx(true).reactiveShimmer(
                                true,
                                shimmerColors = Color.White.shimmerize(),
                                repeatMode = RepeatMode.Restart,
                                duration = 10.seconds,
                            ),
                    )
                    Text(
                        text = stringResource(milestone.title).lowercase(),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp).alpha(.5f),
                    )
                    Text(
                        text = milestone.subtitle,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                letterSpacing = 0.5.sp,
                                shadow = Shadow(MaterialTheme.colorScheme.primary, blurRadius = 15f),
                                fontWeight = FontWeight.SemiBold,
                            ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    milestone.message?.takeIf { it.isNotBlank() }?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }

                    if (milestone is SagaMilestone.ChapterFinished) {
                        ChapterCoverCard(coverImage = coverImage, modifier = Modifier.padding(top = 16.dp))
                    }

                    if (milestone is SagaMilestone.ActFinished && actCoverImages.isNotEmpty()) {
                        ActCoverRow(coverImages = actCoverImages, modifier = Modifier.padding(top = 16.dp))
                    }

                    if (milestone.wikis.isNotEmpty() && genre != null) {
                        MilestoneWikisRow(
                            wikis = milestone.wikis,
                            genre = genre,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }

                    if (milestone.characters.isNotEmpty() && genre != null) {
                        MilestoneCharactersRow(
                            characters = milestone.characters,
                            genre = genre,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }

                    milestone.emotionalReviewText?.takeIf { it.isNotBlank() }?.let { review ->
                        EmotionalReviewNote(text = review, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }

        milestone.detailDestination(sagaId)?.let { destination ->
            TextButton(onClick = { onNavigate(destination) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.milestone_view_details))
            }
        }

        if (milestone is SagaMilestone.ActFinished) {
            GenerateBookAction(
                act = milestone.act,
                bookGenerationState = bookGenerationState,
                onGenerateBook = onGenerateBook,
            )
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text(stringResource(R.string.continue_button))
        }
    }
}

/** Not ready the instant ChapterFinished shows — cover generation runs fire-and-forget in the
 * background right after the chapter closes (see ChapterUseCaseImpl.generateChapterCover). Fades
 * and pops in on its own once [MilestoneViewModel.chapterCoverImage] catches up. */
@Composable
private fun ChapterCoverCard(
    coverImage: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = coverImage != null,
        enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.94f, animationSpec = tween(500)),
        modifier = modifier,
    ) {
        coverImage?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.large),
            )
        }
    }
}

/** Styled like a community note — a quiet, neutrally-bordered aside, deliberately not competing
 * with the narrative title/message above it. This is the AI's own reflection on the emotional
 * arc, a distinct voice from the story itself. */
@Composable
private fun EmotionalReviewNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.milestone_emotional_note_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun GenerateBookAction(
    act: Act,
    bookGenerationState: BookGenerationUiState,
    onGenerateBook: (Act) -> Unit,
) {
    val isGeneratingThisAct = (bookGenerationState as? BookGenerationUiState.Generating)?.actId == act.id
    TextButton(
        onClick = { if (!isGeneratingThisAct) onGenerateBook(act) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isGeneratingThisAct) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.milestone_generating_book))
        } else {
            Text(stringResource(R.string.milestone_generate_book))
        }
    }
}

private val SagaMilestone.emotionalReviewText: String?
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> timeline.emotionalReview
            is SagaMilestone.ChapterFinished -> chapter.emotionalReview
            is SagaMilestone.ActFinished -> act.emotionalReview
            else -> null
        }

/** AI-generated wikis attached to this closure step, if any — [NewEvent][SagaMilestone.NewEvent],
 * [ChapterFinished][SagaMilestone.ChapterFinished] and [ActFinished][SagaMilestone.ActFinished]
 * are the only variants that carry them. */
private val SagaMilestone.wikis: List<Wiki>
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> wikis
            is SagaMilestone.ChapterFinished -> wikis
            is SagaMilestone.ActFinished -> wikis
            else -> emptyList()
        }

/** AI-generated/updated characters attached to this closure step, if any — same trio of
 * variants as [wikis]. */
private val SagaMilestone.characters: List<Character>
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> characters
            is SagaMilestone.ChapterFinished -> characters
            is SagaMilestone.ActFinished -> characters
            else -> emptyList()
        }

/** Plain, un-themed row of wiki entries generated alongside this milestone — genre-specific
 * skinning is later work, this phase just proves the generated content reaches the screen. Same
 * "created" label [MilestoneIslandBody][com.ilustris.sagai.ui.components.island.MilestoneIslandBody]
 * already uses for its own wikis row. */
@Composable
private fun MilestoneWikisRow(
    wikis: List<Wiki>,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.milestone_wikis_created),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            wikis.forEach { wiki ->
                WikiCard(
                    wiki = wiki,
                    genre = genre,
                    expanded = false,
                    modifier = Modifier.width(220.dp),
                )
            }
        }
    }
}

/** Plain, un-themed row of character updates for this milestone — see [MilestoneWikisRow]. */
@Composable
private fun MilestoneCharactersRow(
    characters: List<Character>,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.milestone_characters_created),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            characters.forEach { character ->
                MilestoneCharacterUpdateCard(character = character, genre = genre)
            }
        }
    }
}

/** Bare avatar + name — no invented "what changed" summary, [SagaMilestone] only carries the
 * [Character] itself, not a per-character diff. */
@Composable
private fun MilestoneCharacterUpdateCard(
    character: Character,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(72.dp),
    ) {
        CharacterAvatar(
            character = character,
            genre = genre,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = "${character.name} ${character.lastName.orEmpty()}".trim(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Act closure carries one cover per finished chapter (see
 * [MilestoneViewModel.actChapterCovers]) — reuses [ChapterCoverCard] unchanged in a horizontally
 * scrollable row instead of growing it a list-aware variant, so the single-cover
 * [SagaMilestone.ChapterFinished] call site stays untouched. */
@Composable
private fun ActCoverRow(
    coverImages: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        coverImages.forEach { cover ->
            ChapterCoverCard(coverImage = cover, modifier = Modifier.width(200.dp))
        }
    }
}

/** Pushed on top of the Milestone screen, not replacing it — the chain keeps waiting on its own
 * continueMilestone()/advanceNarrative() calls regardless of what's on screen, so "peeking" at
 * the list and coming back leaves the reveal exactly where the user left it. */
private fun SagaMilestone.detailDestination(sagaId: Int): NavKey? =
    when (this) {
        is SagaMilestone.NewEvent -> SagaEventsKey(sagaId.toString())
        is SagaMilestone.ChapterFinished -> SagaChaptersKey(sagaId.toString())
        is SagaMilestone.ActFinished -> SagaActsKey(sagaId.toString())
        else -> null
    }

@Composable
internal fun MilestoneIntroductionContent(
    milestone: SagaMilestone.Introduction,
    onContinue: () -> Unit,
) {
    // Cinematic, unstepped on purpose — this is the "cold open" beat, not a checklist item.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        // Defense in depth: SagaContentManagerImpl already skips revealing this milestone at all
        // when it has nothing to show, but if a blank introduction ever reaches this composable
        // anyway, starting both flags true (instead of waiting on SimpleTypewriterText's
        // onAnimationFinished, which never fires for blank text) means the title/Continue button
        // still show up immediately rather than trapping the player on an empty, un-backable
        // screen.
        val hasIntroduction = milestone.introduction.isNotBlank()
        var textComplete by remember(milestone) {
            mutableStateOf(!hasIntroduction)
        }
        var shownChapter by remember(milestone) {
            mutableStateOf(!hasIntroduction)
        }

        LaunchedEffect(textComplete) {
            if (textComplete) {
                shownChapter = true
                delay(1.seconds)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp).themeVfx(true),
            )
            AnimatedVisibility(shownChapter) {
                Text(
                    text = milestone.number,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp).alpha(.6f),
                )
            }

            AnimatedVisibility(shownChapter) {
                Text(
                    text = milestone.titleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            milestone.introduction.takeIf { it.isNotBlank() }?.let { introduction ->
                SimpleTypewriterText(
                    text = introduction,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(top = 16.dp),
                    onAnimationFinished = {
                        textComplete = true
                    },
                )
            }
        }

        AnimatedVisibility(shownChapter) {
            Button(
                onClick = onContinue,
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
internal fun StepIndicator(
    stepIndex: Int,
    stepTotal: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(stepTotal) { i ->
            val filled = i < stepIndex
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
            )
        }
    }
}
