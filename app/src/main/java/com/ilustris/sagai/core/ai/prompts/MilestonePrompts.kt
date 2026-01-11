package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.currentLanguage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

object MilestonePrompts {
    fun generateCongratsMessage(
        milestone: SagaMilestone,
        saga: SagaContent,
    ): String {
        val genre = saga.data.genre
        val language = currentLanguage()
        val genreTone = getGenreTone(genre)
        val milestoneType = getMilestoneType(milestone)
        val achievementName = milestone.subtitle

        return buildString {
            appendLine("Generate a SHORT, PROVOCATIVE message for a storyteller who just achieved a milestone.")
            appendLine()
            appendLine("CONTEXT:")
            appendLine("- Language: $language")
            appendLine("- Story Genre: ${genre.name}")
            appendLine("- Genre Tone: $genreTone")
            appendLine("- Milestone Type: $milestoneType")
            appendLine("- Achievement: $achievementName")
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
            appendLine("Generate ONLY the provocative message in $language, nothing else:")
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
                    Genre.FANTASY -> "- \"A new soul joins your legend. Their tale begins now!\""
                    Genre.CYBERPUNK -> "- \"New ally jacked in. The network just got stronger.\""
                    Genre.SPACE_OPERA -> "- \"A new star joins your constellation. Adventure awaits!\""
                    Genre.HORROR -> "- \"Another soul enters the darkness. Guide them carefully.\""
                    Genre.COWBOY -> "- \"New rider on the trail. Your posse grows stronger.\""
                    Genre.SHINOBI -> "- \"A new shadow walks beside you. Honor their path.\""
                    Genre.HEROES -> "- \"A new hero rises. Your team grows mightier!\""
                    Genre.CRIME -> "- \"New player in the game. Keep your eyes open.\""
                    Genre.PUNK_ROCK -> "- \"Fresh blood in the crew. Let's make some noise!\""
                }
            }

            is SagaMilestone.NewEvent -> {
                when (genre) {
                    Genre.FANTASY -> "- \"Your saga grows richer. The tale unfolds beautifully!\""
                    Genre.CYBERPUNK -> "- \"Data logged. You're rewriting the system's narrative.\""
                    Genre.SPACE_OPERA -> "- \"Another chapter in the stars. Your odyssey continues!\""
                    Genre.HORROR -> "- \"The darkness deepens. You push forward regardless.\""
                    Genre.COWBOY -> "- \"Another notch on the belt. The legend lives on.\""
                    Genre.SHINOBI -> "- \"Another step on the path. Your discipline shines through.\""
                    Genre.HEROES -> "- \"Another victory for justice. Your heroism inspires!\""
                    Genre.CRIME -> "- \"Another piece of the puzzle. The case unfolds.\""
                    Genre.PUNK_ROCK -> "- \"Another chord struck. Your rebellion echoes loud!\""
                }
            }

            is SagaMilestone.ChapterFinished -> {
                when (genre) {
                    Genre.FANTASY -> "- \"A chapter closes in splendor. Your legend grows eternal!\""
                    Genre.CYBERPUNK -> "- \"Chapter archived. System reboot. What's next, runner?\""
                    Genre.SPACE_OPERA -> "- \"Sector cleared. Plotting coordinates for the next adventure!\""
                    Genre.HORROR -> "- \"You survived another chapter. The night grows darker.\""
                    Genre.COWBOY -> "- \"Another trail conquered. The frontier calls you forward.\""
                    Genre.SHINOBI -> "- \"This path completes. The next awaits your mastery.\""
                    Genre.HEROES -> "- \"Chapter complete! The city thanks you, champion!\""
                    Genre.CRIME -> "- \"Case closed. But the streets always have more secrets.\""
                    Genre.PUNK_ROCK -> "- \"Song's over. Ready to smash the next track?\""
                }
            }

            is SagaMilestone.ActFinished -> {
                when (genre) {
                    Genre.FANTASY -> "- \"An age ends. A new era dawns for your saga!\""
                    Genre.CYBERPUNK -> "- \"Act terminated. System upgrade complete. Level up!\""
                    Genre.SPACE_OPERA -> "- \"Sector conquered. The galaxy awaits your next move!\""
                    Genre.HORROR -> "- \"You escaped the nightmare. But darkness lingers ahead.\""
                    Genre.COWBOY -> "- \"Territory claimed. New frontiers await, partner.\""
                    Genre.SHINOBI -> "- \"Training complete. The true path now reveals itself.\""
                    Genre.HEROES -> "- \"Arc complete! Your heroic legacy grows stronger!\""
                    Genre.CRIME -> "- \"Operation closed. The underworld stirs with new mysteries.\""
                    Genre.PUNK_ROCK -> "- \"Set's done! The crowd roars for more. Encore?\""
                }
            }
        }
}
