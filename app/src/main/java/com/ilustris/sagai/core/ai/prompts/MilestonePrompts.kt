package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

object MilestonePrompts {
    fun generateCongratsMessage(
        milestone: SagaMilestone,
        saga: SagaContent,
    ): String? {
        if (milestone is SagaMilestone.Introduction) {
            return rewriteIntroduction(milestone, saga)
        }

        if (milestone is SagaMilestone.Loading) {
            return generateLoadingMessage(saga)
        }

        if (milestone is SagaMilestone.NewCharacter) {
            return generateNewCharacterMessage(milestone, saga)
        }

        if (milestone is SagaMilestone.CurrentObjective) {
            return null
        }

        val genre = saga.data.genre
        val genreTone = getGenreTone(genre)

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
            appendLine(buildPersonaForGenre(genre))
            appendLine()
            appendLine("CREATIVE GUIDELINES:")
            appendLine("- Be ORIGINAL: Don't use generic congratulations")
            appendLine("- React to SPECIFIC details from the milestone above (character names, plot points, achievements)")
            appendLine("- Use tone that matches $genreTone but with personality and sass")
            appendLine("- Maximum 15 words (be punchy and impactful)")
            appendLine("- Include an emotional twist: sarcasm, dark humor, irony, or unexpected warmth")
            appendLine("- Make them laugh or think 'wow, that's perfect'")
            appendLine("- NO emojis, NO generic phrases, NO 'congratulations' or 'well done'")
            appendLine()
            appendLine("WHAT TO REFERENCE:")
            appendLine(getReferencePoints(milestone))
            appendLine()
            appendLine("TONE EXAMPLES (be creative, not copy these):")
            appendLine(getGenreConversationalTone(genre))
            appendLine()
            appendLine("Output ONLY the single-line provocative message, nothing else:")
        }
    }

    fun generateLoadingMessage(saga: SagaContent): String =
        buildString {
            val genre = saga.data.genre
            appendLine("You are a witty narrator creating a brief interlude while the story loads.")
            appendLine("Generate a SHORT, ATMOSPHERIC transition phrase like comic books and serialized fiction use.")
            appendLine()
            appendLine("# STORY IDENTITY")
            appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine()
            appendLine("# GENRE INTERLUDE STYLE (INSPIRATION ONLY - DO NOT COPY)")
            appendLine(getGenreInterludeStyle(genre))
            appendLine()
            appendLine("CREATIVITY RULES:")
            appendLine("- NEVER copy the examples above - they're just inspiration for the VIBE")
            appendLine("- Invent something fresh, witty, and unexpected every time")
            appendLine("- Think like a comic book writer creating a unique panel caption")
            appendLine("- Be clever and genre-authentic, not generic")
            appendLine()
            appendLine("REQUIREMENTS:")
            appendLine("- Maximum 8 words (shorter is better)")
            appendLine("- Sound like a narrative caption, NOT loading screen text")
            appendLine("- Be vague about specifics but evocative of the genre's atmosphere")
            appendLine("- Create anticipation without spoiling anything")
            appendLine("- NO emojis, NO ellipsis abuse, NO meta-commentary about 'loading'")
            appendLine()
            appendLine("Output ONLY the interlude phrase:")
        }

    private fun getGenreInterludeStyle(genre: Genre): String =
        when (genre) {
            Genre.FANTASY -> {
                """
                Use immersive action-based moments from fantasy adventures:
                - "Sharpening the blade..."
                - "Consulting the ancient tome..."
                - "Brewing a peculiar concoction..."
                - "The runes begin to glow..."
                - "Feeding the griffon..."
                """.trimIndent()
            }

            Genre.CYBERPUNK -> {
                """
                Use terminal/hacker aesthetic actions:
                - "> Decrypting signal..."
                - "> Bypassing firewall..."
                - "Jacking into the network..."
                - "Rebooting neural interface..."
                - "Patching the exploit..."
                """.trimIndent()
            }

            Genre.SPACE_OPERA -> {
                """
                Use spaceship/exploration actions:
                - "Recalibrating hyperdrive..."
                - "Scanning for life signs..."
                - "Charging the ion cannons..."
                - "Plotting the jump coordinates..."
                - "Sealing the airlock..."
                """.trimIndent()
            }

            Genre.HORROR -> {
                """
                Use creeping dread and unsettling moments:
                - "Something watches..."
                - "The floorboards creak..."
                - "Checking the locks again..."
                - "The candle flickers..."
                - "Listening to the silence..."
                """.trimIndent()
            }

            Genre.COWBOY -> {
                """
                Use frontier survival and cowboy actions:
                - "Loading the revolver..."
                - "Tending the fire..."
                - "Checking the horizon..."
                - "Saddling up..."
                - "Counting the bounty..."
                """.trimIndent()
            }

            Genre.SHINOBI -> {
                """
                Use ninja training and stealth actions:
                - "Sharpening the kunai..."
                - "Mixing the poison..."
                - "Meditating on the void..."
                - "Reading the scroll..."
                - "Blending into shadow..."
                """.trimIndent()
            }

            Genre.HEROES -> {
                """
                Use superhero preparation moments:
                - "Suiting up..."
                - "Scanning police frequencies..."
                - "Patching the suit..."
                - "Reviewing the case files..."
                - "The signal lights up..."
                """.trimIndent()
            }

            Genre.CRIME -> {
                """
                Use noir investigation and underworld actions:
                - "Lighting another cigarette..."
                - "Reviewing the evidence..."
                - "Following the money..."
                - "Tailing the suspect..."
                - "Loading the piece..."
                """.trimIndent()
            }

            Genre.PUNK_ROCK -> {
                """
                Use rebellious underground actions:
                - "Tuning the guitar..."
                - "Spray painting the wall..."
                - "Printing the zines..."
                - "Cranking the amp..."
                - "Patching the jacket..."
                """.trimIndent()
            }
        }

    fun generateNewCharacterMessage(
        milestone: SagaMilestone.NewCharacter,
        saga: SagaContent,
    ): String =
        buildString {
            val genre = saga.data.genre
            val genreTone = getGenreTone(genre)
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
            appendLine(buildPersonaForGenre(genre))
            appendLine()
            appendLine("CREATIVE GUIDELINES:")
            appendLine("- Be ORIGINAL: React to specific traits, name, or role of the character.")
            appendLine("- Tone: $genreTone with a twist of your persona's distinctive sass.")
            appendLine("- Focus on the IMPACT or VIBE of this character in the world.")
            appendLine("- Maximum 15 words.")
            appendLine("- NO emojis, NO generic 'welcome to the team' phrases.")
            appendLine()
            appendLine("TONE EXAMPLES:")
            appendLine(getGenreConversationalTone(genre))
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
    ): String =
        buildString {
            val genre = saga.data.genre
            val hasOriginalText = milestone.introduction.isNotBlank()

            if (hasOriginalText) {
                // REWRITE MODE: Shorten existing introduction
                appendLine(
                    "You are a cinematic narrator. The user is beginning a new ${milestone.type.name.lowercase()} in their saga.",
                )
                appendLine("The original introduction text is too long. Rewrite it to be SHORT, IMPACTFUL, and CINEMATIC.")
                appendLine()
                appendLine("# STORY IDENTITY")
                appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
                appendLine()
                appendLine("# ORIGINAL TEXT TO REWRITE")
                appendLine(milestone.introduction)
            } else {
                // GENERATE MODE: Create fresh introduction from context
                appendLine(
                    "You are a cinematic narrator. The user is beginning a new ${milestone.type.name.lowercase()} in their saga.",
                )
                appendLine("Generate a SHORT, IMPACTFUL, CINEMATIC introduction quote for this moment.")
                appendLine()
                appendLine("# STORY IDENTITY")
                appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
                appendLine()
                appendLine("# MILESTONE TYPE")
                appendLine("- Type: ${milestone.type.name.lowercase()}")
                appendLine("- This marks a significant transition in the story")
            }

            appendLine()
            appendLine("YOUR PERSONA:")
            appendLine("You speak like a ${genre.name.lowercase()} aficionado who:")
            appendLine(buildPersonaForGenre(genre))
            appendLine()
            appendLine("REQUIREMENTS:")
            appendLine("- Maximum 25 words (be punchy)")
            appendLine("- Maintain ${genre.name.lowercase()} tone: ${getGenreTone(genre)}")
            appendLine("- Focus on the emotional core or the major shift in the narrative")
            appendLine("- Works perfectly with a typewriter animation reveal")
            appendLine("- NO emojis, NO greeting, NO meta-commentary")
            appendLine()
            appendLine("Output ONLY the cinematic introduction:")
        }
}
