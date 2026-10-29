package com.ilustris.sagai.ui.components.titlefx

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal class TitleFxTrailPoint(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var s: Float = 1f,
)

/**
 * Clock, recurring events and touch state of one title, mirroring the HTML lab's `ev` / `inp`.
 * All positions are in the shader's uv space (0..1, y up).
 */
internal class TitleFxState(
    private val cfg: TitleFxConfig,
    private val interactive: Boolean,
    private val playEntry: Boolean,
) {
    var t = 0f
        private set
    private var la = 0f
    private var lb = 0f
    var seedA = 1f
        private set
    var seedB = 1f
        private set

    var cutX = 0.5f
        private set
    var cutY = 0.5f
        private set
    var cutAng = 0f
        private set
    var cutUser = false
        private set

    private var down = false
    private var x = 0.5f
    private var y = 0.5f
    private var vx = 0f
    private var vy = 0f
    private var lastT = 0L
    private var lastX = 0.5f
    private var lastY = 0.5f
    private var startX = 0f
    private var startY = 0f
    private var hasStart = false
    private var fired = false
    private var active = 0f
    private val trail = ArrayDeque<TitleFxTrailPoint>()

    val trailBuf = FloatArray(32)
    val trailVBuf = FloatArray(16)

    /** Text width over box height, used to decide whether a drag is long enough to cut. */
    var textWOverH = 1f
    var aspect = 1f

    init {
        // shinobi: the first cut comes after the entry; cowboy: the iron lands now (la = 0)
        if (cfg.dragCut) la = (if (playEntry) cfg.entryDur else 0f) + 1f
    }

    private val entering: Boolean
        get() = playEntry && cfg.entryDur > 0f && t < cfg.entryDur + 1.2f

    val entryTime: Float get() = if (playEntry) t else 100f
    val evtA: Float get() = if (cfg.evtPeriod > 0f) t - la else 100f
    val evtB: Float get() = if (cfg.evtPeriod > 0f || cfg.tapEvt) t - lb else 100f
    val pointerX: Float get() = x
    val pointerY: Float get() = y
    val pointerDown: Float get() = if (down) 1f else 0f
    val pointerActive: Float get() = if (interactive) active else 0f

    fun update(dt: Float) {
        t += dt
        if (cfg.evtPeriod > 0f && t - la > cfg.evtPeriod) {
            la = t
            seedA++
            cutUser = false
        }
        val ent = entering
        active += (((if (down && !ent) 1f else 0f)) - active) * min(1f, dt * 9f)
        val vd = exp(-dt * 7f)
        vx *= vd
        vy *= vd
        val decay = exp(-dt * (if (cfg.dragCut) 3.2f else 1.7f))
        for (i in 0 until 8) {
            val tp = if (interactive && !ent) trail.getOrNull(i) else null
            if (tp != null) {
                tp.s *= decay
                trailBuf[i * 4] = tp.x
                trailBuf[i * 4 + 1] = tp.y
                trailBuf[i * 4 + 2] = tp.s
                trailBuf[i * 4 + 3] = 0f
                trailVBuf[i * 2] = tp.vx.coerceIn(-4f, 4f)
                trailVBuf[i * 2 + 1] = tp.vy.coerceIn(-4f, 4f)
            } else {
                trailBuf[i * 4 + 2] = 0f
                trailVBuf[i * 2] = 0f
                trailVBuf[i * 2 + 1] = 0f
            }
        }
    }

    private fun push(
        px: Float,
        py: Float,
        pvx: Float,
        pvy: Float,
    ) {
        trail.addFirst(TitleFxTrailPoint(px, py, pvx, pvy))
        if (trail.size > 8) trail.removeLast()
    }

    fun onDown(
        px: Float,
        py: Float,
        timeMs: Long,
    ) {
        if (!interactive || entering) return
        down = true
        x = px
        y = py
        startX = px
        startY = py
        hasStart = true
        fired = false
        lastT = timeMs
        lastX = px
        lastY = py
        push(px, py, 0f, 0f)
        if (cfg.tapEvt) lb = t
    }

    fun onMove(
        px: Float,
        py: Float,
        timeMs: Long,
    ) {
        if (!interactive || !down) return
        val dt2 = max(0.008f, (timeMs - lastT) / 1000f)
        vx = vx * 0.5f + ((px - lastX) / dt2) * 0.5f
        vy = vy * 0.5f + ((py - lastY) / dt2) * 0.5f
        lastT = timeMs
        lastX = px
        lastY = py
        x = px
        y = py
        val head = trail.firstOrNull()
        val spacing = if (cfg.dragCut) 0.05f else 0.025f
        if (head == null || hypot(px - head.x, py - head.y) > spacing) {
            push(px, py, vx, vy)
        } else {
            head.x = px
            head.y = py
            head.vx = vx
            head.vy = vy
            head.s = 1f
        }
    }

    /** Shinobi: the blade trail is drawn while dragging; on release the cut plays along the gesture. */
    fun onUp() {
        if (!down) return
        down = false
        if (!cfg.dragCut || cfg.opts[1] < 0.5f || !hasStart || fired) return
        val dx = (x - startX) * aspect
        val dy = y - startY
        val len = hypot(dx, dy)
        if (len > 0.3f * textWOverH && abs(dy) < 1.4f * abs(dx)) {
            cutX = (x + startX) / 2f
            cutY = (y + startY) / 2f
            cutAng = atan2(dy, dx)
            cutUser = true
            la = t - 0.55f
            seedA++
            fired = true
            trail.forEach { it.s *= 0.35f }
        }
    }
}
