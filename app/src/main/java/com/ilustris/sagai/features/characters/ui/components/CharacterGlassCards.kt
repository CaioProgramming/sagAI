package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.ui.RelationShipSheet
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.glassSurface
import com.ilustris.sagai.features.progression.domain.CharacterProgress
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor

private val CardShape = RoundedCornerShape(26.dp)
private const val MAX_ITEMS = 3
private val Hairline = Color.White.copy(alpha = .14f)

/**
 * The sheet's glass block, as in the prototype: no title, just sections stacked with a hairline between them.
 * Each section is a small muted label over its value, or a row with a chevron when it can be tapped.
 */
@Composable
fun GlassRowsCard(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .fillMaxWidth()
                .glassSurface(CardShape, accent)
                .padding(horizontal = 22.dp, vertical = 8.dp),
        content = content,
    )
}

/** One section of a [GlassRowsCard]; [first] sections have no hairline above them. */
@Composable
fun RowSection(
    first: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Column {
        if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                    .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
fun Chevron(modifier: Modifier = Modifier) {
    Icon(
        painterResource(R.drawable.round_arrow_forward_ios_24),
        contentDescription = null,
        tint = Color.White.copy(alpha = .45f),
        modifier = modifier.size(15.dp),
    )
}

/** The small muted label above a value. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f), modifier = modifier)
}

@Composable
fun SectionValue(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 17,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = size.sp), color = MaterialTheme.colorScheme.onBackground, maxLines = maxLines, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

/** The closing "Ver mais" row of a card, with a chevron. */
@Composable
fun SeeMoreSection(
    first: Boolean = false,
    onClick: () -> Unit,
) {
    RowSection(first = first, onClick = onClick) {
        Text(stringResource(R.string.see_more), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = .85f), modifier = Modifier.weight(1f))
        Chevron()
    }
}

/** The gold-ish vertical gradient of the accent, lightening the first stop so it never reads dim on glass. */
private fun accentBrush(accent: Color): Brush = Brush.verticalGradient(listOf(lerp(accent, Color.White, .8f), lerp(accent, Color.White, .6f), lerp(accent, Color.White, .45f)))

/** One row of the special actions card. */
data class SpecialAction(
    val iconRes: Int,
    val label: String,
    val onClick: () -> Unit,
)

/** What used to be small round buttons: glass rows with the icon and text painted in the accent gradient, and a chevron. */
@Composable
fun SpecialActionsCard(
    accent: Color,
    actions: List<SpecialAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    val brush = remember(accent) { accentBrush(accent) }
    GlassRowsCard(accent, modifier) {
        actions.forEachIndexed { index, action ->
            RowSection(first = index == 0, onClick = action.onClick) {
                Icon(painterResource(action.iconRes), contentDescription = null, modifier = Modifier.size(24.dp).gradientFill(brush))
                Text(action.label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, brush = brush), modifier = Modifier.weight(1f))
                Chevron()
            }
        }
    }
}

/** A dark glass bottom sheet with its title, a scrolling rows block and a close button, like the prototype's sheets. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSheet(
    title: String,
    accent: Color,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xF01C1426), contentColor = Color.White) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily), textAlign = TextAlign.Center)
            GlassRowsCard(accent, Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) { content() }
            Text(
                stringResource(R.string.progression_close),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = .12f)).clickable(onClick = onDismiss).padding(horizontal = 26.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** History and personality, each a label over a clipped note; "Ver mais" opens them in full. */
@Composable
fun AboutCard(
    backstory: String,
    personality: String,
    accent: Color,
    modifier: Modifier = Modifier,
    backstoryColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    GlassRowsCard(accent, modifier) {
        RowSection(first = true) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLabel(stringResource(R.string.character_form_title_backstory).lowercase())
                Text(backstory, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp), color = backstoryColor.copy(alpha = .92f), maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
        if (personality.isNotBlank()) {
            RowSection(first = false) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(stringResource(R.string.personality_title).lowercase())
                    Text(personality, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = .92f), maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        SeeMoreSection { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_card_about_title), accent, { open = false }) {
            RowSection(first = true) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(stringResource(R.string.character_form_title_backstory).lowercase())
                    SectionValue(backstory, size = 15)
                }
            }
            if (personality.isNotBlank()) {
                RowSection(first = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionLabel(stringResource(R.string.personality_title).lowercase())
                        SectionValue(personality, size = 15)
                    }
                }
            }
        }
    }
}

