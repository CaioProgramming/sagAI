package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.GenreConfig.CompanionConfig
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

object MilestonePrompts {
    fun generateCongratsMessage(
        milestone: SagaMilestone,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String? {
        if (milestone is SagaMilestone.Introduction) {
            return rewriteIntroduction(milestone, saga, companion)
        }

        if (milestone is SagaMilestone.Loading) {
            return generateLoadingMessage(saga, companion)
        }

        if (milestone is SagaMilestone.NewCharacter) {
            return generateNewCharacterMessage(milestone, saga, companion)
        }

        if (milestone is SagaMilestone.CurrentObjective) {
            return null
        }

        val genre = saga.data.genre
        val genreTone = companion?.tone ?: ""

        return buildString {
            appendLine("You are a witty, clever storytelling companion. The user just achieved a milestone in their saga.")
            appendLine("Generate ONE SHORT, MEMORABLE congratulatory message that REACTS DIRECTLY to their achievement.")
            appendLine()
            appendLine("# STORY IDENTITY")
            appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine()
            appendLine("# MILESTONE ACHIEVED")
            appendLine(
                milestone.toAINormalize(
                    fieldsToExclude =
                        ChatPrompts.characterExclusions
                            .plus(ChapterPrompts.CHAPTER_EXCLUSIONS)
                            .plus(TimelinePrompts.timelineExclusions),
                ),
            )
            appendLine()
            appendLine("YOUR PERSONA:")
            appendLine("You speak like a ${genre.name.lowercase()} aficionado who:")
            appendLine(
                companion?.persona ?: "Enjoys commenting playfully on story twists and turns.",
            )
            appendLine()
            appendLine("CREATIVE GUIDELINES:")
            appendLine("- Be ORIGINAL: Don't use generic congratulations")
            appendLine("- React to SPECIFIC details from the milestone above (character names, plot points, achievements)")
            if (genreTone.isNotEmpty()) {
                appendLine("- Use tone that matches $genreTone but with personality and sass")
            }
            appendLine("- Maximum 15 words (be punchy and impactful)")
            appendLine("- Include an emotional twist: sarcasm, dark humor, irony, or unexpected warmth")
            appendLine("- Make them laugh or think 'wow, that's perfect'")
            appendLine("- NO emojis, NO generic phrases, NO 'congratulations' or 'well done'")
            appendLine()
            appendLine("WHAT TO REFERENCE:")
            appendLine(getReferencePoints(milestone))
            appendLine()
            appendLine("TONE EXAMPLES (be creative, not copy these):")
            appendLine(companion?.conversationalStyle ?: "Be creatively conversational.")
            appendLine()
            appendLine("Output ONLY the single-line provocative message, nothing else:")
        }
    }

    fun generateLoadingMessage(
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String =
        buildString {
            val genre = saga.data.genre
            appendLine("You are a witty, slightly sarcastic narrator creating a brief, funny interlude while the story loads.")
            appendLine("Generate a SHORT, IRONIC, and UNEXPECTED phrase that sounds like an inside joke for the ${genre.name} genre.")
            appendLine()
            appendLine("# STORY IDENTITY")
            appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine()
            appendLine("# GENRE HUMOR STYLE (INSPIRATION ONLY)")
            appendLine(companion?.interludeStyle ?: "A funny short loading text string")
            appendLine()
            appendLine("CREATIVITY RULES:")
            appendLine("- BE FUNNY & IRONIC: Avoid being over-dramatic or too serious.")
            appendLine("- USE LOCAL JOKES: Think about what a fan of this genre would find funny.")
            appendLine("- BE UNEXPECTED: Invent something fresh and witty every time.")
            appendLine("- NO CLICHÉS: Avoid generic 'loading...' or typical dramatic tropes unless used ironically.")
            appendLine()
            appendLine("REQUIREMENTS:")
            appendLine("- Maximum 8 words (be punchy).")
            appendLine("- Sound like a friendly joke or a funny observation, NOT technical loading text.")
            appendLine("- NO emojis, NO ellipsis abuse, NO meta-commentary about 'AI' or 'system'.")
            appendLine()
            appendLine("Output ONLY the ironic interlude phrase:")
        }

    private fun getGenreInterludeStyle(genre: Genre): String =
        when (genre) {
            Genre.FANTASY -> {
                """
                Think about magical mishaps or mundane hero problems:
                - "Convincing the dragon I'm not delicious..."
                - "Accidentally turning the bard into a frog..."
                - "Searching for where I left my magic map..."
                - "Brewing a potion that's probably just soup..."
                - "Arguing with a sentient sword about its ego..."
                """.trimIndent()
            }

            Genre.CYBERPUNK -> {
                """
                Think about tech glitches and dystopian ironies:
                - "Turning off the firewall to see if it catches fire..."
                - "Downloading a 'legal' neural upgrade..."
                - "Telling the vending machine I'm not a bot..."
                - "Rebooting my artificial sense of humor..."
                - "Patching the holes in my virtual dignity..."
                """.trimIndent()
            }

            Genre.SPACE_OPERA -> {
                """
                Think about cosmic scale vs. tiny problems:
                - "Asking the computer why the moon is following us..."
                - "Trying to find 'Up' in a zero-G bathroom..."
                - "Negotiating with a space-whale for directions..."
                - "Recalibrating the coffee machine for warp speed..."
                - "Checking if the aliens accepted my friend request..."
                """.trimIndent()
            }

            Genre.HORROR -> {
                """
                Think about meta-horror tropes and bad decisions:
                - "Entering the basement despite the creepy music..."
                - "Asking 'Who's there?' like it ever works..."
                - "Trying to run in slow motion away from danger..."
                - "Checking if the monster is just lonely..."
                - "Counting the shadows to see which one moved..."
                """.trimIndent()
            }

            Genre.COWBOY -> {
                """
                Think about frontier grittiness and old-west irony:
                - "Asking the horse for its political opinion..."
                - "Polishing the spurs to look faster than I am..."
                - "Convincing the tumbleweed to pick a side..."
                - "Checking if this 'gold' is actually just shiny rocks..."
                - "Wondering if beans count as a balanced diet..."
                """.trimIndent()
            }

            Genre.SHINOBI -> {
                """
                Think about ninja training vs. everyday life:
                - "Sharpening the katana to cut through social awkwardness..."
                - "Blending into shadows at a loud party..."
                - "Meditating on why I always wear black in summer..."
                - "Reading the secret scroll that's just a grocery list..."
                - "Practicing the 'I wasn't here' smoke bomb exit..."
                """.trimIndent()
            }

            Genre.HEROES -> {
                """
                Think about hero egos and super-power logistics:
                - "Checking if my super soldier serum stock is full..."
                - "Washing the cape because justice smells like laundry..."
                - "Practicing my hero landing without breaking my knees..."
                - "Explaining to the police why I have a sidekick..."
                - "Reviewing the case files (mostly fan mail)..."
                """.trimIndent()
            }

            Genre.CRIME -> {
                """
                Think about noir tropes and detective clichés:
                - "Staring at the rain like it owes me money..."
                - "Finding the 'smoking gun' but it's just a toaster..."
                - "Lighting a cigarette in a non-smoking interrogation room..."
                - "Tailing a suspect who's just going to the gym..."
                - "Following the money until I run out of gas..."
                """.trimIndent()
            }

            Genre.PUNK_ROCK -> {
                """
                Think about rebellion and underground chaos:
                - "Tuning the guitar for a three-chord revolution..."
                - "Spray painting 'Loading' on the establishment..."
                - "Cranking the amp to 11 just to annoy the neighbors..."
                - "Sewing a patch that says 'I hate patches'..."
                - "Checking if the mosh pit is still in the same place..."
                """.trimIndent()
            }
        }

    fun generateNewCharacterMessage(
        milestone: SagaMilestone.NewCharacter,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String =
        buildString {
            val genre = saga.data.genre
            val genreTone = companion?.tone ?: ""
            appendLine("You are a witty, clever storytelling companion. A new character has just joined the saga.")
            appendLine("Generate ONE SHORT, MEMORABLE congratulatory message that REACTS DIRECTLY to this newcomer.")
            appendLine()
            appendLine("STORY CONTEXT:")
            appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine()
            appendLine("NEW CHARACTER INFO:")
            appendLine(
                milestone.character.toAINormalize(
                    fieldsToExclude = ChatPrompts.characterExclusions,
                ),
            )
            appendLine()
            appendLine("YOUR PERSONA:")
            appendLine("You speak like a ${genre.name.lowercase()} aficionado who:")
            appendLine(companion?.persona ?: "Observes new allies with skepticism and amusement.")
            appendLine()
            appendLine("CREATIVE GUIDELINES:")
            appendLine("- Be ORIGINAL: React to specific traits, name, or role of the character.")
            if (genreTone.isNotEmpty()) {
                appendLine("- Tone: $genreTone with a twist of your persona's distinctive sass.")
            }
            appendLine("- Focus on the IMPACT or VIBE of this character in the world.")
            appendLine("- Maximum 15 words.")
            appendLine("- NO emojis, NO generic 'welcome to the team' phrases.")
            appendLine()
            appendLine("TONE EXAMPLES:")
            appendLine(companion?.conversationalStyle ?: "Greet them creatively.")
            appendLine()
            appendLine("Output ONLY the single-line provocative character reaction:")
        }

    private fun getMilestoneType(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> "New Character Introduction"
            is SagaMilestone.NewEvent -> "Story Event Completion"
            is SagaMilestone.ChapterFinished -> "Chapter Completion"
            is SagaMilestone.ActFinished -> "Act Completion"
            is SagaMilestone.CurrentObjective -> "New Objective Assigned"
            is SagaMilestone.Introduction -> "Story Introduction"
            is SagaMilestone.Loading -> "Loading"
        }

    // Removed hardcoded functions: getGenreInterludeStyle, getGenreTone, getGenreConversationalTone, buildPersonaForGenre

    private fun getReferencePoints(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> {
                ""
            }

            is SagaMilestone.NewEvent -> {
                """
                - The event title and what actually happened
                - Characters involved and their relationships
                - Plot importance and emotional impact
                - React specifically to this event, not generic "event completion"
                """.trimIndent()
            }

            is SagaMilestone.ChapterFinished -> {
                """
                - Chapter title and the arc it covered
                - Major plot points and character developments
                - How it moves the story forward
                - React to what happened in THIS chapter, not chapters in general
                """.trimIndent()
            }

            is SagaMilestone.ActFinished -> {
                """
                - Act title and its scope within the saga
                - Major themes and conflicts resolved
                - How it sets up the next act
                - React to the significance of completing this specific act
                """.trimIndent()
            }

            is SagaMilestone.CurrentObjective -> {
                """
                - The actual objective they need to achieve
                - Stakes and why it matters to the story
                - How it challenges the protagonist
                - React to what lies ahead, not generic "mission loaded"
                """.trimIndent()
            }

            // Introduction handles its own display
            is SagaMilestone.Introduction -> {
                ""
            }

            is SagaMilestone.Loading -> {
                ""
            }
        }

    fun rewriteIntroduction(
        milestone: SagaMilestone.Introduction,
        saga: SagaContent,
        companion: CompanionConfig?,
    ): String =
        buildString {
            val genre = saga.data.genre
            val summary = milestone.sceneSummary

            appendLine(
                "You are a cinematic narrator. The user is resuming their saga at a significant point.",
            )
            appendLine("Your task is to SYNTHESIZE the current state of the story into a SHORT, IMPACTFUL, and CINEMATIC introduction.")
            appendLine()
            appendLine("# STORY IDENTITY")
            appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine()

            if (summary != null) {
                appendLine("# SCENE CONTEXT TO SYNTHESIZE")
                appendLine("- Current Mood: ${summary.mood}")
                appendLine("- Immediate Objective: ${summary.immediateObjective}")
                appendLine("- Main Conflict: ${summary.currentConflict}")
                appendLine("- Active Characters: ${summary.charactersPresent.joinToString()}")
                appendLine("- Location: ${summary.currentLocation}")
                appendLine("- Narrative Weight: ${summary.tensionLevel}")
                appendLine()
                appendLine(
                    "INSTRUCTION: Use ALL the fields above to create a single, evocative narrative hook. Don't just list them; weave them into a cinematic 'Previously on...' or 'The story continues...' vibe.",
                )
            } else if (milestone.introduction.isNotBlank()) {
                appendLine("# ORIGINAL TEXT TO REWRITE")
                appendLine(milestone.introduction)
            } else {
                appendLine("# MILESTONE TYPE")
                appendLine("- Type: ${milestone.type.name.lowercase()}")
                appendLine("- This marks a significant transition in the story")
            }

            appendLine()
            appendLine("YOUR PERSONA:")
            appendLine("You speak like a ${genre.name.lowercase()} aficionado who:")
            appendLine(companion?.persona ?: "Enjoys observing heroes face their fate.")
            appendLine()
            appendLine("REQUIREMENTS:")
            appendLine("- Maximum 35 words (be punchy but descriptive)")
            if (companion?.tone?.isNotEmpty() == true) {
                appendLine("- Maintain ${genre.name.lowercase()} tone: ${companion.tone}")
            }
            appendLine("- Focus on the emotional core or the major shift in the narrative")
            appendLine("- Works perfectly with a typewriter animation reveal")
            appendLine("- NO emojis, NO greeting, NO meta-commentary")
            appendLine()
            appendLine("Output ONLY the cinematic story synthesis:")
        }
}
