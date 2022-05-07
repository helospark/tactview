#include <string>
#include <map>
#include <set>
#include <iostream>
#include "common.h"


#ifdef DEBUG
    #include <fstream>
    #include <sstream>
#endif

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavutil/error.h>

#include <stdio.h>

    struct DecodeStructure
    {
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtx = NULL;
        const AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        struct SwsContext *sws_ctx = NULL;
        int width, height;
        int frame = 0;
    };

    int globalJobId = 0;
    std::map<int, DecodeStructure*> decodeStructureMap;

    void copyFrameData(AVFrame *pFrame, int width, int height, int iFrame, char* frames)
    {
        //DEBUG("Copying data " << width << " " << height );
        for(int y=0; y<height; y++)
        {
            for (int i = 0; i < width; ++i)
            {
                int id = y*pFrame->linesize[0] + i * 4;
                frames[y * width * 4 + i * 4 + 0] = *(pFrame->data[0] + id + 2);
                frames[y * width * 4 + i * 4 + 1] = *(pFrame->data[0] + id + 1);
                frames[y * width * 4 + i * 4 + 2] = *(pFrame->data[0] + id + 0);
                frames[y * width * 4 + i * 4 + 3] = *(pFrame->data[0] + id + 3);
            }
        }

    }

    typedef struct
    {
        int frame;
        char* data;
    } FFmpegFrameWithFrameNumber;

    typedef struct
    {
        int jobId;
        int numberOfFrames;
        FFmpegFrameWithFrameNumber* frames;
    } QueryFramesRequest;

    AVFrame* allocateFrame(int width, int height)
    {
        AVFrame* pFrameRGB=av_frame_alloc();
        int numBytes=av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 16);

        uint8_t* buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
        av_image_fill_arrays(pFrameRGB->data, pFrameRGB->linesize, buffer, AV_PIX_FMT_RGBA,
                       width, height, 1);
        pFrameRGB->opaque = buffer;
        return pFrameRGB;
    }


    void freeFrame(AVFrame* frame)
    {
        // DEBUG("Preparing to free " << frame << " " << frame->opaque );
        av_free(frame->opaque);
        av_frame_free(&frame);
    }

    int receiveFrame(AVCodecContext *avctx, AVFrame *frame, int *got_frame, AVPacket *pkt) {
        int ret = avcodec_receive_frame(avctx, frame);
        if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF)
            return ret;
        if (ret >= 0)
            *got_frame = 1;
        return 0;
    }


    // From here: https://blogs.gentoo.org/lu_zero/2016/03/29/new-avcodec-api/
    int decode_video_frame(AVCodecContext *avctx, AVFrame *frame, int *got_frame, AVPacket *pkt)
    {
        int ret;

        *got_frame = 0;

        if (pkt) {
            ret = avcodec_send_packet(avctx, pkt);
            // In particular, we don't expect AVERROR(EAGAIN), because we read all
            // decoded frames with avcodec_receive_frame() until done.
            
            if ( ret == AVERROR(EAGAIN)) {
                receiveFrame(avctx, frame, got_frame, pkt);
                return AVERROR(EAGAIN);
            }
            
            if (ret < 0) {
                return ret == AVERROR_EOF ? 0 : ret;
            }
        }

        return receiveFrame(avctx, frame, got_frame, pkt);
    }


    EXPORTED int readFrames(QueryFramesRequest* request)
    {
        DEBUG("Called readFrames" );
        std::map<int,DecodeStructure*>::iterator elementIterator = decodeStructureMap.find(request->jobId);

        DecodeStructure* decodeStructure;

        if (elementIterator == decodeStructureMap.end())
        {
            ERROR("Job not initialized" );
            return -1;
        }
        else
        {
            decodeStructure = elementIterator->second;
        }

        AVFormatContext   *pFormatCtx = decodeStructure->pFormatCtx;
        int               videoStream = decodeStructure->videoStream;
        AVCodecContext    *pCodecCtx = decodeStructure->pCodecCtx;
        const AVCodec           *pCodec = decodeStructure->pCodec;
        AVFrame           *pFrame = decodeStructure->pFrame;
        AVPacket          packet;
        int               frameFinished = 0;
        struct SwsContext *sws_ctx = decodeStructure->sws_ctx;

        int i = 0;
        while(i < request->numberOfFrames && av_read_frame(pFormatCtx, &packet)>=0)
        {
            if(packet.stream_index==videoStream)
            {
                DEBUG("Read pts=" << packet.pts << " dts=" << packet.dts );
                int decodedFrame = decode_video_frame(pCodecCtx, pFrame, &frameFinished, &packet);

                if(frameFinished)
                {
                    AVFrame*  pFrameRGB=allocateFrame(decodeStructure->width, decodeStructure->height);
                    sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
                                pFrame->linesize, 0, pCodecCtx->height,
                                pFrameRGB->data, pFrameRGB->linesize);

                    copyFrameData(pFrameRGB, decodeStructure->width, decodeStructure->height, i, request->frames[i].data);
                    request->frames[i].frame = decodeStructure->frame;
                    ++i;
                    ++decodeStructure->frame;
                    freeFrame(pFrameRGB);
                }
            }
            // Free the packet that was allocated by av_read_frame
            av_packet_unref(&packet);
        }

        return i;
    }

    struct InitializeReadJobRequest {
        const char* path;
        int width;
        int height;
    };

    EXPORTED int openFile(InitializeReadJobRequest* request)
    {
        DEBUG("Opening file " << request->path << " " << request->width << " " << request->height );

        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodecParameters *pCodecCtxOrig = NULL;
        const AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        struct SwsContext *sws_ctx = NULL;

        if(avformat_open_input(&pFormatCtx, request->path, NULL, NULL)!=0)
        {
            ERROR("Cannot open input " );
            return -1;
        }

        if(avformat_find_stream_info(pFormatCtx, NULL)<0)
        {
            ERROR("Cannot find stream info " );
            return -1;
        }

        videoStream=-1;
        for(int i=0; i<pFormatCtx->nb_streams; i++)
            if(pFormatCtx->streams[i]->codecpar->codec_type==AVMEDIA_TYPE_VIDEO)
            {
                videoStream=i;
                break;
            }
        if(videoStream==-1)
        {
            ERROR("No video stream found in " << request->path );
            return -1;
        }

        pCodecCtxOrig=pFormatCtx->streams[videoStream]->codecpar;
        int ret = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pCodec, 0);
        if(pCodec==NULL)
        {
            ERROR("Unsupported codec: " << pCodecCtxOrig->codec_id );
            return NULL;
        }
        DEBUG("Using codec " << pCodec->name << " for " << request->path );

        pCodecCtx = avcodec_alloc_context3(pCodec);

        if (avcodec_parameters_to_context(pCodecCtx, pCodecCtxOrig) < 0) {
            ERROR("Cannot create context from parameters");
            return NULL;
        }

        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
        {
            ERROR("Cannot open codec context" );
            return NULL;
        }


        pFrame=av_frame_alloc();

        DEBUG("Opening file with size " << request->width << "x" << request->height );

        sws_ctx = sws_getContext(pCodecCtx->width,
                                 pCodecCtx->height,
                                 pCodecCtx->pix_fmt,
                                 request->width,
                                 request->height,
                                 AV_PIX_FMT_BGRA,
                                 SWS_BILINEAR,
                                 NULL,
                                 NULL,
                                 NULL
                                );

        DecodeStructure* element = new DecodeStructure();
        element->pFormatCtx = pFormatCtx;
        element->videoStream = videoStream;
        element->pCodecCtx = pCodecCtx;
        element->pCodec = pCodec;
        element->pFrame = pFrame;
        element->sws_ctx = sws_ctx;
        element->width = request->width;
        element->height = request->height;

        int id = globalJobId++;

        decodeStructureMap[id] = element;

        return id;
    }

    EXPORTED void freeJob(int jobId) {
        std::map<int,DecodeStructure*>::iterator elementIterator = decodeStructureMap.find(jobId);

        DecodeStructure* decodeStructure;

        if (elementIterator == decodeStructureMap.end())
        {
            ERROR("Job not initialized" );
            return;
        }
        else
        {
            decodeStructure = elementIterator->second;
        }

        //avcodec_free_context(&decodeStructure->pCodecCtx);
        avformat_free_context(decodeStructure->pFormatCtx);
        av_frame_free(&decodeStructure->pFrame);
        sws_freeContext(decodeStructure->sws_ctx);
    }
}

