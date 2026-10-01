package com.ilustris.sagai.features.chapter.data.model

import com.google.gson.annotations.SerializedName
import com.ilustris.sagai.core.ai.model.GeneratedChapter
import com.ilustris.sagai.features.narrative.data.model.ContinuitySummary
import com.ilustris.sagai.features.narrative.data.model.GeneratedLocationCheckpoint
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.timeline.data.model.GeneratedWikiUpdate

data class UnifiedChapterUpdate(
    val chapter: GeneratedChapter,
    val characterArcs: List<GeneratedCharacterArc> = emptyList(),
    val landmarkWikis: List<GeneratedWikiUpdate> = emptyList(),
    val worldStateUpdate: String? = null,
    val continuitySummary: ContinuitySummary? = null,
    /** Where/when this chapter ends — becomes the next chapter's opening checkpoint. */
    val closingCheckpoint: GeneratedLocationCheckpoint? = null,
    /**
     * The player's read after answering this chapter's cards: the previous read, reassessed against
     * the picks. Only asked for (and only kept) when the chapter closed with answered cards.
     */
    val playerSpectrum: String? = null,
)

/**
 * One forced-choice dilemma: a short question and the options the player turns over. Options are
 * a list so a card can grow past two without a new schema; today every card is dealt exactly two
 * (see PLAYER_CHOICE_OPTION_COUNT).
 */
data class GeneratedChoiceCard(
    /** A short, direct question — not a rambling dilemma sentence. */
    val choiceTitle: String = "",
    val options: List<ChoiceOption> = emptyList(),
)

data class ChoiceOption(
    /** What the player reads on the card face. */
    val text: String = "",
    /**
     * A psychological read of what taking this option shows about the player — the motive and what
     * it protects them from. Never shown; it feeds [Chapter.playerSpectrum]. Hands stored before
     * this was a sentence called it `tag`, hence the alternate name.
     */
    @SerializedName(value = "insight", alternate = ["tag"])
    val insight: String = "",
    /** The emotional weight of taking this option, on the same scale the story's events use. */
    val emotionalTone: EmotionalTone? = null,
    /**
     * Title of the chapter event whose stakes this option carries. Picking it tells the synthesis
     * which events the player leaned into. Blank when the option isn't about one event, or when the
     * model's title didn't match a real one.
     */
    val eventTitle: String = "",
)

/**
 * The option [answer] refers to. New answers are the picked option's index; hands answered before
 * that stored the option's tag (now [ChoiceOption.insight]) as the answer, so those still resolve.
 */
fun GeneratedChoiceCard.pickedOption(answer: String): ChoiceOption? =
    answer.toIntOrNull()?.let { options.getOrNull(it) } ?: options.find { it.insight == answer }

/**
 * The whole hand dealt for a chapter's closure: the screen's own framing (shown once, above
 * whichever card is up) plus the three dilemmas. A wrapper of just `cards` collapsed under the
 * model on at least one run — [GemmaClient]'s response already nests once under `data`, and a
 * single-field passthrough object read as redundant and got flattened away. Three real fields
 * reads as a real object instead, the same shape [UnifiedChapterUpdate]/[UnifiedActUpdate] use
 * without that problem.
 */
data class GeneratedPlayerChoices(
    /** A short framing line in the story's own voice — never a UI label, never meta. */
    val screenTitle: String = "",
    val screenSubtitle: String = "",
    val cards: List<GeneratedChoiceCard> = emptyList(),
)

data class GeneratedCharacterArc(
    val characterName: String,
    val arcTitle: String,
    val arcContent: String,
)
