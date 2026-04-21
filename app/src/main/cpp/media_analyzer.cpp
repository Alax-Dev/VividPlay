#include "media_analyzer.h"
#include <cmath>

namespace MediaAnalyzer {

    std::string version() {
        return "1.0.0";
    }

    // FNV-1a 64-bit. Useful to build stable thumbnail cache keys.
    uint64_t fnv1a(const uint8_t* data, size_t len) {
        constexpr uint64_t kOffset = 0xcbf29ce484222325ULL;
        constexpr uint64_t kPrime  = 0x00000100000001B3ULL;
        uint64_t h = kOffset;
        for (size_t i = 0; i < len; ++i) {
            h ^= data[i];
            h *= kPrime;
        }
        return h;
    }

    // RMS level of a signed 16-bit PCM buffer, returned in 0..1 range.
    float rms16(const int16_t* pcm, size_t sampleCount) {
        if (sampleCount == 0) return 0.0f;
        double acc = 0.0;
        for (size_t i = 0; i < sampleCount; ++i) {
            double s = static_cast<double>(pcm[i]) / 32768.0;
            acc += s * s;
        }
        return static_cast<float>(std::sqrt(acc / static_cast<double>(sampleCount)));
    }

}
