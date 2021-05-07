#include <iostream>
#include "common.h"

extern "C" {
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>

const AVSampleFormat RESAMPLE_FORMAT = AV_SAMPLE_FMT_S32P;

    struct AVCodecAudioMetadataResponse {
        int sampleRate;
        int channels;
        int bytesPerSample;
        long long bitRate;
        long long lengthInMicroseconds;
    };

    AVFrame *alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                      uint64_t channel_layout,
                                      int sample_rate, int nb_samples)
    {
        AVFrame *frame = av_frame_alloc();
        int ret;

        if (!frame) {
            ERROR("Error allocating an audio frame");
            exit(1);
        }

        frame->format = sample_fmt;
        frame->channel_layout = channel_layout;
        frame->sample_rate = sample_rate;
        frame->nb_samples = nb_samples;

        if (nb_samples) {
            ret = av_frame_get_buffer(frame, 0);
            if (ret < 0) {
                ERROR("Error allocating an audio buffer for format format=" << sample_fmt << " " << ", layout=" 
                    << channel_layout << " " << " sample_rate=" << sample_rate << " " << " nb_samples=" << nb_samples
                    << " error_code=" << ret);
                exit(1);
            }
        }

        return frame;
    }

    bool doesNeedResampling(AVSampleFormat sampleFormat) {
      return sampleFormat != AV_SAMPLE_FMT_U8P && sampleFormat != AV_SAMPLE_FMT_S16P && sampleFormat != AV_SAMPLE_FMT_S32P;
    }

    EXPORTED AVCodecAudioMetadataResponse readMetadata(const char* path) {
        av_register_all();

        // get format from audio file
        AVFormatContext* format = avformat_alloc_context();
        if (avformat_open_input(&format, path, NULL, NULL) != 0) {
            ERROR("Could not open file " << path);
            return AVCodecAudioMetadataResponse();
        }
        if (avformat_find_stream_info(format, NULL) < 0) {
            ERROR("Could not retrieve stream info from file " << path);
            return AVCodecAudioMetadataResponse();
        }

        // Find the index of the first audio stream
        int stream_index =- 1;
        for (int i=0; i<format->nb_streams; i++) {
            if (format->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
                stream_index = i;
                break;
            }
        }
        if (stream_index == -1) {
            ERROR("Could not retrieve audio stream from file" << path);
            return AVCodecAudioMetadataResponse();
        }
        AVStream* stream = format->streams[stream_index];

        AVCodecContext* codec = stream->codec;
        if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
            ERROR("Failed to open decoder for stream " << stream_index << " in file " << path);
            return AVCodecAudioMetadataResponse();
        }
        AVCodec* pCodec = avcodec_find_decoder(codec->codec_id);

        AVCodecAudioMetadataResponse response;

        AVSampleFormat sampleFormat = codec->sample_fmt;

        response.channels = codec->channels;
        response.sampleRate = codec->sample_rate;
        response.lengthInMicroseconds = format->duration / (AV_TIME_BASE / 1000000);
        response.bitRate = codec->bit_rate;
        response.bytesPerSample = av_get_bytes_per_sample(sampleFormat);

        avcodec_close(codec);
        avformat_free_context(format);

        return response;
    }

    typedef struct {
        char* data;
    } FFMpegFrame;


    struct AVCodecAudioRequest {
        const char* path;

        int sampleRate;
        int bytesPerSample;

        long long startMicroseconds;
        long long bufferSize;

        long long numberOfChannels;
        FFMpegFrame* channels;
    };

    EXPORTED int readAudio(AVCodecAudioRequest* request) {
        //DEBUG("Audio size: " << request->bufferSize << " " << request->path << " " << request->startMicroseconds << " " << request->numberOfChannels);

        av_register_all();

        // get format from audio file
        AVFormatContext* format = avformat_alloc_context();
        if (avformat_open_input(&format, request->path, NULL, NULL) != 0) {
            ERROR("Could not open file " << request->path);
            return -1;
        }
        if (avformat_find_stream_info(format, NULL) < 0) {
            ERROR("Could not retrieve stream info from file " << request->path);
            return -1;
        }

        // Find the index of the first audio stream
        int stream_index =- 1;
        for (int i=0; i<format->nb_streams; i++) {
            if (format->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
                stream_index = i;
                break;
            }
        }
        //DEBUG("Stream index: " << stream_index);
        if (stream_index == -1) {
            ERROR("Could not retrieve audio stream from file " << request->path);
            return -1;
        }
        AVStream* stream = format->streams[stream_index];

        // find & open codec
        AVCodecContext* codec = stream->codec;
        AVCodec* pCodec = avcodec_find_decoder(codec->codec_id);
        if (avcodec_open2(codec, pCodec, NULL) < 0) {
            ERROR("Failed to open decoder for stream " << stream_index << " in file " << request->path);
            return -1;
        }

        bool needsResampling = false;


        SwrContext* swrContext = NULL;
        AVFrame* tmp_frame = NULL;

        AVSampleFormat sampleFormat = codec->sample_fmt;

        int codecBytes = av_get_bytes_per_sample(sampleFormat);


        int64_t outputChannelLayout = av_get_default_channel_layout((int)request->numberOfChannels);

        if (doesNeedResampling(sampleFormat) || codec->sample_rate != request->sampleRate || codecBytes != request->bytesPerSample) {
          needsResampling = true;
          swrContext = swr_alloc();
          if (!swrContext) {
              ERROR("Could not allocate resampler context");
              exit(1);
          }

          AVSampleFormat outputSampleFormat = sampleFormat;
          if (request->bytesPerSample != codecBytes || doesNeedResampling(sampleFormat)) {
               if (request->bytesPerSample == 1) {
                  outputSampleFormat = AV_SAMPLE_FMT_U8P;
               } else if (request->bytesPerSample == 2) {
                  outputSampleFormat = AV_SAMPLE_FMT_S16P;
               } else if (request->bytesPerSample == 4) {
                  outputSampleFormat = AV_SAMPLE_FMT_S32P;
               } else {
                  ERROR("Unexpected sample rescale " << request->bytesPerSample);
               }
          }

          DEBUG("Resampling audio to " << request->sampleRate << " " << codec->sample_fmt << " " <<  outputSampleFormat << " bytes=" << av_get_bytes_per_sample(outputSampleFormat));
          
          /* set options */
          av_opt_set_int       (swrContext, "in_channel_count",   codec->channels,       0);
          av_opt_set_int       (swrContext, "in_sample_rate",     codec->sample_rate,    0);
          av_opt_set_sample_fmt(swrContext, "in_sample_fmt",      codec->sample_fmt, 0);
          av_opt_set_channel_layout(swrContext, "in_channel_layout",  codec->channel_layout, 0);
          av_opt_set_int       (swrContext, "out_channel_count",  (int)request->numberOfChannels,       0);
          av_opt_set_int       (swrContext, "out_sample_rate",    request->sampleRate,    0);
          av_opt_set_sample_fmt(swrContext, "out_sample_fmt",     outputSampleFormat,     0);
          av_opt_set_channel_layout(swrContext, "out_channel_layout", outputChannelLayout,  0);

          //DEBUG("Initializing SWR");
          if ((swr_init(swrContext)) < 0) {
              ERROR("Failed to initialize the resampling context");
              exit(1);
          }

          sampleFormat = outputSampleFormat;
        } 
        int sampleSize = av_get_bytes_per_sample(sampleFormat);


        // prepare to read data
        AVPacket packet;
        av_init_packet(&packet);
        AVFrame* frame = av_frame_alloc();
        if (!frame) {
            ERROR("Error allocating the frame");
            return -1;
        }

        bool isPlanar = av_sample_fmt_is_planar(sampleFormat);

        int64_t seek_target = request->startMicroseconds * (AV_TIME_BASE / 1000000);


		    // AV_TIME_BASE_Q   (AVRational){1, AV_TIME_BASE} -> VC++ causes error, so redefine without braces
        AVRational timeBaseQ;
        timeBaseQ.num = 1;
        timeBaseQ.den = AV_TIME_BASE;
		
        seek_target= av_rescale_q(seek_target, timeBaseQ, stream->time_base);
        av_seek_frame(format, stream_index, seek_target, AVSEEK_FLAG_BACKWARD);
        avcodec_flush_buffers(codec);

        DEBUG("SEEKING to " << request->startMicroseconds << " " << seek_target);

        int totalNumberOfSamplesRead = 0;
        bool running = true;
        int returnStatusCode;
        while ((returnStatusCode=av_read_frame(format, &packet)) >= 0 && running) {
            if(packet.stream_index==stream_index) {
                int gotFrame;
                int readBytes = avcodec_decode_audio4(codec, frame, &gotFrame, &packet);
                if (readBytes < 0) {
                    ERROR("Cannot decode package");
                    break;
                }
                //DEBUG("Read packet " << packet.stream_index << " (" << (av_frame_get_best_effort_timestamp(frame) * av_q2d(stream->time_base)) << " " << totalNumberOfSamplesRead << ")");
                if (!gotFrame) {
                    continue;
                }
                if (packet.pts < seek_target) {
                  DEBUG("Skipping package " << packet.pts);
                  continue;
                }
                //DEBUG("Got frame");

                AVFrame* frameToUse = frame;
                int actualNumberOfOutputSamples = frame->nb_samples;
                if (needsResampling) {

                  int resampledCount = (int)ceil(frame->nb_samples * ((double)request->sampleRate / codec->sample_rate));

                  //DEBUG("Allocating " << resampledCount);

                  tmp_frame = alloc_audio_frame(sampleFormat, outputChannelLayout,
                                       request->sampleRate, resampledCount);

                  actualNumberOfOutputSamples = swr_convert(swrContext,
                                ( uint8_t **)tmp_frame->data, tmp_frame->nb_samples,
                                (const uint8_t **)frame->data, frame->nb_samples);

                  frameToUse = tmp_frame;
                }

                if (isPlanar) {
                    //DEBUG("Before copy planar: " << readBytes << " " << request->sampleRate << " " << codec->sample_rate << " " << frame->nb_samples << " " <<  frameToUse->nb_samples << " " << sampleSize);
                    int actuallyWrittenSamples = 0;
                    for (int channel = 0; channel < request->numberOfChannels; ++channel) {
                        for (int i = 0; i < actualNumberOfOutputSamples; ++i) {
                            for (int k = 0; k < sampleSize; ++k) {
                                int toUpdate = totalNumberOfSamplesRead + i * sampleSize + k;
                                if (toUpdate >= request->bufferSize) {
                                    running = false;
                                  //  DEBUG("Buffer is full 1");
                                    break;
                                }

                                request->channels[channel].data[toUpdate] = frameToUse->data[channel][i * sampleSize + k];
                            }
                            if (running && channel==0) actuallyWrittenSamples++;
                        }
                        //DEBUG("\nChannel done" << channel);
                    }
                    totalNumberOfSamplesRead += actuallyWrittenSamples * sampleSize;
                } else {
                    // TODO: currently this branch is unused, when fixed add non-planar format back into resampling needed method
                    // like: sampleFormat != AV_SAMPLE_FMT_U8 && sampleFormat != AV_SAMPLE_FMT_S16 && sampleFormat != AV_SAMPLE_FMT_S32
                    DEBUG("Not planar");
                    for (int i = 0, j = 0; i < frameToUse->nb_samples; ++i, ++j) {
                        for (int channel = 0; channel < request->numberOfChannels; ++channel) {
                            for (int k = 0; k < sampleSize; ++k) {
                                int outputBufferIndex = j * sampleSize + k;
                                if (totalNumberOfSamplesRead >= request->bufferSize) {
                                    running = false;
                                 //   DEBUG("Buffer is full 2");
                                    break;
                                }
                                // TODO: this only supports single channel
                                request->channels[channel].data[totalNumberOfSamplesRead++] = frameToUse->data[0][request->numberOfChannels * sampleSize * i + channel *sampleSize + k];
                            }
                        }
                    }
                }
                if (tmp_frame) {
                  av_frame_free(&tmp_frame);
                  tmp_frame = NULL;
                }
            }
            av_free_packet(&packet);
        }

        if (returnStatusCode < 0) {
          char* errorStr = new char[1000];
          av_make_error_string(errorStr, 1000, returnStatusCode);
          ERROR("Audio decode ended with error, statusCode=" << returnStatusCode << " (" << errorStr << ")");
          delete[] errorStr;
        }


        // clean up
        av_frame_free(&frame);
        if (needsResampling) {
          swr_free(&swrContext);
        }
        if (tmp_frame) {
          av_frame_free(&tmp_frame);
        }
        avcodec_close(codec);
        avformat_close_input(&format);

        return totalNumberOfSamplesRead;

    }




/**
    int main() {
        double* data = NULL;
        int size = 0;
        AVCodecAudioRequest request;
        request.path = "/home/black/Documents/pic_tetris.mp4";
        request.numberOfChannels = 2;
        request.channels = new FFMpegFrame[2];

        FFMpegFrame frame1;
        frame1.data = new char[100000];
        FFMpegFrame frame2;
        frame2.data = new char[100000];

        request.channels[0] = frame1;
        request.channels[1] = frame2;
        request.startMicroseconds = 0;
        request.bufferSize = 100000;

        int readSamples = decode_audio_file(&request);
        DEBUG(readSamples);
        for (int i = 0; i < 50000; ++i) {
         //   DEBUG((int)request.channels[1].data[i] << " ";
        }
        DEBUG(std::endl;
    }
    */
}

