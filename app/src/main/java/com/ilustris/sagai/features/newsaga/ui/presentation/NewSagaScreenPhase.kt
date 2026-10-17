package com.ilustris.sagai.features.newsaga.ui.presentation

enum class NewSagaScreenPhase {
    /** The idea screen: prompt bar and suggestion chips. */
    Suggestions,

    /** The three dilemmas asked after the idea, before any story is proposed. */
    Quiz,

    /** Stories and protagonists, up to saving. */
    Creation,
}
