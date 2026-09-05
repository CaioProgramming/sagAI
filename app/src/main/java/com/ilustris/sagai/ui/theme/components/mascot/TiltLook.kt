package com.ilustris.sagai.ui.theme.components.mascot

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber

/**
 * Where the mascot looks when the phone moves.
 *
 * A touch screen has no cursor and no hover, so the device's own tilt stands in for one: the blob
 * looks the way you lean it. Unlike a drag, this costs no gesture and cannot fight a scroll.
 *
 * The neutral is deliberately not "upright" — nobody holds a phone upright, and anchoring to it
 * would leave the eyes permanently pinned to one corner. Neutral is a slow average of how the
 * device has actually been held over the last few seconds, and the look is the gap between the
 * fast reading and that slow one. So the blob answers a tilt, then drifts back to centre while you
 * keep holding the new angle: it reacts to movement, not to posture.
 *
 * Returned as [State] rather than a plain value because it changes about sixteen times a second —
 * read it inside [BlobMascot]'s draw lambda so those updates repaint without recomposing the page
 * around it.
 *
 * Null when the device has no suitable sensor, which is the value that leaves [BlobMascot] on its
 * own idle drift. Needs no permission.
 */
@Composable
fun rememberTiltLook(enabled: Boolean = true): State<Offset?> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val look = remember { mutableStateOf<Offset?>(null) }

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) {
            look.value = null
            return@DisposableEffect onDispose { }
        }

        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor =
            manager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
                ?: manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (manager == null || sensor == null) {
            Timber.tag(TAG).d("No gravity sensor — mascot stays on idle drift")
            look.value = null
            return@DisposableEffect onDispose { }
        }

        val listener = tiltListener(look)
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START ->
                        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

                    Lifecycle.Event.ON_STOP -> manager.unregisterListener(listener)
                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            manager.unregisterListener(listener)
        }
    }

    return look
}

private fun tiltListener(look: MutableState<Offset?>) =
    object : SensorEventListener {
        private var fastX = 0f
        private var fastY = 0f
        private var restX = 0f
        private var restY = 0f
        private var seeded = false

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]

            if (!seeded) {
                fastX = x
                fastY = y
                restX = x
                restY = y
                seeded = true
            }

            fastX += (x - fastX) * FAST_ALPHA
            fastY += (y - fastY) * FAST_ALPHA
            restX += (x - restX) * REST_ALPHA
            restY += (y - restY) * REST_ALPHA

            look.value =
                Offset(
                    // Roll right and gravity gains +x, so the eyes go the way the phone leans.
                    x = ((fastX - restX) / TILT_RANGE).coerceIn(-1f, 1f),
                    // Lean the top away and screen-up gravity drops, so the eyes go down with it.
                    y = (-(fastY - restY) / TILT_RANGE).coerceIn(-1f, 1f),
                )
        }

        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int,
        ) = Unit
    }

private const val TAG = "TiltLook"

/** How much of a new reading each sample takes — the eyes' own reaction time. */
private const val FAST_ALPHA = 0.35f

/** How fast "neutral" catches up to how the phone is being held. ~5s at SENSOR_DELAY_UI. */
private const val REST_ALPHA = 0.012f

/** Gravity delta, in m/s², that counts as looking all the way over. Roughly 18 degrees. */
private const val TILT_RANGE = 3f
