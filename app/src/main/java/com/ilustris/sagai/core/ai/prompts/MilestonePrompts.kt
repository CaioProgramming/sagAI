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
        val milestoneType = getMilestoneType(milestone)
        milestone.subtitle

        return buildString {
            appendLine("Generate a SHORT, PROVOCATIVE message for a storyteller who just achieved a milestone.")
            appendLine()
            append(SagaPrompts.mainContext(saga, ommitCharacter = true))
            appendLine("- Genre Tone: $genreTone")

            appendLine("- Milestone: $milestoneType")
            appendLine(
                milestone.toAINormalize(
                    fieldsToExclude =
                        ChatPrompts.characterExclusions
                            .plus(ChapterPrompts.CHAPTER_EXCLUSIONS)
                            .plus(TimelinePrompts.timelineExclusions),
                ),
            )
            appendLine()
            appendLine("TONE & STYLE:")
            appendLine(getGenreConversationalTone(genre))
            appendLine()
            appendLine("RULES:")
            appendLine("- Maximum 15 words")
            appendLine("- Be SARCASTIC, IRONIC, PLAYFULLY MEAN, or UNEXPECTEDLY FUNNY")
            appendLine("- Make the user laugh or say 'I can't believe they said that!'")
            appendLine("- Tease them, challenge them, or be darkly humorous")
            appendLine("- Match the $genreTone atmosphere but with ATTITUDE")
            appendLine("- Address the storyteller directly with sass")
            appendLine("- NO emojis or special characters")
            appendLine("- NO boring generic praise - be PROVOCATIVE and MEMORABLE")
            appendLine("- Can be self-deprecating, questioning their choices, or mockingly impressed")
            appendLine()
            appendLine("EXAMPLES for different genres/milestones:")
            appendLine(getExamplesForMilestone(milestone, genre))
            appendLine()
            appendLine("Generate ONLY the provocative message, nothing else:")
        }
    }

    private fun getMilestoneType(milestone: SagaMilestone): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> "New Character Introduction"
            is SagaMilestone.NewEvent -> "Story Event Completion"
            is SagaMilestone.ChapterFinished -> "Chapter Completion"
            is SagaMilestone.ActFinished -> "Act Completion"
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

    private fun getExamplesForMilestone(
        milestone: SagaMilestone,
        genre: Genre,
    ): String =
        when (milestone) {
            is SagaMilestone.NewCharacter -> {
                when (genre) {
                    Genre.FANTASY -> "- \"Oh great, another 'chosen one' with destiny issues.\""
                    Genre.CYBERPUNK -> "- \"New NPC unlocked. Hope they're not as glitchy as you.\""
                    Genre.SPACE_OPERA -> "- \"Another speck in the infinite void. How touching.\""
                    Genre.HORROR -> "- \"Fresh meat for the grinder. This'll end well.\""
                    Genre.COWBOY -> "- \"Another greenhorn to babysit. Fantastic.\""
                    Genre.SHINOBI -> "- \"New shadow enters. Still not stealthy enough, amateur.\""
                    Genre.HEROES -> "- \"Another cape? The dry-cleaning bill must be insane.\""
                    Genre.CRIME -> "- \"New suspect unlocked. Everyone's guilty until proven otherwise.\""
                    Genre.PUNK_ROCK -> "- \"Fresh blood? Let's see if they're actually punk or poser.\""
                }
            }

            is SagaMilestone.NewEvent -> {
                when (genre) {
                    Genre.FANTASY -> "- \"Wow, something happened in your fantasy. Groundbreaking.\""
                    Genre.CYBERPUNK -> "- \"Event logged. The machines are impressed. Barely.\""
                    Genre.SPACE_OPERA -> "- \"In the vastness of space, this matters so little. But sure.\""
                    Genre.HORROR -> "- \"Digging deeper into madness? Bold strategy, let's see how it plays.\""
                    Genre.COWBOY -> "- \"Another tale for the saloon. Wake me when it gets interesting.\""
                    Genre.SHINOBI -> "- \"Progress noted. Still miles from mastery, grasshopper.\""
                    Genre.HEROES -> "- \"Saved the day again? Must be Tuesday.\""
                    Genre.CRIME -> "- \"Another clue found. Congrats, detective obvious.\""
                    Genre.PUNK_ROCK -> "- \"That barely counts as rebellion but okay.\""
                }
            }

            is SagaMilestone.ChapterFinished -> {
                when (genre) {
                    Genre.FANTASY -> "- \"Chapter done. Your epic saga continues being... adequate.\""
                    Genre.CYBERPUNK -> "- \"Chapter closed. System impressed. Slightly.\""
                    Genre.SPACE_OPERA -> "- \"One chapter in infinity. Feel accomplished yet?\""
                    Genre.HORROR -> "- \"You survived? That's... actually surprising. Well done.\""
                    Genre.COWBOY -> "- \"Chapter finished. Didn't fall off your horse this time.\""
                    Genre.SHINOBI -> "- \"Chapter complete. Maybe you're not entirely hopeless.\""
                    Genre.HEROES -> "- \"Chapter saved. The city throws you a mediocre parade.\""
                    Genre.CRIME -> "- \"Case closed? Don't quit your day job, detective.\""
                    Genre.PUNK_ROCK -> "- \"Set finished. That was almost punk. Almost.\""
                }
            }

            is SagaMilestone.ActFinished -> {
                when (genre) {
                    Genre.FANTASY -> "- \"Entire act done? Your mother would be so proud.\""
                    Genre.CYBERPUNK -> "- \"Act terminated. Even the AI didn't see that coming.\""
                    Genre.SPACE_OPERA -> "- \"Act complete. The universe remains unimpressed but sure.\""
                    Genre.HORROR -> "- \"You finished an act and kept your sanity? Miracles happen.\""
                    Genre.COWBOY -> "- \"Whole act done. You might actually survive the frontier.\""
                    Genre.SHINOBI -> "- \"Act mastered. Perhaps there's a ninja in you after all.\""
                    Genre.HEROES -> "- \"Full arc done. Try not to let the glory go to your head.\""
                    Genre.CRIME -> "- \"Operation complete. The streets are still dirty but whatever.\""
                    Genre.PUNK_ROCK -> "- \"Entire set done. Okay, that was actually pretty punk.\""
                }
            }
        }
}
