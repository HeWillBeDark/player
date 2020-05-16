# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )
#find_library(libcutils                  cutils)
#find_library(libutils                   utils)
#find_library(libbinder                  binder)
#find_library(libui                      ui)
#find_library(libgui                     gui)
#find_library(libandroid_runtime         android_runtime)
#find_library(libstagefright_foundation  stagefright_foundation)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#target_link_libraries( # Specifies the target library.
#                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
#                       ${log-lib} )

# 设置路径
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../libs)

add_library( avutil SHARED IMPORTED )
set_target_properties( avutil
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libavutil.so )

add_library( swresample
        SHARED
        IMPORTED )
set_target_properties( swresample
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libswresample.so )

add_library( avcodec
        SHARED
        IMPORTED )
set_target_properties( avcodec
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libavcodec.so )

add_library( avfilter
        SHARED
        IMPORTED)
set_target_properties( avfilter
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libavfilter.so )

add_library( swscale
        SHARED
        IMPORTED)
set_target_properties( swscale
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libswscale.so )

add_library( avformat
        SHARED
        IMPORTED)
set_target_properties( avformat
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libavformat.so )


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(include)

#target_include_directories(native-lib PRIVATE libs/include)


add_library(native-lib

        SHARED

        native-lib.cpp)

target_link_libraries( native-lib swresample avcodec avfilter swscale  avformat avutil android

        ${log-lib} )