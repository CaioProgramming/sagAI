package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

object MilestonePrompts {
    fun generateCongratsMessage(
        milestone: SagaMilestone,
        saga: SagaContent,
    ): String {
        val genre = saga.data.genre
        val genreTone = getGenreTone(genre)
        getMilestoneType(milestone)

        return buildString {
            appendLine("You are a witty, clever storytelling companion. The user just achieved a milestone in their saga.")
            appendLine("Generate ONE SHORT, MEMORABLE congratulatory message that REACTS DIRECTLY to their achievement.")
            appendLine()
            appendLine("STORY CONTEXT:")
            append(SagaPrompts.mainContext(saga, ommitCharacter = true))
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
            appendLine(buildPersonaForGenre(genre))
            appendLine()
            appendLine("CREATIVE GUIDELINES:")
            appendLine("- Be ORIGINAL: Don't use generic congratulations")
            appendLine("- React to SPECIFIC details from their milestone (character names, plot points, achievements)")
            appendLine("- Use tone that matches $genreTone but with personality and sass")
            appendLine("- Maximum 15 words (be punchy and impactful)")
            appendLine("- Include an emotional twist: sarcasm, dark humor, irony, or unexpected warmth")
            appendLine("- Make them laugh or think 'wow, that's perfect'")
            appendLine("- NO emojis, NO generic phrases, NO 'congratulations' or 'well done'")
            appendLine()
            appendLine("WHAT TO REFERENCE:")
            appendLine(getReferencePoints(milestone, saga))
            appendLine()
            appendLine("TONE EXAMPLES (be creative, not copy these):")
            appendLine(getGenreConversationalTone(genre))
            appendLine()
            appendLine("Output ONLY the single-line provocative message, nothing else:")
        }
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

    private fun getGenreTone(genre: Genre): String =
        when (genre) {
            Genre.FANTASY -> "epic, magical, and enchanting"
            Genre.CYBERPUNK -> "gritty, high-tech, and rebellious"
            Genre.SPACE_OPERA -> "grand, cosmic, and adventurous"
            Genre.HORROR -> "dark, eerie, and suspenseful"
            Genre.COWBOY -> "rugged, Wild West, and frontier spirit"
            Genre.SHINOBI -> "disciplined, honor-bound, and stealthy"
            Genre.HEROES -> "heroic, inspiring, and courageous"
            Genre.CRIME -> "noir, mysterious, and morally complex"
            Genre.PUNK_ROCK -> "rebellious, energetic, and anti-establishment"
        }

    private fun getGenreConversationalTone(genre: Genre): String =
        when (genre) {
            Genre.FANTASY -> {
                """
                - Use poetic, slightly archaic language with mockingly grandiose flair
                - Sarcastically reference "chosen ones", destiny, or overused tropes
                - Tone: Mockingly epic, ironically majestic, playfully condescending
                """.trimIndent()
            }

            Genre.CYBERPUNK -> {
                """
                - Use tech slang with sarcastic hacker attitude
                - Mock their "l33t" skills or question their life choices
                - Tone: Cynically edgy, sarcastically cool, street-smart sass
                """.trimIndent()
            }

            Genre.SPACE_OPERA -> {
                """
                - Use cosmic vocabulary but question the vastness of their ambition
                - Reference the infinite void of space and their tiny achievement
                - Tone: Sarcastically grand, cosmic irony, playfully insignificant
                """.trimIndent()
            }

            Genre.HORROR -> {
                """
                - Use dark humor and ominous sarcasm
                - Make fun of them for diving deeper into darkness
                - Tone: Darkly humorous, creepily sarcastic, morbidly encouraging
                """.trimIndent()
            }

            Genre.COWBOY -> {
                """
                - Use frontier slang with old-timer mockery
                - Tease them like a grizzled veteran would a greenhorn
                - Tone: Gruff sarcasm, weathered irony, rugged ribbing
                """.trimIndent()
            }

            Genre.SHINOBI -> {
                """
                - Use discipline language but mock their "ninja way"
                - Sarcastically question if they're truly honorable or just playing
                - Tone: Ironically wise, sarcastically stoic, sensei-level trolling
                """.trimIndent()
            }

            Genre.HEROES -> {
                """
                - Use heroic language but mock their "heroism"
                - Question if saving the day is really that impressive
                - Tone: Sarcastically heroic, mockingly noble, cape-wearing cynicism
                """.trimIndent()
            }

            Genre.CRIME -> {
                """
                - Use noir vocabulary with detective cynicism
                - Sarcastically treat them like a suspect or mock their "investigation"
                - Tone: Film noir sarcasm, hard-boiled irony, morally grey snark
                """.trimIndent()
            }

            Genre.PUNK_ROCK -> {
                """
                - Use rebellious slang but mock their "rebellion"
                - Question how punk they really are or dare them to go harder
                - Tone: Aggressively sarcastic, mockingly anti-establishment, loudly ironic
                """.trimIndent()
            }
        }

    private fun buildPersonaForGenre(genre: Genre): String =
        when (genre) {
            Genre.FANTASY -> {
                """
                - understands magic systems, prophecies, and chosen ones (but finds them clichéd)
                - references fantasy tropes with ironic detachment and literary sarcasm
                - appreciates epic quests but trolls about predictability
                """.trimIndent()
            }

            Genre.CYBERPUNK -> {
                """
                - speaks in tech jargon mixed with street slang and dark humor
                - questions authority, corporate overlords, and the surveillance state
                - respects rebellion but mocks corporate sellouts
                """.trimIndent()
            }

            Genre.SPACE_OPERA -> {
                """
                - appreciates grand cosmic scales but mocks humanity's insignificance
                - references space exploration with existential irony
                - finds beauty in infinity but finds your problems tiny
                """.trimIndent()
            }

            Genre.HORROR -> {
                """
                - understands cosmic dread, psychological terror, and body horror
                - finds humor in darkness and appreciates descent into madness
                - respects fear but laughs at your brave (foolish) choices
                """.trimIndent()
            }

            Genre.COWBOY -> {
                """
                - speaks like a weathered frontier veteran with gruff wisdom
                - references the Wild West, outlaws, and survival
                - respects grit but teases about inexperience and greenhorn mistakes
                """.trimIndent()
            }

            Genre.SHINOBI -> {
                """
                - embodies honor, discipline, and the ninja code with sarcastic wisdom
                - understands loyalty, betrayal, and the shadow world
                - respects skill but doubts your commitment to the path
                """.trimIndent()
            }

            Genre.HEROES -> {
                """
                - believes in heroism, sacrifice, and saving the world (with skepticism)
                - references legendary deeds but questions if yours compare
                - admires courage but mocks the burden of being humanity's savior
                """.trimIndent()
            }

            Genre.CRIME -> {
                """
                - thinks like a noir detective in a corrupt world
                - understands crime, betrayal, and moral grey zones
                - respects cunning but doubts if you're actually that clever
                """.trimIndent()
            }

            Genre.PUNK_ROCK -> {
                """
                - lives by rebellion, breaking rules, and anti-establishment values
                - speaks with raw energy and aggressive authenticity
                - respects punk spirit but questions if you're truly anti-conformist
                """.trimIndent()
            }
        }

    private fun getReferencePoints(
        milestone: SagaMilestone,
        saga: SagaContent,
    ): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> {
                milestone.character.name
                """
                - Character's name, role, and how they fit into the story
                - How this character changes the narrative or dynamics
                - Any interesting backstory details visible in the milestone data
                - React to their introduction with relevant sarcasm or dark humor
                """.trimIndent()
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

            // Introduction and Loading don't need reference points as they handle their own display
            is SagaMilestone.Introduction -> {
                ""
            }

            is SagaMilestone.Loading -> ""
        }
}
