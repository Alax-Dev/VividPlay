package com.vividplay.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    val relativePath: String?,
    val isVideo: Boolean,
)

/**
 * Thin wrapper over MediaStore. Returns both videos and audio so the library
 * screen can tabbed-browse either.
 */
class MediaRepository(private val context: Context) {

    suspend fun loadVideos(): List<MediaItem> = withContext(Dispatchers.IO) {
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.RELATIVE_PATH,
        )
        val sort = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val results = mutableListOf<MediaItem>()
        context.contentResolver.query(uri, projection, null, null, sort)?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nC  = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dC  = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sC  = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mC  = c.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val rC  = c.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                results += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(uri, id),
                    title = c.getString(nC) ?: "Untitled",
                    durationMs = c.getLong(dC),
                    sizeBytes = c.getLong(sC),
                    mimeType = c.getString(mC),
                    relativePath = c.getString(rC),
                    isVideo = true,
                )
            }
        }
        results
    }

    suspend fun loadAudio(): List<MediaItem> = withContext(Dispatchers.IO) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.RELATIVE_PATH,
        )
        val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val results = mutableListOf<MediaItem>()
        context.contentResolver.query(uri, projection, "${MediaStore.Audio.Media.IS_MUSIC}=1", null, sort)?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nC  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dC  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sC  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mC  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val rC  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                results += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(uri, id),
                    title = c.getString(nC) ?: "Untitled",
                    durationMs = c.getLong(dC),
                    sizeBytes = c.getLong(sC),
                    mimeType = c.getString(mC),
                    relativePath = c.getString(rC),
                    isVideo = false,
                )
            }
        }
        results
    }
}
