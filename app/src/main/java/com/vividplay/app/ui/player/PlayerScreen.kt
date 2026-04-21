package com.vividplay.app.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.vividplay.app.data.ResumeStore
import com.vividplay.app.gesture.PlayerGestureEvent
import com.vividplay.app.gesture.playerGestures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    uri: Uri,
    title: String?,
    onExit: () -> Unit,
    onPipRequest: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val audioMgr = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val resumeStore = remember { ResumeStore(context) }
    val scope = rememberCoroutineScope()

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = true
            prepare()
        }
    }

    // Restore saved position.
    LaunchedEffect(uri) {
        val saved = resumeStore.position(uri.toString())
        if (saved > 1_000) player.seekTo(saved)
    }

    // Periodically persist playback position.
    LaunchedEffect(player) {
        while (true) {
            delay(5_000)
            if (player.currentPosition > 0) {
                resumeStore.save(uri.toString(), player.currentPosition)
            }
        }
    }

    DisposableEffect(Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            scope.launch { resumeStore.save(uri.toString(), player.currentPosition) }
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            player.release()
        }
    }

    // --- UI state ---------------------------------------------------------
    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var speed by remember { mutableFloatStateOf(1f) }
    var resizeModeIdx by remember { mutableStateOf(0) } // 0=fit 1=fill 2=zoom
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var seekPreviewDeltaSec by remember { mutableStateOf<Int?>(null) }
    var brightness by remember {
        mutableFloatStateOf(
            activity.window.attributes.screenBrightness.takeIf { it in 0f..1f } ?: 0.5f
        )
    }
    var volume by remember {
        mutableFloatStateOf(
            audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        )
    }

    // Track player state.
    LaunchedEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) duration = player.duration.coerceAtLeast(0)
            }
        }
        player.addListener(listener)
        while (true) {
            position = player.currentPosition
            if (duration == 0L) duration = player.duration.coerceAtLeast(0).let { if (it == C.TIME_UNSET) 0 else it }
            delay(250)
        }
    }

    // Auto-hide controls after inactivity.
    LaunchedEffect(controlsVisible, locked) {
        if (controlsVisible && !locked) {
            delay(4_000)
            controlsVisible = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .playerGestures { event ->
                if (locked && event !is PlayerGestureEvent.ToggleControls) return@playerGestures
                when (event) {
                    PlayerGestureEvent.ToggleControls -> controlsVisible = !controlsVisible
                    PlayerGestureEvent.TogglePlay -> {
                        if (player.isPlaying) player.pause() else player.play()
                    }
                    is PlayerGestureEvent.QuickJump -> {
                        val step = if (event.forward) 10_000 else -10_000
                        player.seekTo((player.currentPosition + step).coerceAtLeast(0))
                        overlay = Overlay.Jump(event.forward)
                    }
                    is PlayerGestureEvent.Seek -> {
                        seekPreviewDeltaSec = event.deltaSeconds
                        if (event.committing) {
                            val target = (player.currentPosition + event.deltaSeconds * 1000L)
                                .coerceIn(0, duration.coerceAtLeast(1))
                            player.seekTo(target)
                            seekPreviewDeltaSec = null
                        }
                    }
                    is PlayerGestureEvent.Brightness -> {
                        brightness = (brightness + event.delta).coerceIn(0.01f, 1f)
                        val lp = activity.window.attributes
                        lp.screenBrightness = brightness
                        activity.window.attributes = lp
                        overlay = Overlay.Brightness(brightness)
                    }
                    is PlayerGestureEvent.Volume -> {
                        volume = (volume + event.delta).coerceIn(0f, 1f)
                        val max = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, (volume * max).roundToInt(), 0)
                        overlay = Overlay.Volume(volume)
                    }
                    is PlayerGestureEvent.Zoom -> {
                        // Cycle resize modes when a strong zoom gesture is seen.
                        if (event.scaleFactor > 1.15f) resizeModeIdx = (resizeModeIdx + 1) % 3
                        else if (event.scaleFactor < 0.85f) resizeModeIdx = (resizeModeIdx + 2) % 3
                        overlay = Overlay.Info(resizeLabel(resizeModeIdx))
                    }
                    is PlayerGestureEvent.Speed -> {
                        val target = if (event.holding) 2f else 1f
                        speed = target
                        player.setPlaybackSpeed(target)
                        overlay = if (event.holding) Overlay.Info("${target}x") else null
                    }
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                }
            },
            update = { view ->
                view.resizeMode = when (resizeModeIdx) {
                    0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            }
        )

        // HUD overlays (brightness/volume/jump/seek).
        FloatingHud(
            overlay = overlay,
            seekDeltaSec = seekPreviewDeltaSec,
            positionMs = position,
            durationMs = duration,
            modifier = Modifier.align(Alignment.Center)
        )

        // Top + bottom control chrome.
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
        ) {
            TopBar(
                title = title ?: uri.lastPathSegment.orEmpty(),
                locked = locked,
                onLock = { locked = !locked },
                onClose = onExit,
                onPip = onPipRequest,
                onSubtitle = { /* Subtitle chooser hook — wire up to TrackSelector if needed. */ },
                onAspect = { resizeModeIdx = (resizeModeIdx + 1) % 3; overlay = Overlay.Info(resizeLabel(resizeModeIdx)) },
                onSpeed = {
                    val cycle = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
                    val next = cycle[(cycle.indexOf(speed).coerceAtLeast(0) + 1) % cycle.size]
                    speed = next
                    player.setPlaybackSpeed(next)
                    overlay = Overlay.Info("${next}x")
                }
            )
        }

        AnimatedVisibility(
            visible = controlsVisible && !locked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            BottomBar(
                positionMs = position,
                durationMs = duration,
                isPlaying = isPlaying,
                onSeek = { player.seekTo(it); position = it },
                onPlayPause = { if (player.isPlaying) player.pause() else player.play() },
                onBack10 = { player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0)) },
                onFwd10 = { player.seekTo(player.currentPosition + 10_000) },
            )
        }

        // Lock shade — thin pill at edge to unlock.
        AnimatedVisibility(
            visible = locked && controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(12.dp)
        ) {
            IconButton(
                onClick = { locked = false },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) { Icon(Icons.Default.Lock, null, tint = Color.White) }
        }
    }
}

