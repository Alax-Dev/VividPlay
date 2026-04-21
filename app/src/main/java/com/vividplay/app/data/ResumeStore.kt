package com.vividplay.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.resumeDataStore by preferencesDataStore("vividplay_resume")

class ResumeStore(private val context: Context) {
    private fun keyFor(uri: String) = longPreferencesKey("pos_" + uri.hashCode().toString())

    suspend fun position(uri: String): Long =
        context.resumeDataStore.data.map { it[keyFor(uri)] ?: 0L }.first()

    suspend fun save(uri: String, positionMs: Long) {
        context.resumeDataStore.edit { it[keyFor(uri)] = positionMs }
    }
}
