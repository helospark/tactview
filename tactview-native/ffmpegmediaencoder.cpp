#include <map>
#include <iostream>
extern "C" {
    #include <math.h>
    #include <libavutil/opt.h>
    #include <libavcodec/avcodec.h>
    #include <libavutil/channel_layout.h>
    #include <libavutil/common.h>
    #include <libavutil/imgutils.h>
    #include <libavutil/mathematics.h>
    #include <libavutil/samplefmt.h>
    #include <libswscale/swscale.h>

    #define INBUF_SIZE 4096
    #define AUDIO_INBUF_SIZE 20480
    #define AUDIO_REFILL_THRESH 4096

    struct RenderVideoContext {
        public:
        AVCodec *codec;
        AVCodecContext* c;
        AVFrame* frame;
        int width, height;
        FILE *f;
    };

    struct FFmpegInitEncoderRequest {
        const char* fileName;
        int width;
        int height;
        double framerate;
    };

    struct FFMpegFrame {
        unsigned char* data;
    };

    struct FFmpegEncodeFrameRequest {
        int encoderIndex;
        int startFrameIndex;
        FFMpegFrame* frames;
    };

    struct FFmpegClearEncoderRequest {
        int encoderIndex;
    };

    RenderVideoContext* renderContext = NULL;

    int initEncoder(FFmpegInitEncoderRequest* request) {
        avcodec_register_all();
        AVCodec *codec;
        AVCodecContext *c= NULL;
        int i, ret, x, y, got_output;
        AVFrame *frame;
        printf("Encode video file %s\n", request->fileName);
        /* find the mpeg1 video encoder */
        codec = avcodec_find_encoder(AV_CODEC_ID_MPEG2VIDEO);
        if (!codec) {
            fprintf(stderr, "Codec not found\n");
            exit(1);
        }
        c = avcodec_alloc_context3(codec);
        if (!c) {
            fprintf(stderr, "Could not allocate video codec context\n");
            exit(1);
        }
        /* put sample parameters */
        c->bit_rate = 400000;
        /* resolution must be a multiple of two */
        c->width = request->width;
        c->height = request->height;
        /* frames per second */
        c->time_base = av_d2q(request->framerate, 5);
        c->gop_size = 10; /* emit one intra frame every ten frames */
        c->max_b_frames = 1;
        c->pix_fmt = AV_PIX_FMT_YUV420P;
        /* open it */
        if (avcodec_open2(c, codec, NULL) < 0) {
            fprintf(stderr, "Could not open codec\n");
            exit(1);
        }
        FILE* f = fopen(request->fileName, "wb");
        if (!f) {
            fprintf(stderr, "Could not open %s\n", request->fileName);
            exit(1);
        }
        frame = av_frame_alloc();
        if (!frame) {
            fprintf(stderr, "Could not allocate video frame\n");
            exit(1);
        }
        frame->format = c->pix_fmt;
        frame->width  = c->width;
        frame->height = c->height;
        /* the image can be allocated by any means and av_image_alloc() is
         * just the most convenient way if av_malloc() is to be used */
        ret = av_image_alloc(frame->data, frame->linesize, c->width, c->height,
                             c->pix_fmt, 32);
        if (ret < 0) {
            fprintf(stderr, "Could not allocate raw picture buffer\n");
            exit(1);
        }

        std::cout << "f1" << frame->data[0][0]  << std::endl;

        RenderVideoContext* asd = new RenderVideoContext;
        asd->c = c;
        asd->frame = frame;
        asd->f = f;
        asd->width = c->width;
        asd->height = c->height;
        asd->codec = codec;

        renderContext = asd;
        std::cout << "f1" << renderContext->c << " " << renderContext->codec  << std::endl;
        return 0;
    }

    void encodeFrames(FFmpegEncodeFrameRequest* request)
    {
        AVCodecContext* c = renderContext->c;
        AVFrame* frame = renderContext->frame;
        FILE* f = renderContext->f;
        int width = renderContext->width;
        int height = renderContext->height;
        AVCodec* codec = renderContext->codec;
        AVPacket pkt;
            av_init_packet(&pkt);
            pkt.data = NULL;    // packet data will be allocated by the encoder
            pkt.size = 0;
            fflush(stdout);
            /* prepare a dummy image */
            /* Y */

            SwsContext * ctx = sws_getContext(c->width, c->height,
                                      AV_PIX_FMT_BGR32, c->width, c->height,
                                      AV_PIX_FMT_YUV420P, 0, 0, 0, 0);
            uint8_t * inData[1] = { request->frames[0].data }; // RGB24 have one plane
            int inLinesize[1] = { 4*c->width }; // RGB stride
            sws_scale(ctx, inData, inLinesize, 0, c->height, frame->data, frame->linesize);


            frame->pts = request->startFrameIndex;
            /* encode the image */
            int got_output;
            int ret = avcodec_encode_video2(c, &pkt, frame, &got_output);
            if (ret < 0) {
                fprintf(stderr, "Error encoding frame\n");
                exit(1);
            }
            if (got_output) {
                printf("Write frame %3d (size=%5d)\n", request->startFrameIndex, pkt.size);
                fwrite(pkt.data, 1, pkt.size, f);
                av_free_packet(&pkt);
            }
    }

    void clearEncoder(FFmpegClearEncoderRequest* request) {
        AVCodecContext* c = renderContext->c;
        AVFrame* frame = renderContext->frame;
        FILE* f = renderContext->f;
        int width = renderContext->width;
        int height = renderContext->height;
        AVCodec* codec = renderContext->codec;
        int i = 0;
        /*
        for (int got_output = 1; got_output; i++) {
            fflush(stdout);
            int ret = avcodec_encode_video2(c, &pkt, NULL, &got_output);
            if (ret < 0) {
                fprintf(stderr, "Error encoding frame\n");
                exit(1);
            }
            if (got_output) {
                printf("Write frame %3d (size=%5d)\n", i, pkt.size);
                fwrite(pkt.data, 1, pkt.size, f);
                av_free_packet(&pkt);
            }
        }*/

        /* add sequence end code to have a real mpeg file */
        uint8_t endcode[] = { 0, 0, 1, 0xb7 };
        fwrite(endcode, 1, sizeof(endcode), f);
        fclose(f);
        avcodec_close(c);
        av_free(c);
        av_freep(&frame->data[0]);
        av_frame_free(&frame);
        printf("\n");
    }

    /*
    int main() {
        avcodec_register_all();

        FFmpegInitEncoderRequest* request = new FFmpegInitEncoderRequest();
        request->fileName = "/tmp/test2.mpeg";
        request->framerate = 1/25.0;
        request->width = 1920;
        request->height = 1080;

        initEncoder(request);
        std::cout << "Init done " << (int)renderContext->frame->data[0][0] << std::endl;
        for (int i = 0; i < 200; ++i) {
        std::cout << "Init done " << (int)renderContext->frame->data[0][0] << std::endl;
            uint8_t* data;
            const int WIDTH = request->width;
            const int HEIGHT = request->height;


            FFmpegEncodeFrameRequest* frameRequest = new FFmpegEncodeFrameRequest;
            frameRequest->startFrameIndex = i;
            frameRequest->encoderIndex = 0;
            frameRequest->frames = new FFMpegFrame[1];
            frameRequest->frames[0].data = new uint8_t[HEIGHT * WIDTH * 4];

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {

                    frameRequest->frames[0].data[y * WIDTH * 4 + x * 4 + 0] = ((y + i) % 50) * 3;
                    frameRequest->frames[0].data[y * WIDTH * 4 + x * 4 + 1] = ((y + i) % 50) * 3;
                    frameRequest->frames[0].data[y * WIDTH * 4 + x * 4 + 2] = ((y + i) % 50) * 3;
                    frameRequest->frames[0].data[y * WIDTH * 4 + x * 4 + 3] = 255;
                }
            }
            encodeFrames(frameRequest);

            delete[] frameRequest->frames[0].data;
            delete[] frameRequest->frames;
            delete frameRequest;
        }
        std::cout << "Prepare to clean" << std::endl;

        FFmpegClearEncoderRequest* clearRequest = new FFmpegClearEncoderRequest();
        clearRequest->encoderIndex = 0;

        clearEncoder(clearRequest);

        delete request;
        delete clearRequest;
        delete renderContext;
    } */
}
