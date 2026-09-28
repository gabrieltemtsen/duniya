cmake_minimum_required(VERSION 3.22.1)

project("duniya_engine" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 16KB page alignment for Android 15/16+ compatibility and modern GrapheneOS Pixel hardware
add_link_options("-Wl,-z,max-page-size=16384")

add_library(
    duniya_engine
    SHARED
    duniya_engine.cpp
)

target_compile_options(
    duniya_engine
    PRIVATE
    -O3
    -ffast-math
    -Wall
    -Wextra
    -Wno-unused-parameter
)

find_library(log-lib log)

target_link_libraries(
    duniya_engine
    ${log-lib}
)
