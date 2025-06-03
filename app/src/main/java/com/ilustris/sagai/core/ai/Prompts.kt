package com.ilustris.sagai.core.ai

import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.Genre.*

fun sagaPrompt(
    title: String,
    description: String,
    genre: String,
) = """
Develop a detailed synopsis for an RPG adventure. This synopsis should establish the adventure's setting and core theme, outlining the journey players will undertake.

Adventure Details:
1.  **Title:** $title
2.  **Description:** $description
3.  **Genre:** $genre

**Key Thematic / Contextual Elements to Integrate:** [Insert specific themes or contextual details here, e.g., "A dying magical forest and a looming corruption," or "A political conspiracy within a steampunk metropolis," or "Survival against alien invaders on a desolate planet."]

The synopsis should include:
1.  An engaging hook that sets the scene.
2.  The primary antagonist or opposing force.
3.  The main quest or objective for the player characters.
4.  Potential for moral dilemmas or significant choices.
5.  An indication of the adventure's scope and potential for a grand finale.

Target a synopsis length of 100 words, ensuring it captures the essence of a playable RPG experience.
"""

fun Genre.iconPrompt(description: String) =
    when (this) {
        FANTASY ->
            """
        Type: Ultra close-up portrait of a character's face and upper torso, dynamic composition.
        Subject: Warrior.
        Appearance: Wearing armor with red details.  
        Action: Intimidating pose, Looking in 45 degree angle. With a brave and intimidating expression. The pose of the upper body should convey absolute confidence and readiness 
        Art Style: In the art style of Jim Lee, known for dynamic action, detailed characters, and a modern comic book aesthetic.  
        Background: Medieval fantasy landscape with orange and red color pallete. 
        Story context: "$description"
        """
        SCI_FI ->
            """
     Type:Ultra close-up portrait of a character's face and upper torso, dynamic composition.
    Subject:Cyberpunk mercenary.
    Appearance:Wearing a cyborg suit with purple details with metal reflective details.
    Action:The character's face is looking over the shoulder, displaying a highly confident expression. The pose of the upper body should convey absolute confidence and readiness.
    Art Style:Cyberpunk, with flat colors and strong, sharp linework. Reminiscent of Studio Trigger, emphasizing dynamic action and character presence.
    Color Palette:Primarily dark blue and purple for the character and weapon, with contrasting neon accents.
    Background:A single, strong, solid color background that provides a powerful, stark contrast to the character, making them stand out prominently.
    Lighting:Dramatic and sharp, casting strong shadows to enhance the intimidating feel and highlight the character's facial features and the metallic details.
    Story context: $description
            """
    }.trimIndent()
