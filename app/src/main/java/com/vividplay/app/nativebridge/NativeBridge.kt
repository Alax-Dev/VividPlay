package com.vividplay.app.nativebridge

/**
 * Kotlin side of the JNI bridge into libvividplay_native.so.
 *
 * Keep this surface narrow — most media work lives in Media3 on the JVM side;
 * native is reserved for hot, CPU-bound helpers.
 */
object NativeBridge {
    init { System.loadLibrary("vividplay_native") }

    external fun nativeVersion(): String
    external fun fastHash(bytes: ByteArray): Long
    external fun audioRms(pcm: ShortArray): Float

    fun version(): String = runCatching { nativeVersion() }.getOrDefault("native unavailable")
}
