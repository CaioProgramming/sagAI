package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.ArcSourceType
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc

@Composable
fun CharacterDetailSection(
    title: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
    ) {
        Text(
            title,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = contentColor,
                ),
        )
        content()
    }
}

@Composable
fun CharacterDetailText(
    text: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                color = contentColor,
            ),
        modifier = modifier.padding(vertical = 8.dp),
    )
}

@Composable
fun CharacterDetailLabelValue(
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = contentColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                ),
        )
        Text(
            value,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = contentColor,
                ),
        )
    }
}

@Composable
fun CharacterArcTimeline(
    arcs: List<CharacterArc>,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        arcs.forEachIndexed { index, arc ->
            CharacterArcEntry(
                arc = arc,
                contentColor = contentColor,
                accentColor = accentColor,
                isLast = index == arcs.lastIndex,
            )
        }
    }
}

@Composable
private fun CharacterArcEntry(
    arc: CharacterArc,
    contentColor: Color,
    accentColor: Color,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor),
            )
            if (!isLast) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 2.dp, height = 48.dp)
                            .background(accentColor.copy(alpha = 0.3f)),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(bottom = if (isLast) 0.dp else 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    arc.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = contentColor,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    arc.sourceType.label(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = accentColor,
                        ),
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Text(
                arc.content,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = contentColor.copy(alpha = 0.85f),
                    ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ArcSourceType.label(): String =
    when (this) {
        ArcSourceType.CHAPTER -> stringResource(R.string.character_arc_source_chapter)
        ArcSourceType.ACT -> stringResource(R.string.character_arc_source_act)
    }

@Composable
fun CharacterKnowledgeList(
    knowledge: List<String>,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        knowledge.forEach { fact ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "•",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                        ),
                )
                Text(
                    fact,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = contentColor,
                        ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun CharacterAppearanceSection(
    character: Character,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val traits = character.details.physicalTraits
    val facial = traits.facialDetails
    val body = traits.bodyFeatures
    val clothing = character.details.clothing

    val fields =
        listOfNotNull(
            facial.hair.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_label_hair) to it
            },
            facial.eyes.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_label_eyes) to it
            },
            facial.mouth.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_label_mouth) to it
            },
            facial.distinctiveMarks.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_label_scars) to it
            },
            facial.jawline.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_details_jawline) to it
            },
            body.buildAndPosture.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_details_build) to it
            },
            body.skinAppearance.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_details_skin) to it
            },
            body.distinguishFeatures.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_details_distinctive) to it
            },
            clothing.outfitDescription.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_title_clothing_attire) to it
            },
            clothing.accessories.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_form_label_clothing_accessories) to it
            },
            clothing.carriedItems.takeIf { it.isNotBlank() }?.let {
                stringResource(R.string.character_details_carried_items) to it
            },
        )

    if (fields.isEmpty()) return

    CharacterDetailSection(
        title = stringResource(R.string.character_details_appearance_title),
        contentColor = contentColor,
        modifier = modifier,
    ) {
        fields.forEach { (label, value) ->
            CharacterDetailLabelValue(label = label, value = value, contentColor = contentColor)
        }
    }
}

@Composable
fun CharacterAbilitiesSection(
    character: Character,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val abilities = character.details.abilities
    val skills = abilities.skillsAndProficiencies.takeIf { it.isNotBlank() }
    val talents = abilities.uniqueOrSignatureTalents.takeIf { it.isNotBlank() }

    if (skills == null && talents == null) return

    CharacterDetailSection(
        title = stringResource(R.string.character_details_abilities_title),
        contentColor = contentColor,
        modifier = modifier,
    ) {
        skills?.let {
            CharacterDetailLabelValue(
                label = stringResource(R.string.character_details_skills),
                value = it,
                contentColor = contentColor,
            )
        }
        talents?.let {
            CharacterDetailLabelValue(
                label = stringResource(R.string.character_details_talents),
                value = it,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
fun CharacterDetailDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(.1f),
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
    )
}
