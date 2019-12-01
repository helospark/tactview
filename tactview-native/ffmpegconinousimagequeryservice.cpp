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

#include <stdio.h>

    struct DecodeStructure
    {
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        struct SwsContext *sws_ctx = NULL;
        int width, height;
        int frame = 0;
    };

    int globalJobId = 0;
    std::map<int, DecodeStructure*> jobDecodeStructureMap;

    void copyFrameData(AVFrame *pFrame, int width, int height, int iFrame, char* frames)
    {
        std::cout << "Copying data " << width << " " << height << std::endl;
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
        int numBytes=avpicture_get_size(AV_PIX_FMT_BGRA, width, height);

        uint8_t* buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
        avpicture_fill((AVPicture *)pFrameRGB, buffer, AV_PIX_FMT_BGRA,
                       width, height);
        pFrameRGB->opaque = buffer;
        return pFrameRGB;
    }

    void freeFrame(AVFrame* frame)
    {
        // std::cout << "Preparing to free " << frame << " " << frame->opaque << std::endl;
        av_free(frame->opaque);
        av_frame_free(&frame);
    }

    EXPORTED int readFrames(QueryFramesRequest* request)
    {
        std::cout << "Called readFrames" << std::endl;
        std::map<int,DecodeStructure*>::iterator elementIterator = jobDecodeStructureMap.find(request->jobId);

        DecodeStructure* decodeStructure;

        if (elementIterator == jobDecodeStructureMap.end())
        {
            std::cout << "Job not initialized" << std::endl;
            return -1;
        }
        else
        {
            decodeStructure = elementIterator->second;
        }

        AVFormatContext   *pFormatCtx = decodeStructure->pFormatCtx;
        int               videoStream = decodeStructure->videoStream;
        AVCodecContext    *pCodecCtx = decodeStructure->pCodecCtx;
        AVCodec           *pCodec = decodeStructure->pCodec;
        AVFrame           *pFrame = decodeStructure->pFrame;
        AVPacket          packet;
        int               frameFinished = 0;
        struct SwsContext *sws_ctx = decodeStructure->sws_ctx;

        int i = 0;
        while(i < request->numberOfFrames && av_read_frame(pFormatCtx, &packet)>=0)
        {
            if(packet.stream_index==videoStream)
            {
                std::cout << "Read " << packet.pts << " " << packet.dts << std::endl;
                int decodedFrame = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);
                std::cout << "Decoded frame " << frameFinished << std::endl;

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
            av_free_packet(&packet);
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
        std::cout << "Opening file " << request->path << " " << request->width << " " << request->height << std::endl;

        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodecContext    *pCodecCtxOrig = NULL;
        AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        struct SwsContext *sws_ctx = NULL;

        av_register_all();

        if(avformat_open_input(&pFormatCtx, request->path, NULL, NULL)!=0)
        {
            std::cerr << "Cannot open input " << std::endl;
            return -1;
        }

        if(avformat_find_stream_info(pFormatCtx, NULL)<0)
        {
            std::cerr << "Cannot find stream info " << std::endl;
            return -1;
        }

        videoStream=-1;
        for(int i=0; i<pFormatCtx->nb_streams; i++)
            if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO)
            {
                videoStream=i;
                break;
            }
        if(videoStream==-1)
        {
            std::cerr << "No video stream found in " << request->path << std::endl;
            return -1;
        }

        pCodecCtxOrig=pFormatCtx->streams[videoStream]->codec;
        pCodec=avcodec_find_decoder(pCodecCtxOrig->codec_id);
        if(pCodec==NULL)
        {
            std::cerr << "Unsupported codec: " << pCodecCtxOrig->codec_id << std::endl;
            return NULL;
        }
        std::cout << "Using codec " << pCodec->name << " for " << request->path << std::endl;

        pCodecCtx = avcodec_alloc_context3(pCodec);

        if(avcodec_copy_context(pCodecCtx, pCodecCtxOrig) != 0)
        {
            std::cerr << "Couldn't copy codec context" << std::endl;
            return NULL;
        }

        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
        {
            std::cerr << "Cannot open codec context" << std::endl;
            return NULL;
        }


        pFrame=av_frame_alloc();

        std::cout << "Opening file with size " << request->width << " " << request->height << std::endl;

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

        std::cout << "Job with id " << id << " created" << std::endl;

        jobDecodeStructureMap[id] = element;

        std::cout << "returning" << std::endl;

        return id;
    }

    EXPORTED void freeJob(int jobId) {
        std::map<int,DecodeStructure*>::iterator elementIterator = jobDecodeStructureMap.find(jobId);

        DecodeStructure* decodeStructure;

        if (elementIterator == jobDecodeStructureMap.end())
        {
            std::cout << "Job not initialized" << std::endl;
            return;
        }
        else
        {
            decodeStructure = elementIterator->second;
            jobDecodeStructureMap.erase(elementIterator);
        }

        //avcodec_free_context(&decodeStructure->pCodecCtx);
        //avformat_close_input(&decodeStructure->pFormatCtx);
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
    std::cout << "Writing " << frame->frame << std::endl;
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
    initRequest.path = "/home/black/Documents/Time Lapse Video Of Night Sky.mp4";
    initRequest.width = 320;
    initRequest.height = 180;

    int job = openFile(&initRequest);

    if (job < 0) {
        std::cout << "Unable to open job" << std::endl;
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

        std::cout << "Read " << f << " frames" << std::endl;

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