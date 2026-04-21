package com.vividplay.app

import android.app.Application
import com.vividplay.app.nativebridge.NativeBridge

class VividPlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Touch the native lib early so any loader errors surface quickly.
        runCatching { NativeBridge.version() }
    }
}
