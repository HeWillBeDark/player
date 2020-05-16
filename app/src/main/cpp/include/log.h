//
// Created by 林坤 on 2020/5/3.
//

#ifndef PLAYER_LOG_H
#define PLAYER_LOG_H

#endif //PLAYER_LOG_H

#include <android/log.h>

#define LOG_TAG "native.cpp"
#define LOGI(...)    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, ##__VA_ARGS__);
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, ##__VA_ARGS__);
