package com.ilustris.sagai.features.saga.chat.data.voicing

import com.ilustris.sagai.ui.theme.RichTextParser
import com.ilustris.sagai.ui.theme.TextSegment

/**
 * A message is text to read; its audio is a *performance* of it. The script is what the TTS
 * actually says: dialogue by the character, narration by the narrator, actions turned into vocal
 * sounds / delivery cues or dropped, thoughts never spoken.
 *
 * @property style One direction for the whole clip (pace, mood), sent as the TTS instruction.
 */
data class PerformanceScript(
    val style: String = "",
    val lines: List<PerformanceLine> = emptyList(),
)

/**
 * @property speaker [NARRATOR_SPEAKER] or the character's name.
 * @property block Index of the source [MessageBlock] this line performs, so the live captions know
 * which part of the message is playing. -1 when the model didn't say.
 */
data class PerformanceLine(
    val speaker: String = "",
    val text: String = "",
    val block: Int = -1,
) {
    val isNarrator get() = speaker.equals(NARRATOR_SPEAKER, ignoreCase = true)
}

/**
 * Speaker id for narrator lines. Top-level on purpose: the output schema is built from the model
 * classes' declared fields, and a companion constant would show up there as a static field.
 */
const val NARRATOR_SPEAKER = "NARRATOR"

enum class BlockType {
    DIALOGUE,
    ACTION,
    THINK,
    NARRATOR,
}

data class MessageBlock(
    val index: Int,
    val type: BlockType,
    val text: String,
) {
    /** Whether a performance can make this block heard at all (thoughts never are). */
    val canBeSpoken get() = type == BlockType.DIALOGUE || type == BlockType.NARRATOR
}

object MessageBlocks {
    fun split(text: String): List<MessageBlock> =
        RichTextParser
            .parse(text)
            .segments
            .mapNotNull { segment ->
                when (segment) {
                    is TextSegment.Plain -> BlockType.DIALOGUE to segment.text
                    is TextSegment.Action -> BlockType.ACTION to segment.text
                    is TextSegment.Think -> BlockType.THINK to segment.text
                    is TextSegment.Narrator -> BlockType.NARRATOR to segment.text
                }.takeIf { it.second.isNotBlank() }
            }.mapIndexed { index, (type, content) -> MessageBlock(index, type, content.trim()) }

    /** Prompt rendering: `index | TYPE | text`, one block per line. */
    fun render(blocks: List<MessageBlock>): String = blocks.joinToString("\n") { "${it.index} | ${it.type.name} | ${it.text}" }
}

object PerformanceScripts {
    /**
     * The script without a model: dialogue by [speaker] (or the narrator when the message is
     * narration), narrator blocks by the narrator, thoughts dropped. Actions are dropped too, except
     * the ones that are plainly a sound the speaker's own voice makes — a sigh, a laugh, a cough —
     * which become a cue on the nearest line they say, so a timed-out script still breathes.
     */
    fun deterministic(
        blocks: List<MessageBlock>,
        speaker: String?,
    ): PerformanceScript {
        val lines =
            blocks
                .filter { it.canBeSpoken }
                .map { block ->
                    val who =
                        if (block.type == BlockType.NARRATOR || speaker.isNullOrBlank()) NARRATOR_SPEAKER else speaker
                    PerformanceLine(speaker = who, text = block.text, block = block.index)
                }.toMutableList()

        blocks.filter { it.type == BlockType.ACTION }.forEach { action ->
            val cue = vocalCue(action.text) ?: return@forEach
            val speakerLines = lines.withIndex().filter { !it.value.isNarrator }
            // The line right after the action takes it as a lead-in; an action at the very end
            // trails the last thing said instead.
            val next = speakerLines.firstOrNull { it.value.block > action.index }
            val previous = speakerLines.lastOrNull { it.value.block < action.index }
            when {
                next != null -> lines[next.index] = next.value.copy(text = "$cue ${next.value.text}")
                previous != null -> lines[previous.index] = previous.value.copy(text = "${previous.value.text} $cue")
            }
        }
        return PerformanceScript(lines = lines)
    }

    /**
     * Common vocal sounds hidden in an action, by stem (Portuguese and English). Only sounds a
     * voice actually makes: "draws a sword" has none and stays silent.
     */
    private val VOCAL_CUES =
        listOf(
            Regex("suspir|\\bsigh") to "[sighs]",
            Regex("gargalh|risad|\\bri(u|o|ndo|r)?\\b|\\briso\\b|laugh|chuckl") to "[laughs]",
            Regex("toss(e|i|indo|iu)|cough") to "[coughs]",
            Regex("solu[cç]|chor(a|o|ando|ou)|\\bsob(s|bing)\\b|\\bcr(y|ies|ying)\\b") to "[crying]",
            Regex("engol(e|iu|indo) em seco|gulp") to "[gulps]",
            Regex("ofeg|arf(a|ando|ou)|pant") to "[panting]",
            Regex("sussurr|cochich|whisper") to "[whispering]",
            Regex("grit(a|o|ando|ou)|berr|shout|yell") to "[shouting]",
            Regex("geme|gemid|groan") to "[groans]",
            Regex("bufa|bufou|bufando|scoff") to "[scoffs]",
            Regex("respira fundo|inspira fundo|deep breath") to "[deep breath]",
            Regex("pigarre|clears? (his|her|their) throat") to "[clears throat]",
        )

    fun vocalCue(action: String): String? {
        val text = action.lowercase()
        return VOCAL_CUES.firstOrNull { (regex, _) -> regex.containsMatchIn(text) }?.second
    }

    /**
     * Keeps only lines the TTS can take: non-blank, spoken by [speaker] or the narrator (the
     * request allows two speakers at most), without our expressive tags leaking in.
     */
    fun sanitize(
        script: PerformanceScript,
        speaker: String?,
        blockCount: Int,
    ): PerformanceScript {
        val tagRegex = Regex("</?(action|think|narrator)>", RegexOption.IGNORE_CASE)
        val lines =
            script.lines.mapNotNull { line ->
                val text = line.text.replace(tagRegex, "").trim()
                if (text.isBlank()) return@mapNotNull null
                val who =
                    when {
                        line.isNarrator -> NARRATOR_SPEAKER
                        speaker != null && line.speaker.equals(speaker, ignoreCase = true) -> speaker
                        speaker == null -> NARRATOR_SPEAKER
                        else -> return@mapNotNull null
                    }
                line.copy(speaker = who, text = text, block = line.block.takeIf { it in 0 until blockCount } ?: -1)
            }
        return script.copy(style = script.style.trim(), lines = lines)
    }
}
