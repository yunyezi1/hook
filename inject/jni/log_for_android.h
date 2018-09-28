#ifndef _INCLUDE_LOG_FOR_ANDROID_H_
#define _INCLUDE_LOG_FOR_ANDROID_H_
#ifdef __cplusplus
extern "C" {
#endif

#ifdef DEBUG
#define LOGV_(TAG,...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD_(TAG,...) __android_log_print(ANDROID_LOG_DEBUG , TAG, __VA_ARGS__)
//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , TAG, __VA_ARGS__)
#define LOGI_(TAG,...) __android_log_print(ANDROID_LOG_ERROR  , TAG, __VA_ARGS__)
#define LOGW_(TAG,...) __android_log_print(ANDROID_LOG_WARN  , TAG, __VA_ARGS__)
#define LOGE_(TAG,...) __android_log_print(ANDROID_LOG_ERROR  , TAG, __VA_ARGS__)
#else
#define LOGV_(TAG,...)
#define LOGD_(TAG,...)
#define LOGI_(TAG,...)
#define LOGW_(TAG,...)
#define LOGE_(TAG,...)
#endif
#define LOGI2(TAG,...) __android_log_print(ANDROID_LOG_INFO  , TAG, __VA_ARGS__)

#ifdef __cplusplus
}
#endif
#endif
