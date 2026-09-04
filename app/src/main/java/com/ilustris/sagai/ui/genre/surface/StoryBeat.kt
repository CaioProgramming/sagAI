package com.ilustris.sagai.ui.genre.surface

import androidx.compose.runtime.Immutable
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.wiki.data.model.Wiki

/**
 * One beat of story, described rather than laid out.
 *
 * This is deliberately data and not a slot API. The Milestone screen's previous design handed each
 * genre a already-composed `Column(icon, title, body, Row(cards))` and let it decorate *around* it,
 * which is exactly why every genre came out as the same screen with a different backdrop — a
 * decorator can change the wallpaper but it cannot turn that Column into a printed shell log or a
 * chat thread. Describing the beat instead hands each [GenreSurfaceStyle]
 * [com.ilustris.sagai.ui.genre.GenreSurfaceStyle] full authority over layout.
 *
 * Nothing here is a `SagaContent`. Only two of `SagaMilestone`'s six variants carry one, so a
 * surface that demanded it could not render an act closure or a chapter introduction at all.
 *
 * The neutral package resolves no string resources: [entriesLabel], [castLabel], [StoryAside.label]
 * and [StoryBeatAction.label] all arrive already localized by whoever built the beat. That is what
 * lets one package serve both the story review and the Milestone screen without them having to
 * share a string namespace.
 */
@Immutable
data class StoryBeat(
    /**
     * What this beat *is*, for the purpose of restarting animations.
     *
     * Surfaces key their typewriters and tear-reveals on this and never on the whole beat, because
     * a beat is rebuilt whenever any of its data changes — and some of it arrives late. A chapter's
     * cover image is generated fire-and-forget after the milestone is already on screen; keying on
     * the beat itself would make the body re-type from zero the moment that image landed.
     */
    val key: Any,
    val title: String? = null,
    val body: String? = null,
    /** A small label above the title — "novo capítulo", "Chapter - II". */
    val eyebrow: String? = null,
    /**
     * One word for what this beat *does*. Only surfaces that speak in imperatives use it: Terminal
     * renders it as the shell command being run. Others ignore it.
     */
    val verb: String? = null,
    /** Who or what is telling this. Terminal slugifies it into its `admin@host` prompt. */
    val source: String? = null,
    val tone: StoryBeatTone = StoryBeatTone.NARRATION,
    /**
     * An attributed speaker, where there is one. Crime puts their avatar on the bubble and moves it
     * to the correct side; a null speaker is "nobody in the conversation said this".
     */
    val speaker: Character? = null,
    /** Image urls — a chapter's cover, an act's run of them. */
    val figures: List<String> = emptyList(),
    val entries: List<Wiki> = emptyList(),
    val entriesLabel: String? = null,
    val cast: List<Character> = emptyList(),
    val castLabel: String? = null,
    /** A second voice commenting on the beat, distinct from the story telling it. */
    val aside: StoryAside? = null,
    val progress: StoryProgress? = null,
    val actions: List<StoryBeatAction> = emptyList(),
    /**
     * Hold [actions] back until the surface has finished revealing itself. Keeps a cold-open beat
     * from being skipped before it has said anything.
     */
    val gateActionsOnReveal: Boolean = false,
)

/**
 * How the beat is spoken, in terms every style can honour. Book renders [EPIGRAPH] as a centred
 * italic quote and [NARRATION] as a titled paragraph; Crime puts [PLAYER] on the right like your
 * own messages and everything else on the left; [SYSTEM] is nobody's voice — an error, a notice.
 */
enum class StoryBeatTone { NARRATION, EPIGRAPH, ANNOUNCEMENT, PLAYER, SYSTEM }

@Immutable
data class StoryAside(
    val label: String,
    val text: String,
    /**
     * The emotional read this aside is commenting on, where the beat knows it.
     *
     * Carried so a surface can draw the tone rather than only quote the write-up about it — Crime
     * shows the same vibe card its review does. Optional: styles that have nothing to draw a tone
     * with simply ignore it, and a beat with no tone still has its text.
     */
    val tone: EmotionalTone? = null,
)

@Immutable
data class StoryProgress(
    val index: Int,
    val total: Int,
)

/**
 * Something the reader can do at the end of the beat. Each action carries its own [onClick], so a
 * surface never has to know the vocabulary of actions a particular screen supports — which is
 * precisely what keeps `ReviewAction` and its navigation concerns out of this package.
 */
@Immutable
data class StoryBeatAction(
    val id: String,
    val label: String,
    val emphasis: StoryActionEmphasis = StoryActionEmphasis.SECONDARY,
    /** Renders the action mid-work — a spinner, a blinking caret — and ignores taps. */
    val busy: Boolean = false,
    val onClick: () -> Unit,
)

enum class StoryActionEmphasis { PRIMARY, SECONDARY }

/** Floor and ceiling for a typed reveal, shared so every style paces prose the same way. */
private const val MIN_REVEAL_MS = 500L
private const val MAX_REVEAL_MS = 3000L
private const val MS_PER_CHAR = 16L

/**
 * How long this beat's body takes to type in. A pure function of length, matching the formula the
 * review's Crime template has always used — its ChatScroll navigation paces the whole thread off
 * this number, so the arithmetic has to stay exactly where it was.
 */
fun StoryBeat.estimatedRevealDurationMs(): Long =
    (body.orEmpty().length * MS_PER_CHAR).coerceIn(MIN_REVEAL_MS, MAX_REVEAL_MS)