#ifdef DEBUG

int charToUnsignedInt(char data) {
    int iData = (int)data;
    if (data < 0) {
        return iData + 256;
    } else {
        return iData;
    }
}


void saveFrame(int width, int height, FFmpegFrameWithFrameNumber* frame) {
    DEBUG("Writing " << frame->frame );
    std::stringstream ss;
    ss << "/tmp/frame_" << frame->frame << ".ppm";
    std::ofstream file(ss.str());

    file << "P3\n";
    file << width << " " << height << " 255 255 255\n";

    char* imageData = (char*)frame->data;
    for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {
            file << charToUnsignedInt(imageData[i * width * 4 + j * 4 + 2]) << " ";
            file << charToUnsignedInt(imageData[i * width * 4 + j * 4 + 1]) << " ";
            file << charToUnsignedInt(imageData[i * width * 4 + j * 4 + 0]) << " ";
            file << "\n";
        }
    }
}

int main() {
    InitializeReadJobRequest initRequest;
    initRequest.path = "/opt/testvideo.mp4";
    initRequest.width = 1280;
    initRequest.height = 720;

    int job = openFile(&initRequest);

    if (job < 0) {
        ERROR("Unable to open job" );
        return -1;
    }

    int f = 0;

    do {

        QueryFramesRequest req;
        req.jobId = job;
        req.numberOfFrames = 10;
        req.frames = new FFmpegFrameWithFrameNumber[10];
        for (int i = 0; i < req.numberOfFrames; ++i) {
            req.frames[i].data = new char[initRequest.width * initRequest.height * 4];
        }

        f = readFrames(&req);

        DEBUG("Read " << f << " frames" );

        for (int i = 0; i < f; ++i) {
            saveFrame(initRequest.width, initRequest.height, &req.frames[i]);
        }

        for (int i = 0; i < req.numberOfFrames; ++i) {
            delete[] req.frames[i].data;
        }
        delete[] req.frames;

    } while (f > 0);

    freeJob(job);
}
#endif