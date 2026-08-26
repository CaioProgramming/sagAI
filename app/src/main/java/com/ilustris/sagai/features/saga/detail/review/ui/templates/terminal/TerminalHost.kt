package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.genre.terminal.terminalHost

/**
 * The review's shorthand for [terminalHost] — every terminal review page is holding the whole
 * [SagaContent] anyway, so it shouldn't have to reach for `.data.title` at each call site. The
 * neutral kit takes a bare title instead, since the Milestone screen never has a `SagaContent`.
 */
fun SagaContent.terminalHost(): String = terminalHost(data.title)
