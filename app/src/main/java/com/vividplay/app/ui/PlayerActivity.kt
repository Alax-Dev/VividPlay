package com.vividplay.app.ui

import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.vividplay.app.ui.player.PlayerScreen
import com.vividplay.app.ui.theme.VividPlayTheme

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val data = intent?.data
        val title = intent?.getStringExtra("title") ?: intent?.data?.lastPathSegment

        if (data == null) { finish(); return }

        setContent {
            VividPlayTheme(darkTheme = true) {
                PlayerScreen(
                    uri = data,
                    title = title,
                    onExit = { finish() },
                    onPipRequest = { enterPip() }
                )
            }
        }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }
}