private fun resizeLabel(idx: Int): String = when (idx) { 0 -> "Fit"; 1 -> "Fill"; else -> "Zoom" }

@Composable
private fun TopBar(
    title: String,
    locked: Boolean,
    onLock: () -> Unit,
    onClose: () -> Unit,
    onPip: () -> Unit,
    onSubtitle: () -> Unit,
    onAspect: () -> Unit,
    onSpeed: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            maxLines = 1,
        )
        IconButton(onClick = onSubtitle) { Icon(Icons.Default.Subtitles, null, tint = Color.White) }
        IconButton(onClick = onAspect)   { Icon(Icons.Default.AspectRatio, null, tint = Color.White) }
        IconButton(onClick = onSpeed)    { Icon(Icons.Default.Speed, null, tint = Color.White) }
        IconButton(onClick = onPip)      { Icon(Icons.Default.PictureInPicture, null, tint = Color.White) }
        IconButton(onClick = onLock)     { Icon(Icons.Default.Lock, null, tint = if (locked) Color(0xFFD97757) else Color.White) }
    }
}

@Composable
private fun BottomBar(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onBack10: () -> Unit,
    onFwd10: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val maxVal = durationMs.coerceAtLeast(1L).toFloat()
        Slider(
            value = positionMs.coerceIn(0L, durationMs).toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..maxVal,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(formatTime(positionMs), color = Color.White)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onBack10) { Icon(Icons.Default.Replay10, null, tint = Color.White) }
            IconButton(onClick = onPlayPause, modifier = Modifier.size(56.dp)) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null, tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = onFwd10) { Icon(Icons.Default.Forward10, null, tint = Color.White) }
            Spacer(Modifier.weight(1f))
            Text(formatTime(durationMs), color = Color.White)
        }
    }
}

private sealed interface Overlay {
    data class Brightness(val value: Float) : Overlay
    data class Volume(val value: Float) : Overlay
    data class Jump(val forward: Boolean) : Overlay
    data class Info(val text: String) : Overlay
}

@Composable
private fun FloatingHud(
    overlay: Overlay?,
    seekDeltaSec: Int?,
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when {
            seekDeltaSec != null -> {
                val sign = if (seekDeltaSec >= 0) "+" else ""
                val target = (positionMs + seekDeltaSec * 1000L).coerceIn(0, durationMs)
                HudBox {
                    Text("${sign}${seekDeltaSec}s", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("${formatTime(target)} / ${formatTime(durationMs)}", color = Color.White.copy(alpha = 0.8f))
                }
            }
            overlay is Overlay.Brightness -> HudBox {
                Text("Brightness ${(overlay.value * 100).toInt()}%", color = Color.White)
                LinearProgressIndicator(progress = { overlay.value }, modifier = Modifier.fillMaxWidth())
            }
            overlay is Overlay.Volume -> HudBox {
                Text("Volume ${(overlay.value * 100).toInt()}%", color = Color.White)
                LinearProgressIndicator(progress = { overlay.value }, modifier = Modifier.fillMaxWidth())
            }
            overlay is Overlay.Jump -> HudBox {
                Text(if (overlay.forward) "+10s ⏩" else "⏪ -10s", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            overlay is Overlay.Info -> HudBox { Text(overlay.text, color = Color.White, fontWeight = FontWeight.Medium) }
            else -> Unit
        }
    }
}

@Composable
private fun HudBox(content: @Composable () -> Unit) {
    Column(
        Modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
