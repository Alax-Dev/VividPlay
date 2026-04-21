package com.vividplay.app.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.vividplay.app.data.MediaItem
import com.vividplay.app.data.MediaRepository
import com.vividplay.app.ui.theme.VividPlayTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            VividPlayTheme {
                Home(onOpen = { uri, title -> openInPlayer(uri, title) })
            }
        }
    }

    private fun openInPlayer(uri: Uri, title: String?) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            data = uri
            if (title != null) putExtra("title", title)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }
}

@Composable
private fun Home(onOpen: (Uri, String?) -> Unit) {
    val context = LocalContext.current
    val repo = remember { MediaRepository(context) }
    var tab by remember { mutableStateOf(Tab.Video) }
    var videos by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var audio by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var permissionReady by remember { mutableStateOf(false) }

    val openDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onOpen(uri, uri.lastPathSegment)
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionReady = true }

    LaunchedEffect(Unit) {
        val perms = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permLauncher.launch(perms)
    }

    LaunchedEffect(permissionReady) {
        if (permissionReady) withContext(Dispatchers.IO) {
            videos = repo.loadVideos()
            audio  = repo.loadAudio()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Header(onPickFile = { openDoc.launch(arrayOf("video/*", "audio/*", "*/*")) })

            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tab == Tab.Video,
                    onClick = { tab = Tab.Video },
                    label = { Text("Videos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
                FilterChip(
                    selected = tab == Tab.Audio,
                    onClick = { tab = Tab.Audio },
                    label = { Text("Audio") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }

            val data = if (tab == Tab.Video) videos else audio
            if (data.isEmpty()) {
                EmptyState(tab)
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp, vertical = 8.dp
                    )
                ) {
                    items(data, key = { it.id }) { item ->
                        MediaRow(item) { onOpen(item.uri, item.title) }
                    }
                }
            }
        }
    }
}

private enum class Tab { Video, Audio }

@Composable
private fun Header(onPickFile: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    "VividPlay",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Every format. Every gesture. Thoughtfully crafted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onPickFile,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) { Icon(Icons.Default.FolderOpen, "Pick file", tint = MaterialTheme.colorScheme.onPrimary) }
        }
    }
    Spacer(Modifier.size(4.dp))
}

@Composable
private fun MediaRow(item: MediaItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.isVideo) Icons.Default.PlayArrow else Icons.Default.Audiotrack,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    buildString {
                        append(formatDuration(item.durationMs))
                        append("  •  ")
                        append(formatSize(item.sizeBytes))
                        if (!item.mimeType.isNullOrBlank()) {
                            append("  •  "); append(item.mimeType.substringAfter('/').uppercase())
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(tab: Tab) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (tab == Tab.Video) "No videos found yet." else "No audio found yet.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            "Grant media access or tap the folder icon to open any file.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatDuration(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600; val m = (s % 3600) / 60; val ss = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, ss) else "%d:%02d".format(m, ss)
}

private fun formatSize(bytes: Long): String {
    val kb = 1024.0
    return when {
        bytes < kb         -> "$bytes B"
        bytes < kb * kb    -> "%.1f KB".format(bytes / kb)
        bytes < kb * kb * kb -> "%.1f MB".format(bytes / (kb * kb))
        else               -> "%.2f GB".format(bytes / (kb * kb * kb))
    }
}
