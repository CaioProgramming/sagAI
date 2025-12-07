package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.models.ChapterConclusionContext
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonFormatIncludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.data.model.ChapterGeneration
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findChapterAct
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.getDirective

object ChapterPrompts {
    val CHAPTER_EXCLUSIONS =
        listOf("id", "currentEventId", "coverImage", "createdAt", "actId", "featuredCharacters")

    fun chapterSummary(sagaContent: SagaContent) =
        buildString {
            sagaContent.currentActInfo
                ?.chapters
                ?.filter { it.isComplete() }
                ?.map { it.data }
                ?.let { chapters ->
                    if (chapters.isNotEmpty()) {
                        appendLine("**CURRENT ACT CHAPTERS Overview:**")
                        appendLine("This section provides the summaries of chapters already written in the current act")
                        appendLine("// Use this to understand the immediate narrative progression and context within the act.")
                        appendLine(
                            chapters.normalizetoAIItems(
                                listOf(
                                    "id",
                                    "actId",
                                    "currentEventId",
                                    "coverImage",
                                    "createdAt",
                                    "featuredCharacters",
                                ),
                            ),
                        )
                    }
                }
        }

    fun chapterIntroductionPrompt(
        sagaContent: SagaContent,
        currentChapter: Chapter,
        currentAct: ActContent,
    ): String =
        buildString {
            sagaContent.findChapterAct(currentChapter)
            val previousChaptersInAct =
                currentAct.chapters
                    .filter { it.isComplete() }
                    .map { it.data }
                    .filter { it.id != currentChapter.id }

            // Check if this is the very first chapter of the saga
            val isFirstChapter = sagaContent.flatChapters().first().data.id == currentChapter.id

            // Get previous act context if this is not the first act
            val previousAct =
                if (sagaContent.acts.size > 1 && currentAct == sagaContent.acts.firstOrNull()) {
                    null
                } else {
                    val currentActIndex =
                        sagaContent.acts.indexOfFirst { it.data.id == currentAct.data.id }
                    if (currentActIndex > 0) sagaContent.acts[currentActIndex - 1] else null
                }

            val chapterExclusions =
                listOf(
                    "id",
                    "emotionalReview",
                    "actId",
                    "currentEventId",
                    "coverImage",
                    "createdAt",
                    "featuredCharacters",
                )

            appendLine(
                "You are an AI storyteller writing an introduction for the next chapter of an ongoing saga.",
            )
            appendLine(
                "Your task is to create a natural transition based ONLY on established story context, not invented events.",
            )
            appendLine()

            appendLine(SagaPrompts.mainContext(sagaContent))

            // Provide context based on what's actually established
            when {
                isFirstChapter -> {
                    appendLine("### CONTEXT: This is the FIRST CHAPTER of the saga.")
                    appendLine("- Base your introduction on the saga's premise and the main character's starting situation.")
                    appendLine("- DO NOT reference events that haven't happened yet.")
                    appendLine("- Focus on setting the tone and establishing the beginning of the journey.")
                }

                previousChaptersInAct.isEmpty() && previousAct != null -> {
                    appendLine("### CONTEXT: This is the FIRST CHAPTER of a new act.")
                    appendLine("### Previous Act Context:")
                    appendLine("Title: ${previousAct.data.title}")
                    appendLine("Description: ${previousAct.data.content}")
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Create a bridge from the previous act to this new phase of the story.")
                }

                previousChaptersInAct.isNotEmpty() -> {
                    appendLine("### Previous Chapters in Current Act:")
                    appendLine(previousChaptersInAct.normalizetoAIItems(chapterExclusions))
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Continue naturally from where the previous chapters left off.")
                }

                else -> {
                    appendLine("### Current Act Theme:")
                    appendLine(currentAct.data.introduction)
                    appendLine("- Create an introduction that fits the current act's theme and progression.")
                }
            }

            appendLine("### Narrative Directive (Pacing and Style):")
            appendLine(sagaContent.getDirective())

            appendLine("## YOUR TASK")
            appendLine("Write a single paragraph introduction that:")
            appendLine(
                "1. **Reflects Current State:** Based ONLY on established context (previous chapters, act themes, saga premise).",
            )
            appendLine(
                "2. **Creates Natural Continuation:** Show where the story stands now without inventing new events.",
            )
            appendLine(
                "3. **Engages Without Fabrication:** Create anticipation for what's to come based on the natural story flow.",
            )
            appendLine(
                "4. **Maintains Consistency:** Never contradict or advance beyond what's actually been established.",
            )

            appendLine("## CRITICAL RULES")
            appendLine("- NEVER invent events that haven't been established in the provided context")
            appendLine("- For first chapters: Focus on the premise and starting situation, not imaginary prior events")
            appendLine("- For continuation chapters: Build only on what previous chapters actually established")
            appendLine("- Create hooks through atmosphere and anticipation, not fabricated plot points")

            appendLine("## OUTPUT REQUIREMENTS")
            appendLine("- **Length:** 1 concise paragraph (40-60 words).")
            appendLine("- **Content:** NO dialogue, character names, or specific plot details not in context.")
            appendLine("- **Format:** Output ONLY the introduction text itself. No quotes, no labels, no extra commentary.")
        }