private fun heightText(height: Double): String = if (height < 3.0) "${"%.2f".format(height)} m" else "${height.toInt()} cm"

/** Gender, age and race / ethnicity as label-over-value rows; height, weight, appearance and skills are in the sheet. */
@Composable
fun ProfileCard(
    character: Character,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val traits = character.details.physicalTraits
    val raceEthnicity = listOf(traits.race, traits.ethnicity).filter { it.isNotBlank() }.joinToString(" · ")
    val rows =
        listOfNotNull(
            traits.gender.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_title_gender).lowercase() to it },
            traits.age.takeIf { it > 0 }?.let { stringResource(R.string.character_stat_age).lowercase() to it.toString() },
            raceEthnicity.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_card_race_ethnicity) to it },
        )
    GlassRowsCard(accent, modifier) {
        rows.forEachIndexed { index, (label, value) ->
            RowSection(first = index == 0) {
                Column { SectionLabel(label); SectionValue(value) }
            }
        }
        SeeMoreSection(first = rows.isEmpty()) { open = true }
    }
    if (open) {
        GlassSheet(character.name, accent, { open = false }) {
            val facial = traits.facialDetails
            val body = traits.bodyFeatures
            val clothing = character.details.clothing
            val fields =
                listOfNotNull(
                    traits.height.takeIf { it > 0 }?.let { stringResource(R.string.character_stat_height).lowercase() to heightText(it) },
                    traits.weight.takeIf { it > 0 }?.let { stringResource(R.string.character_stat_weight).lowercase() to "${it.toInt()} kg" },
                    facial.hair.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_hair).lowercase() to it },
                    facial.eyes.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_eyes).lowercase() to it },
                    facial.mouth.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_mouth).lowercase() to it },
                    facial.distinctiveMarks.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_scars).lowercase() to it },
                    facial.jawline.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_jawline).lowercase() to it },
                    body.buildAndPosture.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_build).lowercase() to it },
                    body.skinAppearance.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_skin).lowercase() to it },
                    body.distinguishFeatures.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_distinctive).lowercase() to it },
                    clothing.outfitDescription.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_title_clothing_attire).lowercase() to it },
                    clothing.accessories.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_clothing_accessories).lowercase() to it },
                    clothing.carriedItems.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_carried_items).lowercase() to it },
                )
            fields.forEachIndexed { index, (label, value) ->
                RowSection(first = index == 0) { Column { SectionLabel(label); SectionValue(value) } }
            }
            val skills = character.skills
            val abilities = character.details.abilities
            if (skills.isNotEmpty()) {
                RowSection(first = fields.isEmpty()) {
                    Column {
                        SectionLabel(stringResource(R.string.progression_skills_title).lowercase())
                        SkillChipsFlow(skills, accent)
                    }
                }
            } else {
                listOf(
                    stringResource(R.string.character_details_skills).lowercase() to abilities.skillsAndProficiencies,
                    stringResource(R.string.character_details_talents).lowercase() to abilities.uniqueOrSignatureTalents,
                ).filter { it.second.isNotBlank() }.forEachIndexed { index, (label, value) ->
                    RowSection(first = fields.isEmpty() && index == 0) { Column { SectionLabel(label); SectionValue(value) } }
                }
            }
        }
    }
}

/** Relations as rows: avatar, name, the latest relation title below it, and the relation emoji where a chevron would be. */
@Composable
fun RelationsCard(
    relations: List<RelationshipContent>,
    character: Character,
    saga: Saga,
    genre: Genre,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (relations.isEmpty()) return
    var all by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<RelationshipContent?>(null) }
    GlassRowsCard(accent, modifier) {
        relations.take(MAX_ITEMS).forEachIndexed { index, relation -> RelationRow(relation, character, genre, index == 0) { selected = relation } }
        if (relations.size > MAX_ITEMS) SeeMoreSection { all = true }
    }
    if (all) {
        GlassSheet(stringResource(R.string.saga_detail_relationships_section_title), accent, { all = false }) {
            relations.forEachIndexed { index, relation -> RelationRow(relation, character, genre, index == 0) { selected = relation } }
        }
    }
    selected?.let { relation ->
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(onDismissRequest = { selected = null }) { RelationShipSheet(saga = saga, content = relation) }
    }
}

