#pragma once
#include <cstdint>
#include <cstddef>
#include <string>

namespace MediaAnalyzer {
    std::string version();
    uint64_t fnv1a(const uint8_t* data, size_t len);
    float rms16(const int16_t* pcm, size_t sampleCount);
}
