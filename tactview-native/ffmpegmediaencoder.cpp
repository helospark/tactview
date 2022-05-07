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
    #include <libavcodec/avcodec.h>

    #define SCALE_FLAGS SWS_BICUBIC

    // Fix for: error C4576: a parenthesized type followed by an initializer list is a non-standard explicit type conversion syntax
    #define AV_TIME_BASE_Q AVRational {1, AV_TIME_BASE}

    struct ChapterInformation {
        long long timeInMicroseconds;
        char* name;
    };

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

        int numberOfChapters;
        long long totalLengthInMicroseconds;
        ChapterInformation* chapters;
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


        AVPacket *tmp_pkt;

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
        const AVOutputFormat *fmt;
        AVFormatContext *oc;
        const AVCodec *audio_codec, *video_codec;
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


    std::string av_err2string(int errnum) {
        char str[AV_ERROR_MAX_STRING_SIZE];
        return av_make_error_string(str, AV_ERROR_MAX_STRING_SIZE, errnum);
    }


    static int write_frame(AVFormatContext *fmt_ctx, AVCodecContext *c,
                        AVStream *st, AVFrame *frame, AVPacket *pkt)
    {
        int ret;

        // send the frame to the encoder
        ret = avcodec_send_frame(c, frame);
        if (ret < 0) {
            fprintf(stderr, "Error sending a frame to the encoder: %s\n",
                    av_err2string(ret));
            exit(1);
        }

        while (ret >= 0) {
            ret = avcodec_receive_packet(c, pkt);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
                break;
            else if (ret < 0) {
                fprintf(stderr, "Error encoding a frame: %s\n", av_err2string(ret));
                exit(1);
            }

            /* rescale output packet timestamp values from codec to stream timebase */
            av_packet_rescale_ts(pkt, c->time_base, st->time_base);
            pkt->stream_index = st->index;

            /* Write the compressed frame to the media file. */

            ret = av_interleaved_write_frame(fmt_ctx, pkt);
            /* pkt is now blank (av_interleaved_write_frame() takes ownership of
            * its contents and resets pkt), so that no unreferencing is necessary.
            * This would be different if one used av_write_frame(). */
            if (ret < 0) {
                fprintf(stderr, "Error while writing output packet: %s\n", av_err2string(ret));
                exit(1);
            }
        }

        return ret == AVERROR_EOF ? 1 : 0;
    }


    /* Add an output stream. */
    static int add_video_stream(OutputStream *ost, AVFormatContext *oc,
                           const AVCodec **codec,
                           enum AVCodecID codec_id,
                           FFmpegInitEncoderRequest* request,
                           AVPixelFormat videoPixelFormat)
    {
        AVCodecContext *c;
        int i;

        /* find the encoder */
        *codec = avcodec_find_encoder(codec_id);
        if (!(*codec)) {
            ERROR("[ERROR] Could not find encoder for '" << avcodec_get_name(codec_id) << "'");
            return -1;
        }

        
        ost->tmp_pkt = av_packet_alloc();
        if (!ost->tmp_pkt) {
            fprintf(stderr, "Could not allocate AVPacket\n");
            exit(1);
        }

        ost->st = avformat_new_stream(oc, NULL);
        if (!ost->st) {
            ERROR("[ERROR] Could not allocate stream");
            return -1;
        }
        ost->st->id = oc->nb_streams-1;
        c = avcodec_alloc_context3(*codec);
        if (!c) {
            ERROR("[ERROR] Could not alloc an encoding context");
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

        DEBUG("Encoder FPS: " << ost->st->time_base.num << " / " << ost->st->time_base.den);

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
                           const AVCodec **codec,
                           enum AVCodecID codec_id,
                           FFmpegInitEncoderRequest* request)
    {
        AVCodecContext *c;
        int i;

        /* find the encoder */
        *codec = avcodec_find_encoder(codec_id);
        if (!(*codec)) {
            ERROR("[ERROR] Could not find encoder for '" << avcodec_get_name(codec_id) << "'");
            return -1;
        }

        ost->tmp_pkt = av_packet_alloc();
        if (!ost->tmp_pkt) {
            fprintf(stderr, "Could not allocate AVPacket\n");
            exit(1);
        }

        ost->st = avformat_new_stream(oc, NULL);
        if (!ost->st) {
            ERROR("[ERROR] Could not allocate stream");
            return -1;
        }
        ost->st->id = oc->nb_streams-1;
        c = avcodec_alloc_context3(*codec);
        if (!c) {
            ERROR("[ERROR] Could not alloc an encoding context");
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
                    DEBUG("Supported channel layout " << (*codec)->channel_layouts[i] << " " << av_get_channel_layout_nb_channels((*codec)->channel_layouts[i])); 
                    c->channel_layout = expectedChannelLayout;
                    break;
                }
            }
        } else {
            DEBUG("Channel layouts are unknown, falling back to stereo");
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
            ERROR("[ERROR] Error allocating an audio frame");
            return NULL;
        }

        frame->format = sample_fmt;
        frame->channel_layout = channel_layout;
        frame->sample_rate = sample_rate;
        frame->nb_samples = nb_samples;

        if (nb_samples) {
            ret = av_frame_get_buffer(frame, 0);
            if (ret < 0) {
                ERROR("[ERROR] Error allocating an audio buffer");
                return NULL;
            }
        }

        return frame;
    }

    static int open_audio(AVFormatContext *oc, const AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg, FFmpegInitEncoderRequest* request)
    {
        AVCodecContext *c;
        int nb_samples;
        int ret;
        AVDictionary *opt = NULL;

        c = ost->enc;

        c->thread_count = 0; // 0 = automatic
        c->thread_type = (FF_THREAD_FRAME | FF_THREAD_SLICE);

        /* open it */
        av_dict_copy(&opt, opt_arg, 0);
        ret = avcodec_open2(c, codec, &opt);
        av_dict_free(&opt);
        if (ret < 0) {
            ERROR("[ERROR] Could not open audio codec:");
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
                ERROR("[ERROR] Could not allocate resampler context");
                return -1;
            }

            DEBUG("SWR " << c->sample_rate << " " << request->sampleRate << " " << request->audioChannels);

            /* set options */
            av_opt_set_int       (ost->swr_ctx, "in_channel_count",   request->audioChannels,       0);
            av_opt_set_int       (ost->swr_ctx, "in_sample_rate",     request->sampleRate,    0);
            av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt",      ost->sampleFormat, 0);
            av_opt_set_int       (ost->swr_ctx, "out_channel_count",  c->channels,       0);
            av_opt_set_int       (ost->swr_ctx, "out_sample_rate",    c->sample_rate,    0);
            av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt",     c->sample_fmt,     0);

            /* initialize the resampling context */
            if ((ret = swr_init(ost->swr_ctx)) < 0) {
                ERROR("[ERROR] Failed to initialize the resampling context");
                return -1;
            }
            return 0;
    }

    static AVFrame *get_audio_frame(OutputStream *ost, unsigned char* dataStart)
    {
        AVFrame *frame = ost->tmp_frame;
        int j, i, v;
        unsigned char *q = (unsigned char*)frame->data[0];

        memcpy(q, dataStart, frame->nb_samples * renderContext.audioChannels * ost->bytesPerSample);

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
        int ret;
        int dst_nb_samples;

        c = ost->enc;

        if (frame) {
            /* convert samples from native format to destination codec format, using the resampler */
            /* compute destination number of samples */
            dst_nb_samples = av_rescale_rnd(swr_get_delay(ost->swr_ctx, c->sample_rate) + frame->nb_samples,
                                            c->sample_rate, c->sample_rate, AV_ROUND_UP);
            av_assert0(dst_nb_samples == frame->nb_samples);

            /* when we pass a frame to the encoder, it may keep a reference to it
            * internally;
            * make sure we do not overwrite it here
            */
            ret = av_frame_make_writable(ost->frame);
            if (ret < 0)
                exit(1);

            /* convert to destination format */
            ret = swr_convert(ost->swr_ctx,
                            ost->frame->data, dst_nb_samples,
                            (const uint8_t **)frame->data, frame->nb_samples);
            if (ret < 0) {
                fprintf(stderr, "Error while converting\n");
                exit(1);
            }
            frame = ost->frame;

            frame->pts = av_rescale_q(ost->samples_count, (AVRational){1, c->sample_rate}, c->time_base);
            ost->samples_count += dst_nb_samples;
        }

        return write_frame(oc, c, ost->st, frame, ost->tmp_pkt);
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
            ERROR("[ERROR] Could not allocate frame data.");
            return NULL;
        }

        return picture;
    }

    static int open_video(AVFormatContext *oc, const AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg, FFmpegInitEncoderRequest* request, AVPixelFormat videoPixelFormat)
    {
        int ret;
        AVCodecContext *c = ost->enc;
        AVDictionary *opt = NULL;

        av_dict_copy(&opt, opt_arg, 0);
  
        if (request->videoPreset != NULL) {
          av_dict_set( &opt, "preset", request->videoPreset, 0 );
          DEBUG("Set preset " << request->videoPreset);
        }

        c->thread_count = 0; // 0 = automatic
        c->thread_type = (FF_THREAD_FRAME | FF_THREAD_SLICE);


        ret = avcodec_open2(c, codec, &opt);
        av_dict_free(&opt);
        if (ret < 0) {
            ERROR("[ERROR] Could not open video codec for " << request->fileName << ", statuscode: " << ret);
            return -1;
        }

        /* allocate and init a re-usable frame */
        ost->frame = alloc_picture(videoPixelFormat, c->width, c->height);
        if (!ost->frame) {
            ERROR("[ERROR] Could not allocate video frame");
            return -1;
        }

        /* If the output format is not YUV420P, then a temporary YUV420P
         * picture is needed too. It is then converted to the required
         * output format. */
        ost->tmp_frame = NULL;


        /* copy the stream parameters to the muxer */
        ret = avcodec_parameters_from_context(ost->st->codecpar, c);
        if (ret < 0) {
            ERROR("[ERROR] Could not copy the stream parameters");
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
        return write_frame(oc, ost->enc, ost->st, frame, ost->tmp_pkt);
    }

    static void close_stream(AVFormatContext *oc, OutputStream *ost)
    {
        avcodec_free_context(&ost->enc);
        av_frame_free(&ost->frame);
        av_frame_free(&ost->tmp_frame);
        sws_freeContext(ost->sws_ctx);
        swr_free(&ost->swr_ctx);
    }

    AVPixelFormat getPixelFormat(FFmpegInitEncoderRequest* request, const AVCodec *videoCodec) {
        if (strcmp(request->videoPixelFormat, "default") == 0) {
          return videoCodec->pix_fmts[0];
        } else {
          return av_get_pix_fmt(request->videoPixelFormat);
        }
    }

    /**************************************************************/
    /* media file output */

    EXPORTED int initEncoder(FFmpegInitEncoderRequest* request) {
        INFO("Initializing encoder: width=" << request->actualWidth << " height=" << request->actualHeight << " fps=" << request->fps);

        AVFormatContext *oc;
        const AVOutputFormat *fmt;
        OutputStream* video_st = new OutputStream;
        OutputStream* audio_st = new OutputStream;
        memset(video_st, 0, sizeof(OutputStream));
        memset(audio_st, 0, sizeof(OutputStream));
        const AVCodec *audio_codec, *video_codec;
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
            WARN("Could not deduce output format from file extension: using MPEG");
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

        if (request->numberOfChapters != 0) {
            DEBUG("Adding " << request->numberOfChapters << " chapters");
            AVChapter** chapters = (AVChapter**)av_malloc(request->numberOfChapters*sizeof(AVChapter*));
            long long previous = 0;
            for (int i = 0; i < request->numberOfChapters; ++i) {
                chapters[i] = (AVChapter*)av_malloc(sizeof(AVChapter));
                chapters[i]->id = i;
                chapters[i]->start = request->chapters[i].timeInMicroseconds;
                chapters[i]->end = i < (request->numberOfChapters - 1) ? request->chapters[i+1].timeInMicroseconds : request->totalLengthInMicroseconds;
                chapters[i]->time_base = AV_TIME_BASE_Q;

                AVDictionary *dictionary = NULL;
                av_dict_set(&dictionary, "title", request->chapters[i].name, 0);

                chapters[i]->metadata = dictionary;
            }
            oc->chapters = chapters;
            oc->nb_chapters = request->numberOfChapters;
        }

        fmt = oc->oformat;

        AVPixelFormat videoPixelFormat;
        /* Add the audio and video streams using the default format codecs
         * and initialize the codecs. */
        DEBUG("Video fmt->video_codec=" << fmt->video_codec << " request->videoCodec=" << request->videoCodec);
        if (fmt->video_codec != AV_CODEC_ID_NONE && strcmp(request->videoCodec, "none") != 0) {
            AVCodecID codec = fmt->video_codec;
            if (strcmp(request->videoCodec, "default") != 0) {            
                codec = avcodec_find_encoder_by_name(request->videoCodec)->id;
            }


            videoPixelFormat = getPixelFormat(request, avcodec_find_encoder(codec));
            
            const char* videoPixelFormatStr = av_get_pix_fmt_name(videoPixelFormat);
            DEBUG("Using pixel format " << videoPixelFormatStr);

            ret = add_video_stream(video_st, oc, &video_codec, codec, request, videoPixelFormat);
            if (ret < 0) {
                return ret;
            }
            have_video = 1;
            encode_video = 1;

            renderContext.videoPixelFormat =  videoPixelFormat;
        }
        if (fmt->audio_codec != AV_CODEC_ID_NONE && request->audioChannels > 0 && strcmp(request->audioCodec, "none") != 0) {
            AVCodecID codec = fmt->audio_codec;
            if (strcmp(request->audioCodec, "default") != 0) {            
                codec = avcodec_find_encoder_by_name(request->audioCodec)->id;
            }
            ret = add_audio_stream(audio_st, oc, &audio_codec, codec, request);
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
                ERROR("Could not open " << filename);
                return -1;
            }
        }

        /* Write the stream header, if any. */
        ret = avformat_write_header(oc, &opt);
        if (ret < 0) {
            ERROR("Error occurred when opening output file " << filename);
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
        INFO("Closing encoder stream");

        int delayedFrames = 0;
        int responseCode = 0;
        if (renderContext.have_video) {
            while (responseCode == 0 && delayedFrames < 200) {
                DEBUG("Writing delayed video frames " << delayedFrames);
                responseCode = write_video_frame(renderContext.oc, renderContext.video_st, NULL);
                ++delayedFrames;
            }
        }
        delayedFrames = 0;
        responseCode = 0;
        if (renderContext.have_audio) {
            while (responseCode == 0 && delayedFrames < 200) {
                DEBUG("Writing delayed audio frames " << delayedFrames);
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
        

            if (renderContext.encode_video && request->frame->imageData != NULL) {
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

        request->videoCodecNumber = 0;
        request->audioCodecNumber = 0;
        const AVCodec * codec;
        void* i = 0;
        while ((codec = av_codec_iterate(&i))) {
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
        request->availablePixelFormatNumber = 0;
        AVFormatContext *oc;


        avformat_alloc_output_context2(&oc, NULL, NULL, request->fileName);
        if (!oc)
            return;

        const AVOutputFormat* fmt = oc->oformat;

        if (fmt->video_codec != AV_CODEC_ID_NONE) {
            const AVCodec* videoCodec;
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

#ifdef DEBUG_BUILD
int main() {
        const int AUDIO_SAMPLE_PER_SEC = 8000;
        const int WIDTH = 400;
        const int HEIGHT = 300;
        const int FPS = 30;

        FFmpegInitEncoderRequest initRequest;
        initRequest.fileName = "/tmp/testout.mp4";
        initRequest.actualWidth = WIDTH;
        initRequest.actualHeight = HEIGHT;
        initRequest.renderWidth = WIDTH;
        initRequest.renderHeight = HEIGHT;
        initRequest.fps = FPS;

        initRequest.audioChannels = 1;
        initRequest.bytesPerSample = 1;
        initRequest.sampleRate = AUDIO_SAMPLE_PER_SEC;

        initRequest.videoBitRate = 3200000;
        initRequest.audioBitRate = 192000;
        initRequest.audioSampleRate = AUDIO_SAMPLE_PER_SEC;
        initRequest.videoCodec = "default";
        initRequest.audioCodec = "default";
        initRequest.videoPixelFormat = "default";
        initRequest.videoPreset = "medium";

        initRequest.metadata = NULL;

        initRequest.numberOfChapters = 0;
        initRequest.totalLengthInMicroseconds = 10000;
        initRequest.chapters = NULL;

        int encoderIndex = initEncoder(&initRequest);

        double seconds = 0.0;
        double frequency = 600.0;

        for (int i = 0; i < 30 * 10; ++i) {
            RenderFFMpegFrame frame;
            int audioSamples = AUDIO_SAMPLE_PER_SEC / FPS;
            frame.numberOfAudioSamples = audioSamples;
            frame.audioData = new unsigned char[audioSamples];
            
            double sampleSeconds = seconds;
            for (int i = 0; i < audioSamples; ++i) {
                double sampleSecondChange = 1.0 / (FPS * audioSamples);
                sampleSeconds += sampleSecondChange;
                frame.audioData[i] = sin(sampleSeconds * frequency * 2.0 * M_PI) * 255 / 2 + 128;
            }
            
            frame.imageData = new unsigned char[WIDTH * HEIGHT * 4];

            for (int y = 0; y < HEIGHT; ++y) {
                for (int x = 0; x < WIDTH; ++x) {
                    frame.imageData[y * WIDTH * 4 + x * 4 + 0] = (unsigned char)(std::fabs(std::fmod(seconds * 0.1, 1.0)) * 255);
                    frame.imageData[y * WIDTH * 4 + x * 4 + 1] = (unsigned char)(std::fabs(std::fmod(seconds * 0.1 - 0.5, 1.0)) * 255);
                    frame.imageData[y * WIDTH * 4 + x * 4 + 2] = (unsigned char)(std::fabs(std::fmod(seconds * 0.1 + 0.3, 1.0)) * 255);
                    frame.imageData[y * WIDTH * 4 + x * 4 + 3] = (unsigned char) 255;
                }    
            }

            // draw a black + to left corner
            for (int i = 0; i < 40; ++i) {
                int y = 10 + i;
                int x = 50;
                frame.imageData[y * WIDTH * 4 + x * 4 + 0] = 0;
                frame.imageData[y * WIDTH * 4 + x * 4 + 1] = 0;
                frame.imageData[y * WIDTH * 4 + x * 4 + 2] = 0;
            }
            for (int i = 0; i < 40; ++i) {
                int y = 30;
                int x = 30 + i;
                frame.imageData[y * WIDTH * 4 + x * 4 + 0] = 0;
                frame.imageData[y * WIDTH * 4 + x * 4 + 1] = 0;
                frame.imageData[y * WIDTH * 4 + x * 4 + 2] = 0;
            }

            FFmpegEncodeFrameRequest frameRequest;
            frameRequest.encoderIndex = encoderIndex;
            frameRequest.startFrameIndex = i;
            frameRequest.frame = &frame;

            encodeFrames(&frameRequest);

            delete[] frameRequest.frame->imageData;
            delete[] frameRequest.frame->audioData;
            seconds += 1.0 / FPS;
        }

        FFmpegClearEncoderRequest clearRequest;
        clearRequest.encoderIndex = encoderIndex;

        clearEncoder(&clearRequest);

}
#endif