    @Suppress("ktlint:standard:max-line-length")
    fun chapterGeneration(
        sagaContent: SagaContent,
        currentChapterContent: ChapterContent,
    ) = buildString {
        val chapterAct = sagaContent.findChapterAct(currentChapterContent.data)
        val isFirstAct =
            sagaContent.acts
                .first()
                .data.id == chapterAct?.data?.id
        val currentChapters = chapterAct?.chapters?.filter { it.data.id != currentChapterContent.data.id } ?: emptyList()

        val previousAct =
            if (isFirstAct) {
                null
            } else {
                val previousActIndex = sagaContent.acts.indexOfFirst { it.data.id == chapterAct?.data?.id } - 1
                sagaContent.acts[previousActIndex]
            }

        val promptDataContext =
            ChapterConclusionContext(
                sagaData = sagaContent.data,
                mainCharacter = sagaContent.mainCharacter?.data,
                eventsOfThisChapter =
                    currentChapterContent.events
                        .filter { it.isComplete() }
                        .map { it.data },
                previousChaptersInCurrentAct = currentChapters.map { it.data },
                previousActData = previousAct?.data,
            )

        val includedFields =
            listOf(
                "sagaData",
                "mainCharacter",
                "previousActData",
                "previousChaptersInCurrentAct",
                "eventsOfThisChapter",
                "title",
                "description",
                "content",
                "genre",
                "name",
                "backstory",
            )

        val combinedContextJson = promptDataContext.toJsonFormatIncludingFields(includedFields)

        val chapterOutput =
            toJsonMap(
                ChapterGeneration::class.java,
            )

        appendLine("Context:")
        appendLine(combinedContextJson)

        appendLine("TASK:")
        appendLine("You are an AI assistant tasked with concluding a chapter of a saga.")
        appendLine(
            "Based ENTIRELY on the `EVENTS_OF_THIS_CHAPTER` provided in the CONTEXT, you need to generate two pieces of information for the `CHAPTER_BEING_CONCLUDED`:",
        )
        appendLine(
            " - If `PREVIOUS_CHAPTERS_IN_CURRENT_ACT` is provided and not empty, use their `title`s and `overview`s to understand the immediate preceding narrative progression within this act. Your generated `overview` for the current chapter should flow naturally from these, and its hook should set the stage for what might come next, considering this continuity.",
        )
        appendLine(
            " - If `PREVIOUS_ACT_DATA` is provided, use its `title` and `description` (or `overview`) to understand the broader story arc of the saga leading up to the current act. Ensure the current chapter's conclusion aligns with this larger progression.",
        )

        appendLine(
            "1. A concise overview (around 100 words) that summarizes the key outcomes, significant developments, and the immediate aftermath of these events. This overview should also provide a natural hook or transition setting the stage for what might come next.",
        )
        appendLine(
            "2. Generate a fitting title for this chapter that accurately reflects its core content or theme as derived from the events. **The title should be short (ideally 2-5 words) and impactful, creating intrigue or summarizing the chapter's essence memorably.**",
        )
        appendLine(
            "3. Extract 1 - 3 most important characters to include in featuredCharacters array.",
        )

        appendLine("Consider the `SAGA_DATA` for overall tone and style, and the `MAIN_CHARACTER`'s perspective if relevant to the events.")
        appendLine("EXPECTED OUTPUT FORMAT:")
        appendLine(chapterOutput)
    }

