package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.ilustris.sagai.ui.theme.hexToColor

private val CardShape = RoundedCornerShape(24.dp)
private const val MAX_ITEMS = 3

/** The glass surface every block of the character sheet sits on. */
@Composable
fun GlassCard(
    accent: Color,
    modifier: Modifier = Modifier,
    title: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth()
                .glassSurface(CardShape, accent)
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        title?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
        }
        content()
    }
}

@Composable
private fun SeeMore(
    accent: Color,
    onClick: () -> Unit,
) {
    Text(
        stringResource(R.string.see_more),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
    )
}

@Composable
private fun LabelValue(
    label: String,
    value: String,
    contentColor: Color,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = .6f), modifier = Modifier.weight(.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = contentColor, modifier = Modifier.weight(.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassSheet(
    title: String,
    onDismiss: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            content()
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** One row of the special actions card. */
data class SpecialAction(
    val iconRes: Int,
    val label: String,
    val onClick: () -> Unit,
)

/** The actions that used to be small round buttons: one tinted card, each row with icon, text and chevron. */
@Composable
fun SpecialActionsCard(
    accent: Color,
    actions: List<SpecialAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    val brush = remember(accent) { Brush.verticalGradient(accent.darkerPalette(count = 4, factor = 0.1f)) }
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(brush),
    ) {
        actions.forEachIndexed { index, action ->
            if (index > 0) HorizontalDivider(color = Color.White.copy(alpha = .18f), modifier = Modifier.padding(horizontal = 20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = action.onClick).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(painterResource(action.iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Text(action.label, style = MaterialTheme.typography.titleSmall, color = Color.White, modifier = Modifier.weight(1f))
                Icon(painterResource(R.drawable.round_arrow_forward_ios_24), contentDescription = null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** History and personality, each clipped; the whole text is one tap away. */
@Composable
fun AboutCard(
    backstory: String,
    personality: String,
    accent: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    backstoryColor: Color = contentColor,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    GlassCard(accent, modifier, stringResource(R.string.character_card_about_title), contentColor) {
        Text(stringResource(R.string.character_form_title_backstory), style = MaterialTheme.typography.labelLarge, color = accent)
        Text(backstory, style = MaterialTheme.typography.bodyMedium, color = backstoryColor, maxLines = 5, overflow = TextOverflow.Ellipsis)
        if (personality.isNotBlank()) {
            Text(stringResource(R.string.personality_title), style = MaterialTheme.typography.labelLarge, color = accent)
            Text(personality, style = MaterialTheme.typography.bodyMedium, color = contentColor, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        SeeMore(accent) { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_card_about_title), { open = false }) {
            item { Text(backstory, style = MaterialTheme.typography.bodyMedium) }
            if (personality.isNotBlank()) {
                item { Text(stringResource(R.string.personality_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                item { Text(personality, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

/** Ethnicity and the first strokes of the appearance; the stats row already sits under the name. The rest is in the sheet. */
@Composable
fun ProfileCard(
    character: Character,
    accent: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val traits = character.details.physicalTraits
    val facial = traits.facialDetails
    val body = traits.bodyFeatures
    val highlights =
        listOfNotNull(
            traits.ethnicity.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_ethnicity) to it },
            facial.hair.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_hair) to it },
            facial.eyes.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_form_label_eyes) to it },
            body.buildAndPosture.takeIf { it.isNotBlank() }?.let { stringResource(R.string.character_details_build) to it },
        ).take(MAX_ITEMS)
    GlassCard(accent, modifier, stringResource(R.string.character_card_profile_title), contentColor) {
        highlights.forEach { (label, value) -> LabelValue(label, value, contentColor) }
        SeeMore(accent) { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_card_profile_title), { open = false }) {
            item { CharacterAppearanceSection(character = character, contentColor = MaterialTheme.colorScheme.onSurface) }
            item {
                if (character.skills.isNotEmpty()) {
                    CharacterSkillChips(character.skills, accent, MaterialTheme.colorScheme.onSurface)
                } else {
                    CharacterAbilitiesSection(character = character, contentColor = MaterialTheme.colorScheme.onSurface)
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
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (relations.isEmpty()) return
    var all by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<RelationshipContent?>(null) }
    GlassCard(accent, modifier, stringResource(R.string.saga_detail_relationships_section_title), contentColor) {
        relations.take(MAX_ITEMS).forEach { RelationRow(it, character, genre, contentColor) { selected = it } }
        if (relations.size > MAX_ITEMS) SeeMore(accent) { all = true }
    }
    if (all) {
        GlassSheet(stringResource(R.string.saga_detail_relationships_section_title), { all = false }) {
            relations.forEach { relation -> item { RelationRow(relation, character, genre, MaterialTheme.colorScheme.onSurface) { selected = relation } } }
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
    contentColor: Color,
    onClick: () -> Unit,
) {
    val other = relation.getCharacterExcluding(character)
    val latest = relation.relationshipEvents.maxByOrNull { it.timestamp }
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CharacterAvatar(other, other.hexColor.hexToColor(), genre = genre, modifier = Modifier.size(44.dp))
        Column(Modifier.weight(1f)) {
            Text(other.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(latest?.title ?: relation.data.title, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = .65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(latest?.emoji ?: relation.data.emoji, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ArcsCard(
    arcs: List<CharacterArc>,
    accent: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (arcs.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    GlassCard(accent, modifier, stringResource(R.string.character_details_arcs_title), contentColor) {
        CharacterArcTimeline(arcs = arcs.takeLast(MAX_ITEMS), contentColor = contentColor, accentColor = accent)
        if (arcs.size > MAX_ITEMS) SeeMore(accent) { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_details_arcs_title), { open = false }) {
            item { CharacterArcTimeline(arcs = arcs, contentColor = MaterialTheme.colorScheme.onSurface, accentColor = accent) }
        }
    }
}

@Composable
fun KnowledgeCard(
    knowledge: List<String>,
    accent: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (knowledge.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    GlassCard(accent, modifier, stringResource(R.string.character_details_knowledge_title), contentColor) {
        CharacterKnowledgeList(knowledge = knowledge.takeLast(MAX_ITEMS), contentColor = contentColor, accentColor = accent)
        if (knowledge.size > MAX_ITEMS) SeeMore(accent) { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.character_details_knowledge_title), { open = false }) {
            item { CharacterKnowledgeList(knowledge = knowledge, contentColor = MaterialTheme.colorScheme.onSurface, accentColor = accent) }
        }
    }
}

/** The character's events with the XP each one paid; this list is also the feat log. */
@Composable
fun EventsCard(
    events: List<CharacterEventDetails>,
    progress: CharacterProgress?,
    accent: Color,
    contentColor: Color,
    onOpenEvent: (Timeline?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    val xpByEvent = remember(progress) { progress?.feats.orEmpty().groupBy { it.timelineId }.mapValues { (_, feats) -> feats.sumOf { it.xp } } }
    val ordered = remember(events) { events.sortedByDescending { it.event.createdAt } }
    GlassCard(accent, modifier, stringResource(R.string.saga_detail_timeline_section_title), contentColor) {
        ordered.take(MAX_ITEMS).forEach { EventRow(it, xpByEvent, progress != null, accent, contentColor, onOpenEvent) }
        if (ordered.size > MAX_ITEMS) SeeMore(accent) { open = true }
    }
    if (open) {
        GlassSheet(stringResource(R.string.saga_detail_timeline_section_title), { open = false }) {
            ordered.forEach { event ->
                item { EventRow(event, xpByEvent, progress != null, accent, MaterialTheme.colorScheme.onSurface, onOpenEvent) }
            }
        }
    }
}

@Composable
private fun EventRow(
    details: CharacterEventDetails,
    xpByEvent: Map<Int, Int>,
    showXp: Boolean,
    accent: Color,
    contentColor: Color,
    onOpenEvent: (Timeline?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onOpenEvent(details.timeline) },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(details.event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(details.event.summary, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = .7f), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        if (showXp) {
            Text(
                stringResource(R.string.character_card_xp_chip, xpByEvent[details.event.gameTimelineId] ?: 0),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.clip(CircleShape).background(accent.copy(alpha = .25f)).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
