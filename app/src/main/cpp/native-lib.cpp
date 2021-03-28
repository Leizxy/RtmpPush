#include <jni.h>
#include <string>
#include <android/log.h>

#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,"rtmp-jni",__VA_ARGS__)

extern "C" {
#include "librtmp/rtmp.h"
}
typedef struct {
    RTMP *rtmp;
    int16_t sps_len;
    int8_t *sps;
    int16_t pps_len;
    int8_t *pps;
} Live;
Live *live = NULL;

extern "C" JNIEXPORT jstring JNICALL
Java_cn_leizy_rtmpdemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_leizy_rtmpdemo_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);
    int ret;
    do {
        live = static_cast<Live *>(malloc(sizeof(Live)));
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 100;
        LOGW("connect %s", url);
        if (!(ret = RTMP_SetupURL(live->rtmp, (char *) url))) break;
        RTMP_EnableWrite(live->rtmp);
        LOGW("RTMP_Connect");
        if (!(ret = RTMP_Connect(live->rtmp, 0))) break;
        LOGW("RTMP_ConnectStream");
        if (!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;
        LOGW("connect success");
    } while (0);
    if (!ret && live) {
        free(live);
        live = nullptr;
    }
    env->ReleaseStringUTFChars(url_, url);
    return ret;
}extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_leizy_rtmpdemo_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                           jlong tms) {

    return false;
}