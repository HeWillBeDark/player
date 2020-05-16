#include <jni.h>
#include <string>
#include <stdio.h>
#include <android/log.h>
#include <android/native_window_jni.h>

#define LOG_TAG "native.cpp"
#define LOGI(...)    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, ##__VA_ARGS__);
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, ##__VA_ARGS__);

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>


struct AVFrame;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_player_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    char info[10000] = {"Hello from jni"};
    sprintf(info, "%s\n", avcodec_configuration());
    LOGI("hello from jni %s", avcodec_configuration());
    return NULL;
}


extern "C"
void Java_com_example_player_VideoAct_playVideo(JNIEnv *env, jobject thiz, jstring video_path,
                                          jobject surface) {
    const char *path = env->GetStringUTFChars(video_path, 0);

    AVFormatContext *fmt_ctx = avformat_alloc_context();

    // 使用ffmpeg打开文件
    LOGI("打开文件失败：%s", path);
    int re = avformat_open_input(&fmt_ctx, path, nullptr, nullptr);
    if (re != 0) {
        LOGE("打开文件失败：%s", av_err2str(re));
        return;
    }

    //探测流索引
    re = avformat_find_stream_info(fmt_ctx, nullptr);
    if (re < 0) {
        LOGE("索引探测失败：%s", av_err2str(re));
        return;
    }

    //解码器参数
    AVCodecParameters *c_par;
    //解码器上下文
    AVCodecContext *cc_ctx;
    //声明一个解码器
    AVCodec *codec;

    //寻找视频流索引
    int v_idx = av_find_best_stream(
            fmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, &codec, 0);

    if (v_idx == -1) {
        LOGE("获取视频流索引失败");
        return;
    } else {
        LOGI("获取视频流索引成功");
    }


    c_par = fmt_ctx->streams[v_idx]->codecpar;

    //通过id查找解码器
    //codec = avcodec_find_decoder(c_par->codec_id);

    if (!codec) {

        LOGE("查找解码器失败");
        return;
    } else {
        LOGI("codec is founded, type is %s", codec->name);
    }

    //用参数c_par实例化编解码器上下文，，并打开编解码器
    cc_ctx = avcodec_alloc_context3(codec);
    // 关联解码器上下文
    re = avcodec_parameters_to_context(cc_ctx, c_par);

    if (re < 0) {
        LOGE("解码器上下文关联失败:%s", av_err2str(re));
        return;
    }

    //打开解码器
    re = avcodec_open2(cc_ctx, codec, nullptr);

    if (re != 0) {
        LOGE("打开解码器失败:%s", av_err2str(re));
        return;
    }

    //数据包
    AVPacket *pkt;
    //数据帧
    AVFrame *frame;

    //初始化
    pkt = av_packet_alloc();
    frame = av_frame_alloc();


    //初始化像素格式转换的上下文
    SwsContext *vctx = NULL;
    int outWidth = 1920;
    int outHeight = 1080;
    char *rgb = new char[outWidth * outHeight * 4];

    //显示窗口初始化
    ANativeWindow *nwin = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nwin, outWidth, outHeight, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer wbuf;

    while (av_read_frame(fmt_ctx, pkt) >= 0) {//持续读帧
        // 只解码视频流
        if (pkt->stream_index == v_idx) {

            //发送数据包到解码器
            avcodec_send_packet(cc_ctx, pkt);

            //清理
            av_packet_unref(pkt);

            //这里为什么要使用一个for循环呢？
            // 因为avcodec_send_packet和avcodec_receive_frame并不是一对一的关系的
            //一个avcodec_send_packet可能会出发多个avcodec_receive_frame
            for (;;) {
                // 接受解码的数据
                re = avcodec_receive_frame(cc_ctx, frame);
                if (re != 0) {
                    break;
                } else {

                    // 将YUV数据转换成RGB数据显示

                    vctx = sws_getCachedContext(vctx,
                                                frame->width,
                                                frame->height,
                                                (AVPixelFormat) frame->format,
                                                outWidth,
                                                outHeight,
                                                AV_PIX_FMT_RGBA,
                                                SWS_FAST_BILINEAR,
                                                0, 0, 0
                    );
                    if (!vctx) {
                        LOGE("sws_getCachedContext failed!");
                    } else {
                        uint8_t *data[AV_NUM_DATA_POINTERS] = {0};
                        data[0] = (uint8_t *) rgb;
                        int lines[AV_NUM_DATA_POINTERS] = {0};
                        lines[0] = outWidth * 4;
                        int h = sws_scale(vctx,
                                          (const uint8_t **) frame->data,
                                          frame->linesize, 0,
                                          frame->height,
                                          data, lines);
                        LOGE("sws_scale = %d", h);
                        if (h > 0) {
                            // 绘制
                            ANativeWindow_lock(nwin, &wbuf, 0);
                            uint8_t *dst = (uint8_t *) wbuf.bits;
                            memcpy(dst, rgb, outWidth * outHeight * 4);
                            ANativeWindow_unlockAndPost(nwin);
                        }
                    }

                }
            }

        }
    }
    //关闭环境
    avcodec_free_context(&cc_ctx);
    // 释放资源
    av_frame_free(&frame);
    av_packet_free(&pkt);

    avformat_free_context(fmt_ctx);

    LOGE("播放完毕");

    env->ReleaseStringUTFChars(video_path, path);
}

#ifdef __cplusplus
}
#endif



