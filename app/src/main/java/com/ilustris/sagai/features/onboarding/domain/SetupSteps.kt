package com.ilustris.sagai.features.onboarding.domain

/** The stages of the first-access setup flow, in the order they are shown. */
enum class SetupStep {
    /** The app presentation. */
    INTRO,

    /** The optional "how should I call you" question. */
    NAME,

    /** The pitch for bringing your own Gemini key, ending on the key field itself. */
    KEY,
}

/**
 * Which setup steps this user still has to go through.
 *
 * The key is what gates the app, so a user who has one is done: configuring a key is proof they
 * already know the app, and nothing here is worth interrupting them for. Past that gate the intro
 * and the name appear only while still pending, so someone who is merely missing a key (an existing
 * user who predates the bring-your-own-key change) sees the key step and nothing they have already
 * answered.
 */
fun decideSetupSteps(
    keyMissing: Boolean,
    introPending: Boolean,
    namePending: Boolean,
): List<SetupStep> =
    if (!keyMissing) {
        emptyList()
    } else {
        buildList {
            if (introPending) add(SetupStep.INTRO)
            if (namePending) add(SetupStep.NAME)
            add(SetupStep.KEY)
        }
    }
