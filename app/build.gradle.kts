plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
}

android {
 namespace = "com.vividplay.app"
 compileSdk = 34

 defaultConfig {
  applicationId = "com.vividplay.app"
  minSdk = 24
  targetSdk = 34
  versionCode = 1
  versionName = "1.0.0"

  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  vectorDrawables { useSupportLibrary = true }

  externalNativeBuild {
   cmake {
    cppFlags += listOf("-std=c++17", "-O2")
   }
  }
  ndk {
   abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
  }
 }

 signingConfigs {
  create("debugKey") {
   storeFile = file("../debug.keystore")
   storePassword = "android"
   keyAlias = "androiddebugkey"
   keyPassword = "android"
  }
 }

 buildTypes {
  release {
   isMinifyEnabled = true
   isShrinkResources = true
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
   signingConfig = signingConfigs.getByName("debugKey")
  }
  debug {
   isMinifyEnabled = false
  }
 }

 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
 kotlinOptions {
  jvmTarget = "17"
 }
 buildFeatures {
  compose = true
  buildConfig = true
 }
 packaging {
  resources {
   excludes += "/META-INF/{AL2.0,LGPL2.1}"
  }
 }
 externalNativeBuild {
  cmake {
   path = file("src/main/cpp/CMakeLists.txt")
   version = "3.22.1"
  }
 }
}

dependencies {
 val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
 implementation(composeBom)
 androidTestImplementation(composeBom)

 implementation("androidx.core:core-ktx:1.13.1")
 implementation("androidx.activity:activity-compose:1.9.2")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
 implementation("androidx.navigation:navigation-compose:2.8.2")

 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-graphics")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")

 // Media3 ExoPlayer - broad format support (MP4, MKV, WebM, AVI*, FLV*, HLS, DASH, SmoothStreaming,
 // MP3, AAC, FLAC, OGG, Opus, Vorbis, WAV, ALAC, AMR, Midi)
 val media3 = "1.4.1"
 implementation("androidx.media3:media3-exoplayer:$media3")
 implementation("androidx.media3:media3-exoplayer-hls:$media3")
 implementation("androidx.media3:media3-exoplayer-dash:$media3")
 implementation("androidx.media3:media3-exoplayer-smoothstreaming:$media3")
 implementation("androidx.media3:media3-exoplayer-rtsp:$media3")
 implementation("androidx.media3:media3-ui:$media3")
 implementation("androidx.media3:media3-session:$media3")
 implementation("androidx.media3:media3-datasource:$media3")
 implementation("androidx.media3:media3-common:$media3")

 implementation("com.google.accompanist:accompanist-permissions:0.34.0")
 implementation("androidx.documentfile:documentfile:1.0.1")
 implementation("io.coil-kt:coil-compose:2.7.0")
 implementation("io.coil-kt:coil-video:2.7.0")

 implementation("androidx.datastore:datastore-preferences:1.1.1")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

 testImplementation("junit:junit:4.13.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
