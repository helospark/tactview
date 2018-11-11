#include <iostream>


extern "C" {
#include <stdio.h>
#include <stdlib.h>

#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>


    struct AVCodecAudioMetadataResponse {
        int sampleRate;
        int channels;
        int bytesPerSample;
        long lengthInMicroseconds;

        AVCodecAudioMetadataResponse() {
            this->channels = 0;
            this->bytesPerSample = 0;
            this->lengthInMicroseconds = 0;
            this->sampleRate = 0;
        }
    };

    AVCodecAudioMetadataResponse readMetadata(const char* path) {
        av_register_all();

        // get format from audio file
        AVFormatContext* format = avformat_alloc_context();
        if (avformat_open_input(&format, path, NULL, NULL) != 0) {
            fprintf(stderr, "Could not open file '%s'\n", path);
            return AVCodecAudioMetadataResponse();
        }
        if (avformat_find_stream_info(format, NULL) < 0) {
            fprintf(stderr, "Could not retrieve stream info from file '%s'\n", path);
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
            fprintf(stderr, "Could not retrieve audio stream from file '%s'\n", path);
            return AVCodecAudioMetadataResponse();
        }
        AVStream* stream = format->streams[stream_index];

        AVCodecContext* codec = stream->codec;
        if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
            fprintf(stderr, "Failed to open decoder for stream #%u in file '%s'\n", stream_index, path);
            return AVCodecAudioMetadataResponse();
        }
        AVCodec* pCodec = avcodec_find_decoder(codec->codec_id);

        AVCodecAudioMetadataResponse response;

        response.channels = codec->channels;
        response.sampleRate = codec->sample_rate;
        response.lengthInMicroseconds = format->duration / (AV_TIME_BASE / 1000000);
        response.bytesPerSample = av_get_bytes_per_sample(pCodec->sample_fmts[0]);

        avcodec_close(codec);
        avformat_free_context(format);

        return response;
    }

    typedef struct {
        char* data;
    } FFMpegFrame;


    struct AVCodecAudioRequest {
        const char* path;

        long startMicroseconds;
        long bufferSize;

        long numberOfChannels;
        FFMpegFrame* channels;
    };

    int readAudio(AVCodecAudioRequest* request) {
        std::cout << "Audio size: " << request->bufferSize << " " << request->path << " " << request->startMicroseconds << " " << request->numberOfChannels << std::endl;

        av_register_all();

        // get format from audio file
        AVFormatContext* format = avformat_alloc_context();
        if (avformat_open_input(&format, request->path, NULL, NULL) != 0) {
            fprintf(stderr, "Could not open file '%s'\n", request->path);
            return -1;
        }
        if (avformat_find_stream_info(format, NULL) < 0) {
            fprintf(stderr, "Could not retrieve stream info from file '%s'\n", request->path);
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
        std::cout << "Stream index: " << stream_index << std::endl;
        if (stream_index == -1) {
            fprintf(stderr, "Could not retrieve audio stream from file '%s'\n", request->path);
            return -1;
        }
        AVStream* stream = format->streams[stream_index];

        // find & open codec
        AVCodecContext* codec = stream->codec;
        AVCodec* pCodec = avcodec_find_decoder(codec->codec_id);
        if (avcodec_open2(codec, pCodec, NULL) < 0) {
            fprintf(stderr, "Failed to open decoder for stream #%u in file '%s'\n", stream_index, request->path);
            return -1;
        }
        int sampleSize = av_get_bytes_per_sample(codec->sample_fmt);


        // prepare resampler

        std::cout << "sample format: " << codec->sample_fmt << std::endl;

        // prepare to read data
        AVPacket packet;
        av_init_packet(&packet);
        AVFrame* frame = av_frame_alloc();
        if (!frame) {
            fprintf(stderr, "Error allocating the frame\n");
            return -1;
        }
        std::cout << "Data frame init done: " << std::endl;

        bool isPlanar = av_sample_fmt_is_planar(codec->sample_fmt);


        int64_t seek_target = request->startMicroseconds * (AV_TIME_BASE / 1000000);
        seek_target= av_rescale_q(seek_target, AV_TIME_BASE_Q, stream->time_base);
        av_seek_frame(format, stream_index, seek_target, AVSEEK_FLAG_BACKWARD);

        int totalNumberOfSamplesRead = 0;
        bool running = true;
        std::cout << "Before while: " << std::endl;
        while (av_read_frame(format, &packet) >= 0 && running) {
            std::cout << "Read stream: " << packet.stream_index << std::endl;
            if(packet.stream_index==stream_index) {
                int gotFrame;
                //std::cout << "Before continue: " << std::endl;
                if (avcodec_decode_audio4(codec, frame, &gotFrame, &packet) < 0) {
                    break;
                }
                if (!gotFrame) {
                    continue;
                }
                std::cout << "Got frame " << frame->nb_samples  << " " << isPlanar  << std::endl;


                if (isPlanar) {
                    for (int channel = 0; channel < request->numberOfChannels; ++channel) {
                        int startIndex = channel * frame->nb_samples * sampleSize;
                        for (int i = startIndex, j = 0; i < startIndex + frame->nb_samples; ++i, ++j) {
                            for (int k = 0; k < sampleSize; ++k) {
                                if (totalNumberOfSamplesRead >= request->bufferSize) {
                                    running = false;
                                    break;
                                }
                                std::cout << frame->data[0][i * sampleSize + k] << " ";
                                request->channels[channel].data[totalNumberOfSamplesRead++] = frame->data[0][i * sampleSize + k];
                            }
                        }
                    }
                } else {
                    for (int i = 0, j = 0; i < frame->nb_samples; ++i, ++j) {
                        for (int channel = 0; channel < request->numberOfChannels; ++channel) {
                            for (int k = 0; k < sampleSize; ++k) {
                                int outputBufferIndex = j * sampleSize + k;
                                if (totalNumberOfSamplesRead >= request->bufferSize) {
                                    running = false;
                                    break;
                                }
                                request->channels[channel].data[totalNumberOfSamplesRead++] = frame->data[0][request->numberOfChannels * sampleSize * i + channel *sampleSize + k];
                            }
                        }
                    }
                }
            }

        }
                    std::cout << "new: " << std::endl;
                    for (int u = 0; u <  totalNumberOfSamplesRead && u < 5000; ++u) {
                        std::cout << (int)request->channels[0].data[u] << " ";
                    }

        // clean up
        av_frame_free(&frame);
        avcodec_close(codec);
        avformat_free_context(format);

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
        std::cout << readSamples << std::endl;
        for (int i = 0; i < 50000; ++i) {
         //   std::cout << (int)request.channels[1].data[i] << " ";
        }
        std::cout << std::endl;
    }
    */
}

