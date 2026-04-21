# VividPlay

A modern, gesture-first Android video & audio player inspired by MX Player, with a calm, editorial
UI foundation borrowed from Claude's design language (warm parchment, clay accents, ink typography).

## ✨ Highlights

- **Broad format support** — MP4, MKV, WebM, MOV, AVI, FLV, 3GP, TS/M2TS, HLS, DASH, SmoothStreaming, RTSP,
  MP3, FLAC, OGG, Opus, AAC, M4A, WAV, AMR, MIDI and more via Media3 ExoPlayer.
- **MX-style gestures**
  - Left-side vertical drag → Brightness
  - Right-side vertical drag → Volume
  - Horizontal drag → Scrub with live preview overlay
  - Double-tap left/right → −10s / +10s
  - Double-tap center → Play/Pause
  - Long-press → Hold for 2× speed
  - Pinch / middle-vertical → Cycle Fit / Fill / Zoom
  - Single tap → Toggle controls
- **System integration** — Declares itself as an Android video player through `video/*`, `audio/*`
  and dozens of explicit extensions + `VIEW`, `SEND`, `file://`, `content://`, `http(s)://`, `rtsp://`,
  so "Open with…" surfaces VividPlay for virtually any playable file.
- **Picture-in-Picture**, resume playback, background audio service (Media3 session),
  subtitle chooser hook, 0.5× → 2× speed cycle, aspect-ratio toggle, lock screen.
- **Claude-inspired UI** — parchment cream in light, ink charcoal in dark, clay accent throughout.
  Jetpack Compose, Material 3.
- **Kotlin + Jetpack Compose + C++/NDK** — a small native library (`libvividplay_native.so`)
  exposes a fast FNV-1a hash and 16-bit PCM RMS helper to Kotlin over JNI.

## 🧱 Stack

- Kotlin 1.9 · Jetpack Compose (Material 3) · Navigation Compose
- AndroidX Media3 1.4.1 (ExoPlayer, HLS, DASH, SmoothStreaming, RTSP, Session, UI)
- C++17 via CMake & NDK (arm64-v8a, armeabi-v7a, x86_64)
- DataStore for resume positions
- Coil for thumbnails

## 🚀 Build

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## 🤖 CI

`.github/workflows/android.yml` runs on pushes, PRs, **and `workflow_dispatch`** with inputs:

- `build_type` — `debug` or `release`
- `upload_artifact` — whether to upload the APK artifact

Trigger manually from the Actions tab → *Android CI* → *Run workflow*.

## 📱 Minimum Requirements

- Android 7.0 (API 24)
- Target Android 14 (API 34)

## 🗂️ Structure

```
app/
├── src/main/java/com/vividplay/app/
│   ├── ui/              # Compose screens, theme, activities
│   ├── player/          # Media3 playback service
│   ├── gesture/         # MX-style gesture handling
│   ├── data/            # MediaStore repository, resume store
│   └── nativebridge/    # JNI bridge
├── src/main/cpp/        # Native C++ helpers
└── src/main/res/        # Resources (themes, icons, XML)
```

## 📝 License

MIT — see [LICENSE](LICENSE).
