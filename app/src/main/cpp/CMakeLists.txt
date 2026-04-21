cmake_minimum_required(VERSION 3.22.1)
project("vividplay")

add_library(vividplay_native SHARED
    native_lib.cpp
    media_analyzer.cpp
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(vividplay_native
    ${log-lib}
    ${android-lib}
)

target_compile_options(vividplay_native PRIVATE
    -Wall -Wextra -O2 -ffast-math
)
