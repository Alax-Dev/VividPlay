// VividPlay native bridge.
// Exposes fast helpers to Kotlin over JNI. Keeping the native side intentionally
// small and self-contained — heavy lifting stays in Media3 on the Java side.

#include <jni.h>
#include <string>
#include <android/log.h>
#include "media_analyzer.h"

#define LOG_TAG "VividPlayNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_vividplay_app_nativebridge_NativeBridge_nativeVersion(JNIEnv* env, jobject /* thiz */) {
    std::string v = std::string("VividPlay native ") + MediaAnalyzer::version();
    LOGI("nativeVersion() -> %s", v.c_str());
    return env->NewStringUTF(v.c_str());
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_vividplay_app_nativebridge_NativeBridge_fastHash(
        JNIEnv* env, jobject /* thiz */, jbyteArray bytes) {
    jsize len = env->GetArrayLength(bytes);
    jbyte* data = env->GetByteArrayElements(bytes, nullptr);
    uint64_t h = MediaAnalyzer::fnv1a(reinterpret_cast<const uint8_t*>(data),
                                      static_cast<size_t>(len));
    env->ReleaseByteArrayElements(bytes, data, JNI_ABORT);
    return static_cast<jlong>(h);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_vividplay_app_nativebridge_NativeBridge_audioRms(
        JNIEnv* env, jobject /* thiz */, jshortArray pcm) {
    jsize len = env->GetArrayLength(pcm);
    jshort* data = env->GetShortArrayElements(pcm, nullptr);
    float rms = MediaAnalyzer::rms16(data, static_cast<size_t>(len));
    env->ReleaseShortArrayElements(pcm, data, JNI_ABORT);
    return rms;
}
