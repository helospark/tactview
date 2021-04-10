/**
* This is mostly the example from FFmpeg source: https://github.com/FFmpeg/FFmpeg/blob/master/doc/examples/muxing.c
* Modified a bit to:
* - separate the init, teardown and image render phases so it can be called by JNI
* - convert the data coming from the video editor instead of generating it
* - sound is buffered, since we need to fill the entire audio frame buffer and it's usually not the same size as we get from Java side
* 
* Note that synchronization is done on Java side by sending sound and image for every frame, so no need for the original pts synch here.
* Note that this is single threaded due to global variables used
*
* More information about how this was developed can be found here: https://hips.hearstapps.com/ghk.h-cdn.co/assets/cm/15/11/54ffe5266025c-dog1.jpg
*/

// Causes compilation error if I don't define this
#define __STDC_CONSTANT_MACROS whatisthis

#include <iostream>
#include "common.h"

extern "C" {
    #include <stdlib.h>
    #include <stdio.h>
    #include <string.h>
    #include <math.h>

    #include <libavutil/avassert.h>
    #include <libavutil/channel_layout.h>
    #include <libavutil/opt.h>
    #include <libavutil/mathematics.h>
    #include <libavutil/timestamp.h>
    #include <libavformat/avformat.h>
    #include <libswscale/swscale.h>
    #include <libswresample/swresample.h>
    #include "libavutil/pixdesc.h"

    #define SCALE_FLAGS SWS_BICUBIC


    struct FFmpegInitEncoderRequest {
        const char* fileName;
        int actualWidth;
        int actualHeight;
        int renderWidth;
        int renderHeight;
        double fps;

        int audioChannels;
        int bytesPerSample;
        int sampleRate;

        int videoBitRate;
        int audioBitRate;
        int audioSampleRate;
        const char* videoCodec;
        const char* audioCodec;
        const char* videoPixelFormat;
        const char* videoPreset;

        NativeMap* metadata;
    };


    // a wrapper around a single output AVStream
    struct OutputStream {
        AVStream *st;
        AVCodecContext *enc;

        /* pts of the next frame that will be generated */
        int64_t next_pts;
        int samples_count;

        AVFrame *frame;
        AVFrame *tmp_frame;

        // data about sound may no belong here
        AVSampleFormat sampleFormat;
        int bytesPerSample;

        struct SwsContext *sws_ctx;
        struct SwrContext *swr_ctx;
    };

    struct RenderContext {
        OutputStream* video_st = NULL;
        OutputStream* audio_st = NULL;
        const char *filename;
        AVOutputFormat *fmt;
        AVFormatContext *oc;
        AVCodec *audio_codec, *video_codec;
        int ret;
        int have_video = 0, have_audio = 0;
        int encode_video = 0, encode_audio = 0;
        AVDictionary *opt = NULL;
        unsigned char* audioBuffer;
        unsigned int audioBufferPointer = 0;
        int bytesPerSample;
        int numberOfSamplesPerAudioFrame;
        int audioChannels;
        int sampleRate;

        int actualWidth;
        int actualHeight;
        int renderWidth;
        int renderHeight;
        AVPixelFormat videoPixelFormat;    
    };
    RenderContext renderContext;

    struct FFmpegClearEncoderRequest {
        int encoderIndex;
    };

    struct RenderFFMpegFrame {
        unsigned char* imageData;

        unsigned char* audioData;
        int numberOfAudioSamples;
    };

    struct FFmpegEncodeFrameRequest {
        int encoderIndex;
        int startFrameIndex;
        RenderFFMpegFrame* frame;
    };


    static int write_frame(AVFormatContext *fmt_ctx, const AVRational *time_base, AVStream *st, AVPacket *pkt)
    {
        /* rescale output packet timestamp values from codec to stream timebase */
        av_packet_rescale_ts(pkt, *time_base, st->time_base);

        pkt->stream_index = st->index;

        return av_interleaved_write_frame(fmt_ctx, pkt);
    }

    /* Add an output stream. */
    static int add_video_stream(OutputStream *ost, AVFormatContext *oc,
                           AVCodec **codec,
                           enum AVCodecID codec_id,
                           FFmpegInitEncoderRequest* request,
                           AVPixelFormat videoPixelFormat)
    {
        AVCodecContext *c;
        int i;

        /* find the encoder */
        *codec = avcodec_find_encoder(codec_id);
        if (!(*codec)) {
            std::cout << "[ERROR] Could not find encoder for '" << avcodec_get_name(codec_id) << "'" << std::endl;
            return -1;
        }

        ost->st = avformat_new_stream(oc, NULL);
        if (!ost->st) {
            std::cout << "[ERROR] Could not allocate stream" << std::endl;
            return -1;
        }
        ost->st->id = oc->nb_streams-1;
        c = avcodec_alloc_context3(*codec);
        if (!c) {
            std::cout << "[ERROR] Could not alloc an encoding context" << std::endl;
            return -1;
        }
        ost->enc = c;

        c->codec_id = codec_id;

        c->bit_rate = request->videoBitRate;
        /* Resolution must be a multiple of two. */
        c->width = request->renderWidth;
        c->height = request->renderHeight;

        /* timebase: This is the fundamental unit of time (in seconds) in terms
         * of which frame timestamps are represented. For fixed-fps content,
         * timebase should be 1/framerate and timestamp increments should be
         * identical to 1. */
        ost->st->time_base = av_d2q(1.0 / request->fps, 10000000);

        std::cout << "Encoder FPS: " << ost->st->time_base.num << " / " << ost->st->time_base.den << std::endl;

        c->time_base       = ost->st->time_base;

        c->pix_fmt       = videoPixelFormat;
        
        c->max_b_frames = 0; // TODO: make configurable
        
        if (c->codec_id == AV_CODEC_ID_MPEG1VIDEO) {
           /* Needed to avoid using macroblocks in which some coeffs overflow.
            * This does not happen with normal video, it just happens here as
            * the motion of the chroma plane does not match the luma plane. */
           c->mb_decision = 2;
        }

        if (oc->oformat->flags & AVFMT_GLOBALHEADER)
            c->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        ost->sws_ctx = sws_getContext(request->actualWidth, request->actualHeight,
                                  AV_PIX_FMT_BGR32, c->width, c->height,
                                  videoPixelFormat, 0, 0, 0, 0);
        return 0;
    }

/* Add an output stream. */
    static int add_audio_stream(OutputStream *ost, AVFormatContext *oc,
                           AVCodec **codec,
                           enum AVCodecID codec_id,
                           FFmpegInitEncoderRequest* request)
    {
        AVCodecContext *c;
        int i;

        /* find the encoder */
        *codec = avcodec_find_encoder(codec_id);
        if (!(*codec)) {
            std::cout << "[ERROR] Could not find encoder for '" << avcodec_get_name(codec_id) << "'" << std::endl;
            return -1;
        }

        ost->st = avformat_new_stream(oc, NULL);
        if (!ost->st) {
            std::cout << "[ERROR] Could not allocate stream" << std::endl;
            return -1;
        }
        ost->st->id = oc->nb_streams-1;
        c = avcodec_alloc_context3(*codec);
        if (!c) {
            std::cout << "[ERROR] Could not alloc an encoding context" << std::endl;
            return -1;
        }
        ost->enc = c;

        c->sample_fmt  = (*codec)->sample_fmts ?
            (*codec)->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
        c->bit_rate    = request->audioBitRate;
        c->sample_rate = 44100; // default if none found
        if ((*codec)->supported_samplerates) {
            c->sample_rate = (*codec)->supported_samplerates[0];
            for (i = 0; (*codec)->supported_samplerates[i]; i++) {
                if ((*codec)->supported_samplerates[i] == request->audioSampleRate)
                    c->sample_rate = request->audioSampleRate;
            }
        }
        c->channel_layout = AV_CH_LAYOUT_STEREO; // default

        int64_t expectedChannelLayout = av_get_default_channel_layout(request->audioChannels);

        if ((*codec)->channel_layouts) {
            c->channel_layout = (*codec)->channel_layouts[0];
            for (i = 0; (*codec)->channel_layouts[i]; i++) {
                if ((*codec)->channel_layouts[i] == expectedChannelLayout) {
                    std::cout << "Supported channel layout " << (*codec)->channel_layouts[i] << " " << av_get_channel_layout_nb_channels((*codec)->channel_layouts[i]) << std::endl; 
                    c->channel_layout = expectedChannelLayout;
                    break;
                }
            }
        } else {
            std::cout << "Channel layouts are unknown, falling back to stereo" << std::endl;
        }
        c->channels        = av_get_channel_layout_nb_channels(c->channel_layout);
        ost->st->time_base.num = 1;
        ost->st->time_base.den = c->sample_rate;

        /* Some formats want stream headers to be separate. */
        if (oc->oformat->flags & AVFMT_GLOBALHEADER)
            c->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        return 0;
    }


    /**************************************************************/
    /* audio output */

    static AVFrame *alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                      uint64_t channel_layout,
                                      int sample_rate, int nb_samples)
    {
        AVFrame *frame = av_frame_alloc();
        int ret;

        if (!frame) {
            std::cout << "[ERROR] Error allocating an audio frame" << std::endl;
            return NULL;
        }

        frame->format = sample_fmt;
        frame->channel_layout = channel_layout;
        frame->sample_rate = sample_rate;
        frame->nb_samples = nb_samples;

        if (nb_samples) {
            ret = av_frame_get_buffer(frame, 0);
            if (ret < 0) {
                std::cout << "[ERROR] Error allocating an audio buffer" << std::endl;
                return NULL;
            }
        }

        return frame;
    }

    static int open_audio(AVFormatContext *oc, AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg, FFmpegInitEncoderRequest* request)
    {
        AVCodecContext *c;
        int nb_samples;
        int ret;
        AVDictionary *opt = NULL;

        c = ost->enc;

        /* open it */
        av_dict_copy(&opt, opt_arg, 0);
        ret = avcodec_open2(c, codec, &opt);
        av_dict_free(&opt);
        if (ret < 0) {
            std::cout << "[ERROR] Could not open audio codec:" << std::endl;
            return -1;
        }

        if (c->codec->capabilities & AV_CODEC_CAP_VARIABLE_FRAME_SIZE)
            nb_samples = 10000;
        else
            nb_samples = c->frame_size;


        if (request->bytesPerSample == 1) {
            ost->sampleFormat = AV_SAMPLE_FMT_U8;
        } else if (request->bytesPerSample == 2) {
            ost->sampleFormat = AV_SAMPLE_FMT_S16;
        } else if (request->bytesPerSample == 4) {
            ost->sampleFormat = AV_SAMPLE_FMT_S32;
        } else if (request->bytesPerSample == 8) {
            ost->sampleFormat = AV_SAMPLE_FMT_S64;
        }
        ost->bytesPerSample = request->bytesPerSample;
        renderContext.numberOfSamplesPerAudioFrame = nb_samples;


        ost->frame     = alloc_audio_frame(c->sample_fmt, c->channel_layout,
                                           c->sample_rate, nb_samples);
        ost->tmp_frame = alloc_audio_frame(ost->sampleFormat, c->channel_layout,
                                           request->sampleRate, (int)ceil(nb_samples * ((double)request->sampleRate / c->sample_rate)));
        if (ost->frame == NULL || ost->tmp_frame == NULL) {
          return -1;
        }

        /* copy the stream parameters to the muxer */
        ret = avcodec_parameters_from_context(ost->st->codecpar, c);
        if (ret < 0) {
            fprintf(stderr, "Could not copy the stream parameters\n");
            return -1;
        }

        /* create resampler context */
            ost->swr_ctx = swr_alloc();
            if (!ost->swr_ctx) {
                std::cout << "[ERROR] Could not allocate resampler context" << std::endl;
                return -1;
            }

            std::cout << "SWR " << c->sample_rate << " " << request->sampleRate << " " << request->audioChannels << std::endl;

            /* set options */
            av_opt_set_int       (ost->swr_ctx, "in_channel_count",   request->audioChannels,       0);
            av_opt_set_int       (ost->swr_ctx, "in_sample_rate",     request->sampleRate,    0);
            av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt",      ost->sampleFormat, 0);
            av_opt_set_int       (ost->swr_ctx, "out_channel_count",  c->channels,       0);
            av_opt_set_int       (ost->swr_ctx, "out_sample_rate",    c->sample_rate,    0);
            av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt",     c->sample_fmt,     0);

            /* initialize the resampling context */
            if ((ret = swr_init(ost->swr_ctx)) < 0) {
                std::cout << "[ERROR] Failed to initialize the resampling context" << std::endl;
                return -1;
            }
            return 0;
    }

    static AVFrame *get_audio_frame(OutputStream *ost, unsigned char* dataStart)
    {
        AVFrame *frame = ost->tmp_frame;
        int j, i, v;
        unsigned char *q = (unsigned char*)frame->data[0];

        int copySampleFromIndex = 0;
        for (j = 0; j <frame->nb_samples; j++) {
            for (i = 0; i < renderContext.audioChannels; i++) {
                for (int k = 0; k < ost->bytesPerSample; ++k) {
                    *q++ = dataStart[copySampleFromIndex++];
                }
            }
        }

        frame->pts = ost->next_pts;
        ost->next_pts  += frame->nb_samples;

        return frame;
    }

    /*
     * encode one audio frame and send it to the muxer
     * return 1 when encoding is finished, 0 otherwise
     */
    static int write_audio_frame(AVFormatContext *oc, OutputStream *ost, AVFrame *frame)
    {
        AVCodecContext *c;
        AVPacket pkt = { 0 }; // data and size must be 0;
        int ret;
        int got_packet;
        int dst_nb_samples;

        av_init_packet(&pkt);
        c = ost->enc;
        std::cout << "Writing audio frame " << ((double)ost->next_pts * c->time_base.num / c->time_base.den) << " " << ost->samples_count << " " << dst_nb_samples << std::endl;

        if (frame) {
            /* convert samples from native format to destination codec format, using the resampler */
                /* compute destination number of samples */
                int a = swr_get_delay(ost->swr_ctx, renderContext.sampleRate) + frame->nb_samples;
                dst_nb_samples = av_rescale_rnd(a, c->sample_rate, renderContext.sampleRate, AV_ROUND_UP);
                //std::cout << a << " , " << c->sample_rate << " , " << renderContext.sampleRate << " , " << frame->nb_samples << std::endl;
                std::cout << dst_nb_samples << " == " << ost->frame->nb_samples << " | " << std::endl;
                dst_nb_samples = ost->frame->nb_samples < dst_nb_samples ? ost->frame->nb_samples : dst_nb_samples;
                //av_assert0(dst_nb_samples == frame->nb_samples);

            /* when we pass a frame to the encoder, it may keep a reference to it
             * internally;
             * make sure we do not overwrite it here
             */
            ret = av_frame_make_writable(ost->frame);
            if (ret < 0)
                return ret;

            /* convert to destination format */
            ret = swr_convert(ost->swr_ctx,
                              ost->frame->data, dst_nb_samples,
                              (const uint8_t **)frame->data, frame->nb_samples);
            if (ret < 0) {
                std::cout << "[ERROR] Error while converting " << ret << std::endl;
                return ret;
            }
            frame = ost->frame;

			AVRational scale;
			scale.num = 1;
			scale.den = c->sample_rate;

            frame->pts = av_rescale_q(ost->samples_count, scale, c->time_base);
            ost->samples_count += dst_nb_samples;
        }

        ret = avcodec_encode_audio2(c, &pkt, frame, &got_packet);
        if (ret < 0) {
            std::cout << "[ERROR] Error encoding audio frame: " << ret << std::endl;
            return ret;
        }

        if (got_packet) {
            ret = write_frame(oc, &c->time_base, ost->st, &pkt);
            if (ret < 0) {
                std::cout << "[ERROR] Error while writing audio frame: " << ret << std::endl;
                return ret;
            }
            av_free_packet(&pkt);
        }

        return (frame || got_packet) ? 0 : 1;
    }

    /**************************************************************/
    /* video output */

    static AVFrame *alloc_picture(enum AVPixelFormat pix_fmt, int width, int height)
    {
        AVFrame *picture;
        int ret;

        picture = av_frame_alloc();
        if (!picture)
            return NULL;

        picture->format = pix_fmt;
        picture->width  = width;
        picture->height = height;

        /* allocate the buffers for the frame data */
        ret = av_frame_get_buffer(picture, 32);
        if (ret < 0) {
            std::cout << "[ERROR] Could not allocate frame data." << std::endl;
            return NULL;
        }

        return picture;
    }

    static int open_video(AVFormatContext *oc, AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg, FFmpegInitEncoderRequest* request, AVPixelFormat videoPixelFormat)
    {
        int ret;
        AVCodecContext *c = ost->enc;
        AVDictionary *opt = NULL;

        av_dict_copy(&opt, opt_arg, 0);
  
        if (request->videoPreset != NULL) {
          av_dict_set( &opt, "preset", request->videoPreset, 0 );
          std::cout << "Set preset " << request->videoPreset << std::endl;
        }

        ret = avcodec_open2(c, codec, &opt);
        av_dict_free(&opt);
        if (ret < 0) {
            std::cout << "[ERROR] Could not open video codec for " << request->fileName << ", statuscode: " << ret << std::endl;
            return -1;
        }

        /* allocate and init a re-usable frame */
        ost->frame = alloc_picture(videoPixelFormat, c->width, c->height);
        if (!ost->frame) {
            std::cout << "[ERROR] Could not allocate video frame" << std::endl;
            return -1;
        }

        /* If the output format is not YUV420P, then a temporary YUV420P
         * picture is needed too. It is then converted to the required
         * output format. */
        ost->tmp_frame = NULL;


        /* copy the stream parameters to the muxer */
        ret = avcodec_parameters_from_context(ost->st->codecpar, c);
        if (ret < 0) {
            std::cout << "[ERROR] Could not copy the stream parameters" << std::endl;
            return -1;
        }
        return 0;
    }

    static AVFrame *get_video_frame(OutputStream *ost, RenderFFMpegFrame* frame)
    {
        AVCodecContext *c = ost->enc;

        /* when we pass a frame to the encoder, it may keep a reference to it
         * internally; make sure we do not overwrite it here */
        if (av_frame_make_writable(ost->frame) < 0)
            return NULL;


        SwsContext * ctx = ost->sws_ctx;
        uint8_t * inData[1] = { frame->imageData };
        int inLinesize[1] = { 4*renderContext.actualWidth };
        sws_scale(ctx, inData, inLinesize, 0, renderContext.actualHeight, ost->frame->data, ost->frame->linesize);

        ost->frame->pts = ost->next_pts++;

        return ost->frame;
    }

    /*
     * encode one video frame and send it to the muxer
     * return 1 when encoding is finished, 0 otherwise
     */
    static int write_video_frame(AVFormatContext *oc, OutputStream *ost, AVFrame* frame)
    {
        int ret;
        AVCodecContext *c;
        int got_packet = 0;
        AVPacket pkt = { 0 };

        c = ost->enc;

        if (frame != NULL) {
            std::cout << "[DEBUG] Writing video frame " << ((double)frame->pts * c->time_base.num / c->time_base.den) << std::endl;
        }
        av_init_packet(&pkt);

        /* encode the image */
        ret = avcodec_encode_video2(c, &pkt, frame, &got_packet);
        if (ret < 0) {
            std::cout << "[ERROR] Error encoding video frame:" << std::endl;
            return -1;
        }

        if (got_packet) {
            ret = write_frame(oc, &c->time_base, ost->st, &pkt);
            av_free_packet(&pkt);
        } else {
            ret = 0;
        }

        if (ret < 0) {
            std::cout << "[ERROR] Error while writing video frame" << std::endl;
            return -1;
        }

        return (frame || got_packet) ? 0 : 1;
    }

    static void close_stream(AVFormatContext *oc, OutputStream *ost)
    {
        avcodec_free_context(&ost->enc);
        av_frame_free(&ost->frame);
        av_frame_free(&ost->tmp_frame);
        sws_freeContext(ost->sws_ctx);
        swr_free(&ost->swr_ctx);
    }

    AVPixelFormat getPixelFormat(FFmpegInitEncoderRequest* request, AVCodec *videoCodec) {
        if (strcmp(request->videoPixelFormat, "default") == 0) {
          return videoCodec->pix_fmts[0];
        } else {
          return av_get_pix_fmt(request->videoPixelFormat);
        }
    }

    /**************************************************************/
    /* media file output */

    EXPORTED int initEncoder(FFmpegInitEncoderRequest* request) {
        std::cout << "[INFO] Initializing encoder " << request->actualWidth << " " << request->actualHeight << " " << request->fps << std::endl;
        av_register_all();
        AVFormatContext *oc;
        AVOutputFormat *fmt;
        OutputStream* video_st = new OutputStream;
        OutputStream* audio_st = new OutputStream;
        memset(video_st, 0, sizeof(OutputStream));
        memset(audio_st, 0, sizeof(OutputStream));
        AVCodec *audio_codec, *video_codec;
        int ret;
        int have_video = 0, have_audio = 0;
        int encode_video = 0, encode_audio = 0;
        AVDictionary *opt = NULL;

        int i;
        memset(&renderContext, 0, sizeof(RenderContext));


        const char* filename = request->fileName;

        /* allocate the output media context */
        avformat_alloc_output_context2(&oc, NULL, NULL, filename);
        if (!oc) {
            std::cout << "[INFO] Could not deduce output format from file extension: using MPEG." << std::endl;
            avformat_alloc_output_context2(&oc, NULL, "mpeg", filename);
        }
        if (!oc)
            return -1;

        if (request->metadata != NULL) {
          for (int i = 0; i < request->metadata->size; ++i) {
            const char* key = request->metadata->data[i].key;
            const char* value = request->metadata->data[i].value;
            av_dict_set(&oc->metadata, key, value, 0);
          }
        }

        fmt = oc->oformat;

        AVPixelFormat videoPixelFormat;
        /* Add the audio and video streams using the default format codecs
         * and initialize the codecs. */
        if (fmt->video_codec != AV_CODEC_ID_NONE && strcmp(request->videoCodec, "none") != 0) {
            if (strcmp(request->videoCodec, "default") != 0) {            
                fmt->video_codec = avcodec_find_encoder_by_name(request->videoCodec)->id;
            }


            videoPixelFormat = getPixelFormat(request, avcodec_find_encoder(fmt->video_codec));
            
            const char* videoPixelFormatStr = av_get_pix_fmt_name(videoPixelFormat);
            std::cout << "Using pixel format " << videoPixelFormatStr << std::endl;

            ret = add_video_stream(video_st, oc, &video_codec, fmt->video_codec, request, videoPixelFormat);
            if (ret < 0) {
                return ret;
            }
            have_video = 1;
            encode_video = 1;

            renderContext.videoPixelFormat =  videoPixelFormat;
        }
        if (fmt->audio_codec != AV_CODEC_ID_NONE && request->audioChannels > 0 && strcmp(request->audioCodec, "none") != 0) {
            if (strcmp(request->audioCodec, "default") != 0) {            
                fmt->audio_codec = avcodec_find_encoder_by_name(request->audioCodec)->id;
            }
            ret = add_audio_stream(audio_st, oc, &audio_codec, fmt->audio_codec, request);
            if (ret < 0) {
                return ret;
            }
            have_audio = 1;
            encode_audio = 1;
        }

        /* Now that all the parameters are set, we can open the audio and
         * video codecs and allocate the necessary encode buffers. */
        if (have_video) {
            ret = open_video(oc, video_codec, video_st, opt, request, videoPixelFormat);

            if (ret < 0) {
                return ret;
            }
        }

        if (have_audio) {
            ret = open_audio(oc, audio_codec, audio_st, opt, request);
            if (ret < 0) {
                return ret;
            }

        }
        
        av_dump_format(oc, 0, filename, 1);
        /* open the output file, if needed */
        if (!(fmt->flags & AVFMT_NOFILE)) {
            ret = avio_open(&oc->pb, filename, AVIO_FLAG_WRITE);
            if (ret < 0) {
                fprintf(stderr, "Could not open '%s': \n", filename);
                return -1;
            }
        }

        /* Write the stream header, if any. */
        ret = avformat_write_header(oc, &opt);
        if (ret < 0) {
            std::cout <<  "[ERROR] Error occurred when opening output file"<<std::endl;
            return -1;
        }
        renderContext.oc = oc;
        renderContext.fmt = fmt;
        renderContext.have_audio = have_audio;
        renderContext.have_video = have_video;
        renderContext.audio_st = audio_st;
        renderContext.video_st = video_st;
        renderContext.audio_codec = audio_codec;
        renderContext.video_codec = video_codec;
        renderContext.encode_video = encode_video;
        renderContext.encode_audio = encode_audio;
        renderContext.opt = opt;
        renderContext.bytesPerSample = audio_st->bytesPerSample;
        if (have_audio) {
          renderContext.audioBuffer = new unsigned char[audio_st->tmp_frame->nb_samples * renderContext.bytesPerSample * request->audioChannels];
          renderContext.audioChannels = request->audioChannels;
          renderContext.sampleRate = request->sampleRate;
        }
        renderContext.actualWidth = request->actualWidth;
        renderContext.actualHeight = request->actualHeight;
        renderContext.renderWidth = request->renderWidth;
        renderContext.renderHeight = request->renderHeight;

        return 0; // TODO: generate an index, so more renders can proceed in parallel
    }

    EXPORTED void clearEncoder(FFmpegClearEncoderRequest* request) {
        std::cout << "Closing encoder stream" << std::endl;

        int delayedFrames = 0;
        int responseCode = 0;
        if (renderContext.have_video) {
            while (responseCode == 0 && delayedFrames < 200) {
                std::cout << "Writing delayed video frames " << delayedFrames << std::endl;
                responseCode = write_video_frame(renderContext.oc, renderContext.video_st, NULL);
                ++delayedFrames;
            }
        }
        delayedFrames = 0;
        responseCode = 0;
        if (renderContext.have_audio) {
            while (responseCode == 0 && delayedFrames < 200) {
                std::cout << "Writing delayed audio frames " << delayedFrames << std::endl;
                responseCode = write_audio_frame(renderContext.oc, renderContext.audio_st, NULL);
                ++delayedFrames;
            }
        }
        av_write_trailer(renderContext.oc);

        /* Close each codec. */
        if (renderContext.have_video)
            close_stream(renderContext.oc, renderContext.video_st);
        if (renderContext.have_audio)
            close_stream(renderContext.oc, renderContext.audio_st);

        if (!(renderContext.fmt->flags & AVFMT_NOFILE))
            /* Close the output file. */
            avio_closep(&renderContext.oc->pb);

        /* free the stream */
        avformat_free_context(renderContext.oc);
    }

    EXPORTED int encodeFrames(FFmpegEncodeFrameRequest* request) {
            OutputStream* video_st = renderContext.video_st;
            OutputStream* audio_st = renderContext.audio_st;

            if (renderContext.encode_audio) {
              int nbSamples = renderContext.numberOfSamplesPerAudioFrame;

              for (int i = 0; i < request->frame->numberOfAudioSamples * renderContext.bytesPerSample * renderContext.audioChannels; ++i) {
                  renderContext.audioBuffer[renderContext.audioBufferPointer++] = request->frame->audioData[i];

                  if (renderContext.audioBufferPointer >= audio_st->tmp_frame->nb_samples * renderContext.bytesPerSample * renderContext.audioChannels) {
                      AVFrame *frame = get_audio_frame(audio_st, renderContext.audioBuffer);
                      renderContext.encode_audio = !write_audio_frame(renderContext.oc, audio_st, frame);
                      renderContext.audioBufferPointer = 0;
                  }
              }
            }
        

            if (renderContext.encode_video) {
              AVFrame *frame = get_video_frame(video_st, request->frame);
              if (frame == NULL) {
                return -1;
              }
              int ret = write_video_frame(renderContext.oc, video_st, frame);
              if (ret < 0) {
                return ret;
              }
              renderContext.encode_video = !ret;
            }
            return 0;
    }

    struct CodecInformation {
        const char* id;
        const char* longName;
    };

    struct QueryCodecRequest {
        CodecInformation* videoCodecs;
        CodecInformation* audioCodecs;

        int videoCodecNumber;
        int audioCodecNumber;
    };

    EXPORTED void queryCodecs(QueryCodecRequest* request)
    {
        av_register_all();

        request->videoCodecNumber = 0;
        request->audioCodecNumber = 0;
        AVCodec * codec = av_codec_next(NULL);
        while(codec != NULL)
        {
            if (av_codec_is_encoder(codec)) {
                //fprintf(stderr, "%s - %s\n", codec->long_name, codec->name);
                if (codec->type == AVMediaType::AVMEDIA_TYPE_AUDIO) {
                    request->audioCodecs[request->audioCodecNumber].id = codec->name;
                    request->audioCodecs[request->audioCodecNumber++].longName = codec->long_name;
                }
                if (codec->type == AVMediaType::AVMEDIA_TYPE_VIDEO) {
                    request->videoCodecs[request->videoCodecNumber].id = codec->name;
                    request->videoCodecs[request->videoCodecNumber++].longName = codec->long_name;
                }
            }

            codec = av_codec_next(codec);
        }

    }

    struct CodecExtraDataRequest {
        const char* fileName;
        const char* videoCodec;

        CodecInformation* availablePixelFormats;
        int availablePixelFormatNumber;
    };

    EXPORTED void queryCodecExtraData(CodecExtraDataRequest* request)
    {
        av_register_all();

        request->availablePixelFormatNumber = 0;

        av_register_all();
        AVFormatContext *oc;


        avformat_alloc_output_context2(&oc, NULL, NULL, request->fileName);
        if (!oc)
            return;

        AVOutputFormat* fmt = oc->oformat;

        if (fmt->video_codec != AV_CODEC_ID_NONE) {
            AVCodec* videoCodec;
            if (strcmp(request->videoCodec, "default") != 0) {
                videoCodec = avcodec_find_encoder_by_name(request->videoCodec);
            } else {
                videoCodec = avcodec_find_encoder(fmt->video_codec);
            }

            if (videoCodec == NULL) {
              return;
            }

            const AVPixelFormat* formats = videoCodec->pix_fmts;

            int i = 0;
            while (formats != NULL && formats[i] > -1) {
                const char* data = av_get_pix_fmt_name(formats[i]);
                request->availablePixelFormats[i].id = data;
                request->availablePixelFormats[i].longName = data;
                ++i;
            }
            request->availablePixelFormatNumber = i;
        }
    }


}

