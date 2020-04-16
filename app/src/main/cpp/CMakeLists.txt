cmake_minimum_required (VERSION 3.10.2)
project(wolve)

enable_language(CXX)
enable_language(ASM)

set(SOURCE_PATH ${CMAKE_CURRENT_SOURCE_DIR})

add_library(${PROJECT_NAME}
        SHARED
        )

target_include_directories(${PROJECT_NAME}
        PRIVATE
        ${SOURCE_PATH}
        )

target_sources(${PROJECT_NAME}
        PRIVATE

        main.cpp
        Patching/Patch.cpp
        Substrate/hde64.c
        Substrate/SubstrateDebug.cpp
        Substrate/SubstrateHook.cpp
        Substrate/SubstratePosixMemory.cpp
        X64Hook/And64InlineHook.cpp
        )

set_target_properties(${PROJECT_NAME}
        PROPERTIES

        CXX_STANDARD 17
        CXX_STANDARD_REQUIRED YES
        CXX_EXTENSIONS YES
        )

target_compile_features(${PROJECT_NAME}
        INTERFACE

        cxx_std_17
        )

message(STATUS "CMAKE_BUILD_TYPE is ${CMAKE_BUILD_TYPE}")
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(BUILD_TYPE_FLAGS
            -g3
            )
else()
    set(BUILD_TYPE_FLAGS
            -fvisibility=hidden
            -fvisibility-inlines-hidden
            -fomit-frame-pointer
            -ffunction-sections
            -fdata-sections
            -g0
            -Ofast
            -DNDEBUG
            )
endif()
target_compile_options(${PROJECT_NAME}
        INTERFACE

        -Wno-error=format-security
        -fpermissive
        -fno-rtti
        -fno-exceptions
        ${BUILD_TYPE_FLAGS}
        )


set_property(TARGET ${PROJECT_NAME}
        APPEND

        PROPERTY LINK_FLAGS

        -Wl
        --strip-all
        )
# Requires cmake >= 3.13
#target_link_options(${PROJECT_NAME}
#    INTERFACE
#
#    -Wl
#    --strip-all
#)


find_library(
        log-lib

        log
        )

target_link_libraries(${PROJECT_NAME}
        ${log-lib}
        )