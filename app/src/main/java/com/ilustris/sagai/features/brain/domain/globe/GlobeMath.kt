package com.ilustris.sagai.features.brain.domain.globe

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Pure camera and projection math for the globe. No UI dependencies, so it ports to iOS as is. */
object GlobeMath {
    /** Rotates (x, y, z) by [yaw] around Y then [pitch] around X. Writes into [out] (size 3). */
    fun rotate(
        x: Float,
        y: Float,
        z: Float,
        yaw: Float,
        pitch: Float,
        out: FloatArray,
    ) {
        val cy = cos(yaw)
        val sy = sin(yaw)
        val x1 = x * cy + z * sy
        val z1 = -x * sy + z * cy
        val cp = cos(pitch)
        val sp = sin(pitch)
        out[0] = x1
        out[1] = y * cp - z1 * sp
        out[2] = y * sp + z1 * cp
    }

    /** Yaw and pitch that bring the surface point (x, y, z) to the front of the globe. */
    fun focusAngles(
        x: Float,
        y: Float,
        z: Float,
    ): Pair<Float, Float> = atan2(-x, z) to atan2(y, sqrt(x * x + z * z))

    /** Shortest signed distance between two angles, in (-PI, PI]. */
    fun angleDelta(
        from: Float,
        to: Float,
    ): Float {
        val twoPi = 2f * PI.toFloat()
        var d = (to - from) % twoPi
        if (d > PI) d -= twoPi
        if (d <= -PI) d += twoPi
        return d
    }

    /**
     * Point at [t] (0..1) on the arc between a and b, lifted above the surface so connections read
     * as routes over the planet. Links that touch the core are straight lines.
     */
    fun arcPoint(
        a: FloatArray,
        b: FloatArray,
        t: Float,
        out: FloatArray,
    ) {
        val la = hypot(hypot(a[0], a[1]), a[2])
        val lb = hypot(hypot(b[0], b[1]), b[2])
        val x = a[0] + (b[0] - a[0]) * t
        val y = a[1] + (b[1] - a[1]) * t
        val z = a[2] + (b[2] - a[2]) * t
        if (la < 0.01f || lb < 0.01f) {
            out[0] = x
            out[1] = y
            out[2] = z
            return
        }
        val len = hypot(hypot(x, y), z).coerceAtLeast(1e-4f)
        val dot = ((a[0] * b[0] + a[1] * b[1] + a[2] * b[2]) / (la * lb)).coerceIn(-1f, 1f)
        val angle = acos(dot)
        val lift = 1f + (0.03f + 0.22f * angle / PI.toFloat()) * sin(PI.toFloat() * t)
        val radius = (la + (lb - la) * t) * lift / len
        out[0] = x * radius
        out[1] = y * radius
        out[2] = z * radius
    }
}