    fun coverDescription(
        content: SagaContent,
        chapter: Chapter,
        characters: List<Character>,
        visualDirection: String?,
    ): String {
        val coverContext =
            mapOf(
                "sagaTitle" to content.data.title,
                "sagaGenre" to content.data.genre.name,
                "chapterTitle" to chapter.title,
                "chapterDescription" to chapter.overview,
                "charactersInvolved" to characters,
            )
        val fieldsToExcludeForCover =
            listOf(
                "joinedAt",
                "id",
                "image",
                "hexColor",
                "sagaId",
                "abilities",
                "emojified",
            )
        val coverContextJson = coverContext.toJsonFormatExcludingFields(fieldsToExcludeForCover)
        val genre = content.data.genre

        return buildString {
            appendLine(
                "Your task is to act as an AI Image Prompt Engineer specializing in generating concepts for **Minimalistic Chapter Covers**.",
            )
            appendLine("You will receive contextual information about the SAGA (title, genre) and the specific CHARACTERS to be featured.")
            appendLine(
                "You will also (outside this prompt) have access to Visual Reference Images for each character involved to inspire their appearance, and a general Visual Reference Image for overall composition and style.",
            )
            appendLine()
            appendLine("**CRITICAL CONTEXT FOR YOU (THE AI IMAGE PROMPT ENGINEER):**")
            appendLine("1.  **Saga & Character Information (JSON below):** Details about the saga's genre and the characters to feature.")
            appendLine("    $coverContextJson")
            appendLine()
            appendLine("Visual Direction:")
            visualDirection?.let {
                appendLine("This rules dictate how you should describe the icon composition")
                appendLine(it)
            } ?: run {
                appendLine("Ensure to render this art style description matching with the reference image")
                appendLine(GenrePrompts.artStyle(genre))
            }

            appendLine("This description must:")
            appendLine("*   Integrate the **Character Details**.")
            appendLine(
                "*Develop a **Dramatic and Expressive Pose** for the character. This pose should be dynamic and reflect the character's essence, drawing from their **Character Details** (e.g., occupation, personality traits, role, equipped items). The pose should be original and compelling for an icon, not a static or default stance.",
            )
            appendLine("**Character Focus and Framing (CRITICAL - INJECTION OF VISUAL DIRECTION):**")
            appendLine(ImagePrompts.descriptionRules(genre))
            appendLine("**Final Prompt Structure (Mandatory Order):**")
            appendLine("1.  **Technical Foundation (Composed of Injected Data):**")
            appendLine(
                "* Start the prompt with the **Framing, Zoom Level, and Cropping Intention** (e.g., \"ULTRA CLOSE-UP... Very tight shot, Subject fills entire frame...\").",
            )
            appendLine(
                "* Immediately follow with the **Foundational Art Style** (Grand Classical oil painting...) and the **Key Lighting Style** (Dramatic Rembrandt lighting...) and **Color Palette**.",
            )
            appendLine("2.  **Narrative & Character Core (Crucial for Vibe):**")
            appendLine(
                " * **CRITICAL POSE RULE (Anti-Static):** The Agent MUST ensure the character's pose, head angle, and gaze are **dynamic and non-symmetrical**.",
            )
            appendLine(
                "The character MUST NOT be looking directly and neutrally into the camera (Avoid \"Straight on gaze\", \"Neutral head angle\"). The head should be **tilted, turned, or angled to create tension**.",
            )
            appendLine(
                "* Integrate the Characters Details and a **high-value narrative opening** that defines the character's *VIBE* and *EMOTION*. This section must focus on **Expression, Pose, and Story Context**, adapting them to the Framing. (e.g., \"A hauntingly beautiful and fiercely determined Vanya, captured in a moment of restrained fury.\").",
            )
            appendLine(
                "* The Agent MUST use the 'Character Details' to create a **dynamic and expressive pose** that fits the close-up framing.",
            )
            appendLine(
                "* **Action Focus:** For ULTRA CLOSE-UP, the dynamism must be expressed through **head/neck movement, hair movement, expression intensity, or interaction with an object just outside the frame**.",
            )
            appendLine(ImagePrompts.imageHighlight(genre))

            appendLine(
                "* **CRITICAL - ACTION DYNAMISM MANDATE:** The Agent **MUST NOT** use the exact example phrase 'Vanya lunges forward from the darkness, her neck strained, the motion captured as strands of hair whip across her jawline.'",
            )
            appendLine(
                "* The Agent MUST replace passive descriptions with a **unique, active, and dynamic phrasing** that conveys energy and tension.",
            )
            appendLine(
                "* The generated action MUST be **original** for the current image and consistent with the pose/scene described in the 'NARRATIVE & COMPOSITION CORE' (e.g., if the mood is 'Contemplative', the action could be 'She braces her body against the biting wind, her posture radiating stoic defiance,' OR 'Her hand tightens around the dagger hilt in a reflexive gesture of protection').",
            )
            appendLine("* **Goal:** The description must convey **energy and tension** without repeating the boilerplate example.")

            appendLine(
                "**CRITICAL:** The Agent MUST rewrite the character description to be consistent with the first element of the prompt, ensuring the focus is unambiguous.",
            )
            appendLine(
                "*Incorporate the **Overall Compositional Framing** and compatible **Visual Details & Mood** inspired by the general Visual Reference Image, but ensure the **Character\'s Pose** itself is uniquely dramatic and primarily informed by their provided **Character Details**.",
            )
            appendLine(
                "***CRUCIAL: Your output text prompt MUST NOT mention the Visual Reference Image.** It must be a self-contained description.",
            )
            appendLine("* CRUCIAL: ENSURE THAT NO TEXT IS RENDERED AT ALL ONLY THE Image")
            appendLine(
                "- **Looks:** Describe the character's facial features and physical build (e.g., 'a rugged man with a lean physique', 'a Latina woman with a sophisticated haircut').",
            )
            appendLine(
                "- **Clothing:** Detail their attire, including style, color, and accessories (e.g., 'a vibrant Hawaiian-style shirt', 'a sleek two-piece swimsuit').",
            )
            appendLine(
                "- **Expression:** The face should not be neutral. It must convey a strong emotion or intention. Use terms like 'a hardened, protective gaze', 'a piercing, fatal stare', 'a sardonic smile'.",
            )
            appendLine(
                "- **Pose & Body Language:** Describe their posture and how they interact with the environment. Use dynamic phrases like 'relaxed yet alert posture', 'casually lounging on a car hood', 'body language exuding confidence'.",
            )
            appendLine(
                "Dramatic cover of [Character Name], a [Character's key trait/role]. Rendered in a distinct [e.g., 80s cel-shaded anime style with bold inked outlines].",
            )
            appendLine("The background is a vibrant [e.g., neon purple as per genre instructions].")
            appendLine(
                "Specific character accents include [e.g., luminous purple cybernetic eye details and thin circuit patterns on their blackpopover, as per genre instructions].",
            )
            appendLine(
                "The character's skin tone remains natural, and their primary hair color is [e.g., black], with lighting appropriate to the cel-shaded anime style and studio quality.",
            )
            appendLine(
                "The character should be the absolute focus of the image, filling most of the frame in a compelling, dynamic pose. No other characters or complex backgrounds should be present, ensuring the icon is clean and impactful.",
            )
            appendLine("Desired Output: A single, striking icon image. NO TEXT SHOULD BE GENERATED ON THE IMAGE ITSELF.")
        }.trimIndent()
    }
}
