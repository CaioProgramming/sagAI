package com.ilustris.sagai.core.ai.model

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.wiki.data.model.Wiki

/**
 * App-internal companion to [GeneratedContent] used by narrative-chain steps (event/chapter/act
 * synthesis) that also persist landmark wikis and character arcs as a side effect of generation.
 *
 * Deliberately NOT a shape reused by [GeneratedContent] itself: [GeneratedContent] is fed as a
 * reified type argument straight into [com.ilustris.sagai.core.ai.GemmaClient] generation calls,
 * where its fields get reflected into the JSON structure the model is instructed to produce (see
 * `buildDataStructure` in GsonTypeUtils.kt). Adding `wikis`/`characters` there would leak into
 * every unrelated generation call's expected output schema. This type is only ever constructed by
 * app code after the model's response has already been parsed and the wikis/characters already
 * persisted, so it never reaches that schema-building path.
 */
data class GeneratedContentWithLore<T>(
    val data: T,
    val finalMessage: String,
    val wikis: List<Wiki> = emptyList(),
    val characters: List<Character> = emptyList(),
)
