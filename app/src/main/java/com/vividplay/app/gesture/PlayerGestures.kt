package com.vividplay.app.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

/**
 * One gesture surface to rule them all — MX-style:
 *
 *   • Single tap              -> toggle controls overlay
 *   • Double tap (L/R third)  -> skip -10s / +10s
 *   • Double tap (middle)     -> toggle play/pause
 *   • Long press              -> hold for 2x speed
 *   • Vertical drag (L third) -> brightness
 *   • Vertical drag (R third) -> volume
 *   • Horizontal drag         -> seek
 *   • Pinch                   -> aspect / zoom
 */
sealed interface PlayerGestureEvent {
    data object ToggleControls : PlayerGestureEvent
    data object TogglePlay     : PlayerGestureEvent
    data class  Seek(val deltaSeconds: Int, val committing: Boolean) : PlayerGestureEvent
    data class  Brightness(val delta: Float) : PlayerGestureEvent // -1..1 cumulative-ish
    data class  Volume(val delta: Float)     : PlayerGestureEvent
    data class  Zoom(val scaleFactor: Float) : PlayerGestureEvent
    data class  Speed(val holding: Boolean)  : PlayerGestureEvent
    data class  QuickJump(val forward: Boolean) : PlayerGestureEvent
}

fun Modifier.playerGestures(
    onEvent: (PlayerGestureEvent) -> Unit,
): Modifier = this
    .pointerInput(Unit) {
        detectTapGestures(
            onTap = { onEvent(PlayerGestureEvent.ToggleControls) },
            onDoubleTap = { offset ->
                val third = size.width / 3f
                when {
                    offset.x < third            -> onEvent(PlayerGestureEvent.QuickJump(forward = false))
                    offset.x > size.width - third -> onEvent(PlayerGestureEvent.QuickJump(forward = true))
                    else                        -> onEvent(PlayerGestureEvent.TogglePlay)
                }
            },
            onLongPress = { onEvent(PlayerGestureEvent.Speed(holding = true)) },
            onPress = {
                tryAwaitRelease()
                onEvent(PlayerGestureEvent.Speed(holding = false))
            }
        )
    }
    .pointerInput(Unit) {
        // Drag handling with a left/right-third split for brightness vs volume.
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var axisLocked: Axis? = null
            var totalDx = 0f
            var totalDy = 0f
            val startX = down.position.x
            val widthThird = size.width / 3f
            val lane: Lane = when {
                startX < widthThird -> Lane.Left
                startX > size.width - widthThird -> Lane.Right
                else -> Lane.Middle
            }
            var seekAccum = 0f
            val touchSlop = viewConfiguration.touchSlop

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change: PointerInputChange = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    if (axisLocked == Axis.Horizontal && seekAccum != 0f) {
                        val deltaSeconds = (seekAccum / size.width * 120f).toInt() // full swipe = 2 min
                        onEvent(PlayerGestureEvent.Seek(deltaSeconds, committing = true))
                    }
                    break
                }
                val d = change.positionChange()
                totalDx += d.x
                totalDy += d.y
                if (axisLocked == null && (abs(totalDx) > touchSlop || abs(totalDy) > touchSlop)) {
                    axisLocked = if (abs(totalDx) > abs(totalDy)) Axis.Horizontal else Axis.Vertical
                }
                when (axisLocked) {
                    Axis.Vertical -> {
                        // Vertical drag -> up = increase. Normalise by height.
                        val norm = -d.y / size.height.toFloat()
                        when (lane) {
                            Lane.Left   -> onEvent(PlayerGestureEvent.Brightness(norm))
                            Lane.Right  -> onEvent(PlayerGestureEvent.Volume(norm))
                            Lane.Middle -> {
                                // Middle vertical drag = pinch-free zoom
                                onEvent(PlayerGestureEvent.Zoom(1f + norm * 0.5f))
                            }
                        }
                        change.consume()
                    }
                    Axis.Horizontal -> {
                        seekAccum += d.x
                        val preview = (seekAccum / size.width * 120f).toInt()
                        onEvent(PlayerGestureEvent.Seek(preview, committing = false))
                        change.consume()
                    }
                    null -> Unit
                }
                if (axisLocked == null && !change.pressed) break
            }
        }
    }

private enum class Axis { Horizontal, Vertical }
private enum class Lane { Left, Middle, Right }

// Small extension for Offset math, kept here so gesture code stays self-contained.
@Suppress("unused")
private operator fun Offset.component1() = x
@Suppress("unused")
private operator fun Offset.component2() = y
