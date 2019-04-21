#include <string>
#include <map>
#include <iostream>
#include "common.h"

extern "C" {
    #include <libavcodec/avcodec.h>
    #include <libavformat/avformat.h>
    #include <libswscale/swscale.h>

    #include <stdio.h>

    struct DecodeStructure {
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        AVFrame           *pFrameRGB = NULL;
        AVPacket          packet;
        uint8_t           *buffer = NULL;
        struct SwsContext *sws_ctx = NULL;
        int64_t lastPts = -1;
    };


    std::map<std::string, DecodeStructure*> decodeStructureMap;

    void copyFrameData(AVFrame *pFrame, int width, int height, int iFrame, char* frames)
    {
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


    struct MediaMetadata
    {
        double fps;
        int width;
        int height;
        int bitRate;
        long long lengthInMicroseconds;
    };

    EXPORTED MediaMetadata readMediaMetadata(const char* path)
    {
        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               i, videoStream;
        AVCodecContext    *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        AVFrame           *pFrameRGB = NULL;
        MediaMetadata mediaMetadata;
	      mediaMetadata.fps = -1;
	      mediaMetadata.width = -1;
	      mediaMetadata.height = -1;
	      mediaMetadata.lengthInMicroseconds = -1;
        mediaMetadata.bitRate = 0;

        av_register_all();

        if(avformat_open_input(&pFormatCtx, path, NULL, NULL)!=0)
            return mediaMetadata;

        if(avformat_find_stream_info(pFormatCtx, NULL)<0)
            return mediaMetadata;

        av_dump_format(pFormatCtx, 0, path, 0);

        videoStream=-1;
        for(i=0; i<pFormatCtx->nb_streams; i++)
            if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO)
            {
                videoStream=i;
                break;
            }
        if(videoStream==-1)
            return mediaMetadata;

        pCodecCtxOrig=pFormatCtx->streams[videoStream]->codec;

        pCodec=avcodec_find_decoder(pCodecCtxOrig->codec_id);
        if(pCodec==NULL)
        {
            fprintf(stderr, "Unsupported codec!\n");
            return mediaMetadata;
        }

        pCodecCtx = avcodec_alloc_context3(pCodec);

        if(avcodec_copy_context(pCodecCtx, pCodecCtxOrig) != 0)
        {
            fprintf(stderr, "Couldn't copy codec context");
            return mediaMetadata;
        }

        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
            return mediaMetadata;

        pFrame=av_frame_alloc();

        pFrameRGB=av_frame_alloc();
        if(pFrameRGB==NULL)
            return mediaMetadata;

        AVStream* st = pFormatCtx->streams[videoStream];

        mediaMetadata.width = pCodecCtx->width;
        mediaMetadata.height = pCodecCtx->height;
        mediaMetadata.lengthInMicroseconds = pFormatCtx->duration / (AV_TIME_BASE / 1000000);
        mediaMetadata.bitRate = st->codec->bit_rate;

        AVRational framerate;

        if (st->avg_frame_rate.den == 0)
        {
            framerate = st->r_frame_rate;
        }
        else
        {
            framerate = st->avg_frame_rate;
        }

        mediaMetadata.fps = framerate.num / (double)framerate.den;

        std::cout << "Length 1=" << mediaMetadata.lengthInMicroseconds << "2=" << (st->duration / (AV_TIME_BASE / 1000000)) << std::endl;

        av_frame_free(&pFrameRGB);

        av_frame_free(&pFrame);

        avcodec_close(pCodecCtx);
        avcodec_close(pCodecCtxOrig);

        avformat_close_input(&pFormatCtx);

        return mediaMetadata;
    }

    typedef struct
    {
        char* data;
    } FFMpegFrame;

    typedef struct
    {
        int width;
        int height;
        int numberOfFrames;
        int useApproximatePosition;
        long long startMicroseconds;
        char* path;
        FFMpegFrame* frames;
    } FFmpegImageRequest;

    const char* createKey(FFmpegImageRequest* request) {
        return (std::string(request->path) + "_" + std::to_string(request->width) + "_" + std::to_string(request->height)).c_str();
    }

    DecodeStructure* openFile(FFmpegImageRequest* request);

    EXPORTED void readFrames(FFmpegImageRequest* request)
    {
        std::map<std::string,DecodeStructure*>::iterator elementIterator = decodeStructureMap.find(std::string(createKey(request)));

        DecodeStructure* element;

        std::cout << "Found Element" <<  elementIterator->first << std::endl;

        if (elementIterator == decodeStructureMap.end()) {
            element = openFile(request);
        } else {
            element = elementIterator->second;
        }

        if (element == NULL) {
            return;
        }

        int i = 0;
        AVFormatContext   *pFormatCtx = element->pFormatCtx;
        int               videoStream = element->videoStream;
        AVCodecContext    *pCodecCtxOrig = element->pCodecCtxOrig;
        AVCodecContext    *pCodecCtx = element->pCodecCtx;
        AVCodec           *pCodec = element->pCodec;
        AVFrame           *pFrame = element->pFrame;
        AVFrame           *pFrameRGB = element->pFrameRGB;
        AVPacket          packet = element->packet;
        int               frameFinished = 0;
        uint8_t           *buffer = element->buffer;
        struct SwsContext *sws_ctx = element->sws_ctx;

		// AV_TIME_BASE_Q   (AVRational){1, AV_TIME_BASE} -> VC++ causes error
		AVRational timeBaseQ;
		timeBaseQ.num = 1;
		timeBaseQ.den = AV_TIME_BASE;

        int64_t seek_target = request->startMicroseconds * (AV_TIME_BASE / 1000000); // rethink
        int64_t minimumTimeRequiredToSeek = 2 * 1000000 * (AV_TIME_BASE / 1000000); // Seek if distance is more than 2 seconds
        seek_target = av_rescale_q(seek_target, timeBaseQ, pFormatCtx->streams[videoStream]->time_base);
        minimumTimeRequiredToSeek = av_rescale_q(minimumTimeRequiredToSeek, timeBaseQ, pFormatCtx->streams[videoStream]->time_base);
        int64_t seek_distance = seek_target - element->lastPts;

        //std::cout << "Seek distance " << seek_distance << std::endl;
        //std::cout << "MIN TIME = " << minimumTimeRequiredToSeek << std::endl;
        //std::cout << "Want to read " << request->startMicroseconds << " current packet pts " << element->lastPts << std::endl;

        if (seek_distance > minimumTimeRequiredToSeek || seek_distance <= 0) {
          std::cout << "Seeking to " << request->startMicroseconds << " current position " << element->lastPts << " distance " << seek_distance << std::endl;
          av_seek_frame(pFormatCtx, videoStream, seek_target, AVSEEK_FLAG_BACKWARD);
          avcodec_flush_buffers(pCodecCtx);
        } else {
          std::cout << "No seek, distance " << seek_distance << std::endl;
        }

        while(av_read_frame(pFormatCtx, &packet)>=0 && i < request->numberOfFrames)
        {
            if(packet.stream_index==videoStream)
            {
                int decodedFrame = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);

                if (packet.pts < seek_target && frameFinished) {
                  std::cout << "Skipping package " << packet.pts << std::endl;
                }

                if(frameFinished && (packet.pts >= seek_target || request->useApproximatePosition) )
                {
                    sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
                              pFrame->linesize, 0, pCodecCtx->height,
                              pFrameRGB->data, pFrameRGB->linesize);

                    copyFrameData(pFrameRGB, request->width, request->height, i, request->frames[i].data);
                    element->lastPts = packet.pts;
                    ++i;
                }
            }
            // Free the packet that was allocated by av_read_frame
            av_free_packet(&packet);
        }

    }

    DecodeStructure* openFile(FFmpegImageRequest* request) {
        std::cout << "Opening file " << request->path << " " << request->width << " " << request->height << std::endl;

        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecContext    *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        AVFrame           *pFrameRGB = NULL;
        AVPacket          packet;
        uint8_t           *buffer = NULL;
        struct SwsContext *sws_ctx = NULL;

        av_register_all();

        if(avformat_open_input(&pFormatCtx, request->path, NULL, NULL)!=0) {
            std::cerr << "Cannot open input " << std::endl;
            return NULL;
        }

        if(avformat_find_stream_info(pFormatCtx, NULL)<0) {
            std::cerr << "Cannot find stream info " << std::endl;
            return NULL;
        }

        videoStream=-1;
        for(int i=0; i<pFormatCtx->nb_streams; i++)
            if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO)
            {
                videoStream=i;
                break;
            }
        if(videoStream==-1) {
            std::cerr << "No video stream found in " << request->path << std::endl;
            return NULL;
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

        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0) {
            std::cerr << "Cannot open codec context" << std::endl;
            return NULL;
        }

        pFrame=av_frame_alloc();

        pFrameRGB=av_frame_alloc();
        if(pFrameRGB == NULL || pFrame == NULL) {
            std::cerr << "Cannot allocate RGB frame " << std::endl;
            return NULL;
        }

        std::cout << "Opening file with size " << request->width << " " << request->width << std::endl;

        int numBytes=avpicture_get_size(AV_PIX_FMT_RGBA, request->width,
                                     request->height);
        buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));

        avpicture_fill((AVPicture *)pFrameRGB, buffer, AV_PIX_FMT_BGRA,
                       request->width, request->height);

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
        element->pCodecCtxOrig = pCodecCtxOrig;
        element->pCodecCtx = pCodecCtx;
        element->pCodec = pCodec;
        element->pFrame = pFrame;
        element->pFrameRGB = pFrameRGB;
        element->packet = packet;
        element->buffer = buffer;
        element->sws_ctx = sws_ctx;

        decodeStructureMap.insert(std::pair<std::string, DecodeStructure*>(std::string(createKey(request)), element));

        return element;
    }
}