@Composable
private fun RelationRow(
    relation: RelationshipContent,
    character: Character,
    genre: Genre,
    first: Boolean,
    onClick: () -> Unit,
) {
    val other = relation.getCharacterExcluding(character)
    val latest = relation.relationshipEvents.maxByOrNull { it.timestamp }
    RowSection(first = first, onClick = onClick) {
        CharacterAvatar(other, other.hexColor.hexToColor(), genre = genre, modifier = Modifier.size(44.dp))
        Column(Modifier.weight(1f)) {
            SectionValue(other.name, maxLines = 1)
            SectionLabel(latest?.title ?: relation.data.title)
        }
        Text(latest?.emoji ?: relation.data.emoji, fontSize = 26.sp)
    }
}

@Composable
fun ArcsCard(
    arcs: List<CharacterArc>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (arcs.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    GlassRowsCard(accent, modifier) {
        arcs.takeLast(MAX_ITEMS).forEachIndexed { index, arc ->
            RowSection(first = index == 0) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
                SectionValue(arc.title, Modifier.weight(1f))
            }
        }
        if (arcs.size > MAX_ITEMS) SeeMoreSection { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_details_arcs_title), accent, { open = false }) {
            arcs.forEachIndexed { index, arc ->
                RowSection(first = index == 0) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
                    Column(Modifier.weight(1f)) {
                        SectionValue(arc.title)
                        SectionLabel(arc.content)
                    }
                }
            }
        }
    }
}

@Composable
fun KnowledgeCard(
    knowledge: List<String>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (knowledge.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    GlassRowsCard(accent, modifier) {
        knowledge.takeLast(MAX_ITEMS).forEachIndexed { index, fact -> RowSection(first = index == 0) { SectionValue(fact, size = 16, maxLines = 3) } }
        if (knowledge.size > MAX_ITEMS) SeeMoreSection { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_details_knowledge_title), accent, { open = false }) {
            knowledge.forEachIndexed { index, fact -> RowSection(first = index == 0) { SectionValue(fact, size = 16) } }
        }
    }
}

/** The character's events with the XP each one paid; this list is also the feat log. */
@Composable
fun EventsCard(
    events: List<CharacterEventDetails>,
    progress: CharacterProgress?,
    accent: Color,
    onOpenEvent: (Timeline?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    val xpByEvent = remember(progress) { progress?.feats.orEmpty().groupBy { it.timelineId }.mapValues { (_, feats) -> feats.sumOf { it.xp } } }
    val ordered = remember(events) { events.sortedByDescending { it.event.createdAt } }
    val gold = remember(accent) { lerp(accent, Color.White, .5f) }
    GlassRowsCard(accent, modifier) {
        ordered.take(MAX_ITEMS).forEachIndexed { index, e -> EventRow(e, xpByEvent, progress != null, gold, index == 0, onOpenEvent) }
        if (ordered.size > MAX_ITEMS) SeeMoreSection { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.saga_detail_timeline_section_title), accent, { open = false }) {
            ordered.forEachIndexed { index, e -> EventRow(e, xpByEvent, progress != null, gold, index == 0, onOpenEvent) }
        }
    }
}

@Composable
private fun EventRow(
    details: CharacterEventDetails,
    xpByEvent: Map<Int, Int>,
    showXp: Boolean,
    gold: Color,
    first: Boolean,
    onOpenEvent: (Timeline?) -> Unit,
) {
    RowSection(first = first, onClick = { onOpenEvent(details.timeline) }) {
        Column(Modifier.weight(1f)) {
            SectionValue(details.event.title, size = 16)
            SectionLabel(details.event.summary)
        }
        if (showXp) {
            Text(stringResource(R.string.character_card_xp_chip, xpByEvent[details.event.gameTimelineId] ?: 0), style = MaterialTheme.typography.labelMedium, color = gold)
        }
        Chevron()
    }
}

/** Skills as chips inside a sheet row. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SkillChipsFlow(
    skills: List<String>,
    accent: Color,
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        skills.forEach {
            Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = .12f)).padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}
