#include <string>
#include <map>
#include <set>
#include <iostream>
#include <fstream>
#include <vector>
#include <experimental/filesystem>
#include "common.h"

const int QUEUE_SIZE = 10;

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/display.h>
#include <libavutil/imgutils.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/buffersrc.h>
#include <libavfilter/buffersink.h>

#include <stdio.h>

    struct DecodedPackage
    {
        int64_t dts, pts, timestamp;
        AVFrame *pFrame;
    };


    std::ostream& operator<<(std::ostream &strm, const DecodedPackage& decodedPackage)
    {
        return strm << "DecodedPackage [dts=" << decodedPackage.dts << " pts=" << decodedPackage.pts << " timestamp=" << decodedPackage.timestamp << " pFrame=" << decodedPackage.pFrame << "]" << std::endl;
    }

    struct PtsComparator
    {
        bool operator()(const DecodedPackage& a, const DecodedPackage& b) const
        {
            // According to the internet, we need to sort by pts, however that results in horrible video
            // http://dranger.com/ffmpeg/tutorial05.html says we need to sort by pts, however it sorts by timestamp
            // which also seems to be the same as dts.
            // Confusing :/
            return a.timestamp < b.timestamp;
        }
    };

    struct HwDeviceScaleCapability {
        const char* scaleFunction;
        AVPixelFormat outputFormat;
        const char* outputFormatStringDescriptor;

        bool needsScaling;
        bool needsFormatConversion;
    };

    struct DecodeStructure
    {
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecParameters *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        const AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        AVPacket          packet;
        uint8_t           *buffer = NULL;
        struct SwsContext *sws_ctx = NULL;
        int64_t lastPts = -1;
        std::set<DecodedPackage, PtsComparator> decodedPackages;
        int width, height;
        AVRational framerate;

        AVPixelFormat hw_pix_fmt;
        AVHWDeviceType hwDeviceType;
        HwDeviceScaleCapability hwScaleCapability;
        SwsContext* hwSwsContext;
        AVBufferRef* hwContext = NULL;
        AVFilterGraph* hwFilterGraph = NULL;
        AVFilterContext *buffersink_ctx = NULL;
        AVFilterContext *buffersrc_ctx = NULL;
    };


    std::map<std::string, DecodeStructure*> idTodecodeStructureMap;

    void copyFrameData(AVFrame *pFrame, int width, int height, int iFrame, char* frames)
    {
        if (pFrame->linesize[0] == (width * 4)) {
            memcpy(frames, pFrame->data[0], width * height * 4);
        } else {
            for(int y=0; y<height; y++) {
                memcpy(frames + (y * width * 4), pFrame->data[0] + (y * pFrame->linesize[0]),  width * 4);
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
        double rotationAngle;
        int hwDecodingSupported;
    };

    AVRational findFramerate(AVStream* st) {
        AVRational framerate;
        if (st->avg_frame_rate.den == 0)
        {
            framerate = st->r_frame_rate;
        }
        else
        {
            framerate = st->avg_frame_rate;
        }
        return framerate;
    }
 
    // from FFMPEG: https://github.com/FFmpeg/FFmpeg/blob/bc70684e74a185d7b80c8b80bdedda659cb581b8/fftools/cmdutils.c#L2188
    double get_rotation(AVStream *st)
    {
        uint8_t* displaymatrix = av_stream_get_side_data(st,
                                                        AV_PKT_DATA_DISPLAYMATRIX, NULL);
        double theta = 0;
        if (displaymatrix) {
            theta = -av_display_rotation_get((int32_t*) displaymatrix);
        }

        theta -= 360*floor(theta/360 + 0.9/360);

        return theta;
    }

        static enum AVPixelFormat get_hw_format(AVCodecContext *ctx,
                                            const enum AVPixelFormat *pix_fmts)
    {
        const enum AVPixelFormat *p;

        AVPixelFormat hw_pix_fmt = (AVPixelFormat)((long)ctx->opaque);

        for (p = pix_fmts; *p != -1; p++) {
            if (*p == hw_pix_fmt)
                return *p;
        }

        ERROR("Failed to get HW surface format.");
        return AV_PIX_FMT_NONE;
    }

    struct HwDeviceDescriptor {
        AVHWDeviceType type = AV_HWDEVICE_TYPE_NONE;
        const char* deviceTypeString = NULL;
        std::string deviceString;
        std::string driver;
    };

    static std::string getDriverForVaapi(std::string file) {
        std::string line;
        std::string filename = file + "/device/uevent";

        std::ifstream input_file(filename);
        if (!input_file.is_open()) {
            WARN("Cannot open " << filename.c_str());
            return std::string();
        }

        while (std::getline(input_file, line)){
            if (line.find("DRIVER=") != std::string::npos) {
                return line.replace(line.find("DRIVER="), 7, "");
            }
        }
        return "";
    }

    static std::vector<HwDeviceDescriptor> getHwDescriptors() {
        std::vector<HwDeviceDescriptor> hwDevices;

        // vaapi
        std::string path = "/sys/class/drm";
        if (std::experimental::filesystem::is_directory(path)) {
            for (const auto & entry : std::experimental::filesystem::directory_iterator(path)) {
                const std::string path = entry.path().string();
                std::size_t renderIndex = path.find("renderD");
                if (path.find("renderD") != std::string::npos) {
                    std::string driver = getDriverForVaapi(path);
                    if (driver != "") {
                        HwDeviceDescriptor device = HwDeviceDescriptor();
                        device.type = AV_HWDEVICE_TYPE_VAAPI;
                        device.deviceTypeString = "vaapi";
                        device.deviceString = (std::string("/dev/dri/") + path.substr(renderIndex, 10));
                        device.driver = std::string(driver);
                        hwDevices.push_back(device);
                    }
                }
            }
        }
        return hwDevices;
    }

    static int hw_decoder_init(AVCodecContext *ctx, const enum AVHWDeviceType type)
    {
        int err = 0;

        std::vector<HwDeviceDescriptor> desciptors = getHwDescriptors();

        for (int i = 0; i < desciptors.size(); ++i) {
            INFO("Found HW device with type=" << desciptors[i].deviceTypeString << ", driver=" << desciptors[i].driver << ", deviceString=" << desciptors[i].deviceString);
        }

        const char* usedDevice = NULL;

        if (desciptors.size() == 1) {
            usedDevice = desciptors[0].deviceString.c_str();
        } else if (desciptors.size() > 1) {
            for (int i = 0; i < desciptors.size(); ++i) {
                if (desciptors[i].driver == "i915") { // Intel iGPU decoder preferred due to QuickSync
                    usedDevice = desciptors[i].deviceString.c_str();
                }
            }
            if (usedDevice == NULL) {
                usedDevice = desciptors[0].deviceString.c_str();
            }
        }
        INFO("Used HW device=" << usedDevice);

        AVBufferRef *buffer = NULL;

        if ((err = av_hwdevice_ctx_create(&buffer, type, usedDevice, NULL, 0)) < 0) {
            WARN("Failed to create specified HW device.");
            return err;
        }
        ctx->hw_device_ctx = av_buffer_ref(buffer);

        
        return err;
    }

    AVPixelFormat getPixelFormatAndSetCallback(AVCodecContext* pCodecCtx, AVHWDeviceType hwDeviceType, const AVCodec* pCodec) {
        AVPixelFormat hw_pix_fmt = AV_PIX_FMT_NONE;
        if (hwDeviceType != AV_HWDEVICE_TYPE_NONE) {
            for (int i = 0;; i++) {
                const AVCodecHWConfig *config = avcodec_get_hw_config(pCodec, i);
                if (!config) {
                    INFO("Decoder " << pCodec->name << " does not support device type");
                    hwDeviceType = AV_HWDEVICE_TYPE_NONE;
                    break;
                } else if (config->methods & AV_CODEC_HW_CONFIG_METHOD_HW_DEVICE_CTX && config->device_type == hwDeviceType) {
                    hw_pix_fmt = config->pix_fmt;
                    break;
                }
            }

            if (hwDeviceType != AV_HWDEVICE_TYPE_NONE) {
                if (hw_decoder_init(pCodecCtx, hwDeviceType) < 0) {
                    WARN("Cannot initialize hw acceleration");
                    return AV_PIX_FMT_NONE;
                }
                pCodecCtx->get_format  = get_hw_format;
            }
        }
        return hw_pix_fmt;
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


    EXPORTED MediaMetadata readMediaMetadata(const char* path)
    {
        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               i, videoStream;
        AVCodecParameters *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        const AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        MediaMetadata mediaMetadata;
        mediaMetadata.fps = -1;
        mediaMetadata.width = -1;
        mediaMetadata.height = -1;
        mediaMetadata.lengthInMicroseconds = -1;
        mediaMetadata.bitRate = 0;


        AVHWDeviceType hwDeviceType = av_hwdevice_find_type_by_name("vaapi");
        if (hwDeviceType == AV_HWDEVICE_TYPE_NONE) {
            INFO("VAAPI HW acceleration not available for " << path);
        }

        if(avformat_open_input(&pFormatCtx, path, NULL, NULL)!=0)
            return mediaMetadata;

        if(avformat_find_stream_info(pFormatCtx, NULL)<0)
            return mediaMetadata;

        av_dump_format(pFormatCtx, 0, path, 0);

        videoStream=-1;
        for(i=0; i<pFormatCtx->nb_streams; i++)
            if(pFormatCtx->streams[i]->codecpar->codec_type==AVMEDIA_TYPE_VIDEO)
            {
                videoStream=i;
                break;
            }
        if(videoStream==-1)
            return mediaMetadata;

        pCodecCtxOrig=pFormatCtx->streams[videoStream]->codecpar;

        DEBUG("Codec ID for decoding " << pCodecCtxOrig->codec_id);


        if (!(pCodecCtx = avcodec_alloc_context3(pCodec))) {
            return mediaMetadata;
        }
        if (avcodec_parameters_to_context(pCodecCtx, pCodecCtxOrig) < 0) {
            ERROR("Cannot create context from parameters");
            return mediaMetadata;
        }
        int ret = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pCodec, 0);

        if(pCodec==NULL || ret < 0)
        {
            fprintf(stderr, "Unsupported codec!\n");
            return mediaMetadata;
        }



        AVPixelFormat hw_pix_fmt = getPixelFormatAndSetCallback(pCodecCtx, hwDeviceType, pCodec);
        if (hw_pix_fmt == AV_PIX_FMT_NONE) {
            hwDeviceType = AV_HWDEVICE_TYPE_NONE;
        }

        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
            return mediaMetadata;
        pCodecCtx->opaque = (void*)(int)hw_pix_fmt;

        pFrame=av_frame_alloc();

        AVFrame* pFrameRGB=av_frame_alloc();
        if(pFrameRGB==NULL)
            return mediaMetadata;

        AVStream* st = pFormatCtx->streams[videoStream];

        long long durationInMicroseconds = pFormatCtx->duration / (AV_TIME_BASE / 1000000);

        if (durationInMicroseconds < 0) {
            WARN("Unable to determine duration for file " << path << " most likely metadata is missing");
            return mediaMetadata;
        }

        // read one frame, otherwise some things are not initialized regarding HW accel
        AVPacket packet;
        i = 0;
        while(av_read_frame(pFormatCtx, &packet)>=0 && i < 1)
        {
            if(packet.stream_index==videoStream)
            {
                int frameFinished;
                int decodedFrame = decode_video_frame(pCodecCtx, pFrame, &frameFinished, &packet);

                if (decodedFrame < 0) {
                    hwDeviceType = AV_HWDEVICE_TYPE_NONE;
                    break;
                }

                if(frameFinished) {
                    ++i;
                }
            }
            av_packet_unref(&packet);
        }
        if (hwDeviceType != AV_HWDEVICE_TYPE_NONE) {
            AVBufferRef* ref;
            int supported = avcodec_get_hw_frames_parameters(pCodecCtx, pCodecCtx->hw_device_ctx, hw_pix_fmt, &ref);
            if (supported < 0) {
                hwDeviceType = AV_HWDEVICE_TYPE_NONE;
                pCodecCtx->hw_device_ctx = NULL;
            }
        }

        mediaMetadata.width = pCodecCtxOrig->width;
        mediaMetadata.height = pCodecCtxOrig->height;
        mediaMetadata.lengthInMicroseconds = durationInMicroseconds;
        mediaMetadata.bitRate = st->codecpar->bit_rate;
        mediaMetadata.rotationAngle = get_rotation(st);
        mediaMetadata.hwDecodingSupported = (hwDeviceType != AV_HWDEVICE_TYPE_NONE);

        AVRational framerate = findFramerate(st);

        mediaMetadata.fps = framerate.num / (double)framerate.den;

        DEBUG("File length=" << mediaMetadata.lengthInMicroseconds << " in timebase=" << (st->duration / (AV_TIME_BASE / 1000000)));


        av_frame_free(&pFrameRGB);

        av_frame_free(&pFrame);

        avcodec_close(pCodecCtx);

        avformat_close_input(&pFormatCtx);

        return mediaMetadata;
    }

    typedef struct
    {
        char* data;
        long long startTimeInMs;
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

        int actualNumberOfFramesRead;
        long long endTimeInMs;

        int useHardwareDecoding;
    } FFmpegImageRequest;

    int decodedMinPts(DecodeStructure* decodeStructure)
    {
        if (decodeStructure->decodedPackages.size())
        {
            return (*(decodeStructure->decodedPackages.begin())).timestamp;
        }
        else
        {
            return decodeStructure->lastPts;
        }
    }

    AVFrame* allocateFrame(int width, int height)
    {
        AVFrame* pFrameRGB=av_frame_alloc();
        int numBytes=av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 16);

        uint8_t* buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
        av_image_fill_arrays(pFrameRGB->data, pFrameRGB->linesize, buffer, AV_PIX_FMT_RGBA,
                       width, height, 16);
        pFrameRGB->opaque = buffer;
        return pFrameRGB;
    }

    void freeFrame(AVFrame* frame)
    {
        // DEBUG("Preparing to free " << frame << " " << frame->opaque);
        av_free(frame->opaque);
        av_frame_free(&frame);
    }

    DecodeStructure* openFile(FFmpegImageRequest* request);

    void emptyQueue(DecodeStructure* decodeStructure)
    {
        //DEBUG("Emptying queue");
        while (decodeStructure->decodedPackages.size() > 0)
        {
            DecodedPackage element = *(decodeStructure->decodedPackages.begin());
            //DEBUG("Clearing element " << element);
            decodeStructure->decodedPackages.erase(decodeStructure->decodedPackages.begin());

            freeFrame(element.pFrame);
        }
    }

    bool containsFormat(AVPixelFormat* formats, AVPixelFormat format) {
        for (int i = 0; formats[i] != AV_PIX_FMT_NONE; i++) {
            if (formats[i] == format) {
                return true;
            }
        }
        return false;
    }

    int getScaleCapability(AVHWDeviceType type, AVBufferRef* hwBuffer, HwDeviceScaleCapability* result) {
        AVPixelFormat *formats;
        
        int err = av_hwframe_transfer_get_formats(hwBuffer,
                                          AV_HWFRAME_TRANSFER_DIRECTION_FROM,
                                          &formats, 0);

        if (containsFormat(formats, AV_PIX_FMT_RGBA)) {
            result->needsFormatConversion = false;
            result->outputFormatStringDescriptor = "rgba"; // av_get_pix_fmt_string
            result->outputFormat = AV_PIX_FMT_RGBA;
        } else if (containsFormat(formats, AV_PIX_FMT_RGB0)) {
            result->needsFormatConversion = true;
            result->outputFormatStringDescriptor = "rgb0"; // av_get_pix_fmt_string
            result->outputFormat = AV_PIX_FMT_RGB0;
        } else if (containsFormat(formats, AV_PIX_FMT_YUV420P)) {
            result->needsFormatConversion = true;
            result->outputFormatStringDescriptor = "yuv420p"; // av_get_pix_fmt_string
            result->outputFormat = AV_PIX_FMT_YUV420P;
        } else if (containsFormat(formats, AV_PIX_FMT_RGB0)) {
            result->needsFormatConversion = true;
            result->outputFormatStringDescriptor = "rgb0"; // av_get_pix_fmt_string
            result->outputFormat = AV_PIX_FMT_RGB0;
        } else {
            return -1;
        }


        av_freep(&formats);


        if (type == AV_HWDEVICE_TYPE_VAAPI) {
            result->scaleFunction = "scale_vaapi";
            result->needsScaling = false;
        } else {
            result->needsScaling = true;
        }
        return 0;
    }

    static int createInputFilter(AVCodecContext* dec_ctx, AVFilterInOut *inputs, DecodeStructure* element) {
        int ret;
        char args[512];
        const AVFilter *buffersrc  = avfilter_get_by_name("buffer");

        snprintf(args, sizeof(args),
            "video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=%d/%d",
            dec_ctx->width, dec_ctx->height, element->hw_pix_fmt,
            1, 60,
            dec_ctx->sample_aspect_ratio.num, dec_ctx->sample_aspect_ratio.den);
        ret = avfilter_graph_create_filter(&element->buffersrc_ctx, buffersrc, "in",
                                        args, NULL, element->hwFilterGraph);
        if (ret < 0) {
            av_log(NULL, AV_LOG_ERROR, "Cannot create buffer source\n");
            return ret;
        }
        AVBufferSrcParameters *par = av_buffersrc_parameters_alloc();
        

        if (!par)
            return AVERROR(ENOMEM);
        memset(par, 0, sizeof(*par));
        par->format = AV_PIX_FMT_NONE;
        par->hw_frames_ctx = element->hwContext;
        ret = av_buffersrc_parameters_set(element->buffersrc_ctx, par);
        av_freep(&par);
        
        if ((ret = avfilter_link(element->buffersrc_ctx, 0, inputs[0].filter_ctx, inputs[0].pad_idx)) < 0)
            return ret;
        return 0;
    }


    static int createOutputFilter(AVCodecContext* dec_ctx, AVFilterInOut *outputs, DecodeStructure* element) {
        int ret;
        char args[512];
        const AVFilter *buffersink = avfilter_get_by_name("buffersink");

        ret = avfilter_graph_create_filter(&element->buffersink_ctx, buffersink, "out",
                                        NULL, NULL, element->hwFilterGraph);
        
        if (ret < 0) {
            av_log(NULL, AV_LOG_ERROR, "Cannot create buffer sink\n");
            return ret;
        }


        if ((ret = avfilter_link(outputs[0].filter_ctx, 0, element->buffersink_ctx, 0)) < 0)
            return ret;
        return 0;
    }

    static int init_filters(AVCodecContext* dec_ctx, DecodeStructure* element)
    {
        char args[512];
        int ret;
        AVFilterInOut *outputs = NULL;
        AVFilterInOut *inputs  = NULL;
        char hwFilterExpression[400];


        int res = getScaleCapability(element->hwDeviceType, element->hwContext, &element->hwScaleCapability);
        
        HwDeviceScaleCapability capability = element->hwScaleCapability;

        if (res < 0) {
            WARN("Cannot initialize scaling, disable HW acceleration");
            element->hwDeviceType = AV_HWDEVICE_TYPE_NONE;
        } else {
            if (!capability.needsScaling) {
                snprintf(hwFilterExpression, 400, "%s=%d:%d,hwdownload,format=%s", 
                    capability.scaleFunction,  element->width, element->height, capability.outputFormatStringDescriptor);
            } else {
                snprintf(hwFilterExpression, 400, "hwdownload,format=%s", 
                    capability.outputFormatStringDescriptor);
            }
            DEBUG("Using FFMPEG filter expression " << hwFilterExpression);

            if (capability.needsScaling || capability.needsFormatConversion) {
                int fromW = capability.needsScaling ? element->pCodecCtx->width : element->width;
                int fromH = capability.needsScaling ? element->pCodecCtx->height : element->height;
                element->hwSwsContext = sws_getContext(fromW,
                                                        fromH,
                                                        capability.outputFormat,
                                                        element->width,
                                                        element->height,
                                                        AV_PIX_FMT_RGBA,
                                                        SWS_FAST_BILINEAR,
                                                        NULL,
                                                        NULL,
                                                        NULL);
            }
        }



        element->hwFilterGraph = avfilter_graph_alloc();

        if ((ret = avfilter_graph_parse2(element->hwFilterGraph, hwFilterExpression,
                                        &inputs, &outputs)) < 0)
            return ret;

        for (int i = 0; i < element->hwFilterGraph->nb_filters; ++i) {
                element->hwFilterGraph->filters[i]->hw_device_ctx = av_buffer_ref(element->hwContext);
        }

        createInputFilter(dec_ctx, inputs, element);
        createOutputFilter(dec_ctx, outputs, element);



        if ((ret = avfilter_graph_config(element->hwFilterGraph, NULL)) < 0)
            return ret;
        return 0;
    }

    AVFrame* processFrame(DecodeStructure* element, AVFrame *pFrame) {
        AVCodecContext    *pCodecCtx = element->pCodecCtx;
        struct SwsContext *sws_ctx = element->sws_ctx;
        AVCodecParameters *pCodecCtxOrig = element->pCodecCtxOrig;

        AVFrame* pFrameRGB=allocateFrame(element->width, element->height);
        if (element->hwDeviceType != AV_HWDEVICE_TYPE_NONE) {
            if (element->hwContext == NULL) {
                element->hwContext = pFrame->hw_frames_ctx;
            }

            if (element->hwFilterGraph == NULL && init_filters(pCodecCtx, element) < 0) {
                ERROR("Init filter fails\n");
                return NULL;
            }

            // push the decoded frame into the filtergraph
            if (av_buffersrc_add_frame_flags(element->buffersrc_ctx, pFrame, AV_BUFFERSRC_FLAG_KEEP_REF) < 0) {
                av_log(NULL, AV_LOG_ERROR, "Error while feeding the filtergraph\n");
                return NULL;
            }
            // pull filtered frames from the filtergraph
            AVFrame* filt_frame = av_frame_alloc();
            int ret = av_buffersink_get_frame(element->buffersink_ctx, filt_frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                return NULL;
            }
            if (ret < 0) {
                av_log(NULL, AV_LOG_ERROR, "Error reading from buffersink\n");
                return NULL;
            }
            

            if (element->hwScaleCapability.needsFormatConversion || element->hwScaleCapability.needsScaling) {
                sws_scale(element->hwSwsContext, (uint8_t const * const *)filt_frame->data,
                        filt_frame->linesize, 0, filt_frame->height,
                        pFrameRGB->data, pFrameRGB->linesize);
            } else {
                memcpy(pFrameRGB->data, filt_frame->data, element->width*element->height*4);
            }


            av_frame_free(&filt_frame);
        } else {
            sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
                    pFrame->linesize, 0, pCodecCtxOrig->height,
                    pFrameRGB->data, pFrameRGB->linesize);
        }
        return pFrameRGB;
    }

    int fillQueue(DecodeStructure* element)
    {
        AVFormatContext   *pFormatCtx = element->pFormatCtx;
        int               videoStream = element->videoStream;
        AVCodecParameters *pCodecCtxOrig = element->pCodecCtxOrig;
        AVCodecContext    *pCodecCtx = element->pCodecCtx;
        const AVCodec           *pCodec = element->pCodec;
        AVFrame           *pFrame = element->pFrame;
        AVPacket          packet = element->packet;
        struct SwsContext *sws_ctx = element->sws_ctx;
        int frameFinished;
        int decodedFrame;
        while(element->decodedPackages.size() < QUEUE_SIZE && av_read_frame(pFormatCtx, &packet)>=0)
        {
            if(packet.stream_index==videoStream)
            {
                do {
                    decodedFrame = decode_video_frame(pCodecCtx, pFrame, &frameFinished, &packet);
                    if (decodedFrame < 0 && decodedFrame != AVERROR(EAGAIN)) {
                        WARN("Unable to read more frames, statusCode=" << decodedFrame);
                        return decodedFrame;
                    }
                    
                    if(frameFinished)
                    {
                        AVFrame* pFrameRGB = processFrame(element, pFrame);


                        DecodedPackage decodedPackage;
                        decodedPackage.pts = packet.pts;
                        decodedPackage.dts = packet.dts;
                        decodedPackage.timestamp = pFrame->best_effort_timestamp;
                        decodedPackage.pFrame = pFrameRGB;

                        element->decodedPackages.insert(decodedPackage);

                        //DEBUG("Actual video package " << packet.dts << " " <<  packet.pts << " " << decodedPackage.timestamp << " (" << (av_frame_get_best_effort_timestamp(pFrame) * av_q2d(pFormatCtx->streams[videoStream]->time_base)) << ") " << " (" << (pFrame->pts * av_q2d(pFormatCtx->streams[videoStream]->time_base)) << ")");
                    }
                    element->lastPts = packet.pts;
                 //   DEBUG("Read video package " << packet.dts << " " <<  packet.pts << " (" << (av_frame_get_best_effort_timestamp(pFrame) * av_q2d(pFormatCtx->streams[videoStream]->time_base)) << ")");
                } while (decodedFrame == AVERROR(EAGAIN));
            }
            // Free the packet that was allocated by av_read_frame
            av_packet_unref(&packet);
        }
        //DEBUG("Set filled: " << element->decodedPackages);
        return 0;
    }

    std::string createKey(const char* path, int width, int height, bool enableHwAcceleration) {
        return (std::string(path) + "_" + std::to_string(width) + "_" + std::to_string(height) + "_" + std::string("hwaccel=") + std::to_string(enableHwAcceleration));
    }

    EXPORTED void readFrames(FFmpegImageRequest* request)
    {
        std::string key = createKey(request->path, request->width, request->height, request->useHardwareDecoding);
        std::map<std::string,DecodeStructure*>::iterator elementIterator = idTodecodeStructureMap.find(key);

        DecodeStructure* decodeStructure;


        if (elementIterator == idTodecodeStructureMap.end())
        {
            decodeStructure = openFile(request);
        }
        else
        {
            DEBUG("Found Element" <<  elementIterator->first);
            decodeStructure = elementIterator->second;
        }

        if (decodeStructure == NULL)
        {
            return;
        }

        int i = 0;
        AVFormatContext   *pFormatCtx = decodeStructure->pFormatCtx;
        int               videoStream = decodeStructure->videoStream;
        AVCodecParameters *pCodecCtxOrig = decodeStructure->pCodecCtxOrig;
        AVCodecContext    *pCodecCtx = decodeStructure->pCodecCtx;
        const AVCodec           *pCodec = decodeStructure->pCodec;
        AVFrame           *pFrame = decodeStructure->pFrame;
        AVPacket          packet = decodeStructure->packet;
        int               frameFinished = 0;
        uint8_t           *buffer = decodeStructure->buffer;
        struct SwsContext *sws_ctx = decodeStructure->sws_ctx;

        // AV_TIME_BASE_Q   (AVRational){1, AV_TIME_BASE} -> VC++ causes error
        AVRational timeBaseQ;
        timeBaseQ.num = 1;
        timeBaseQ.den = AV_TIME_BASE;

        int64_t seek_target = request->startMicroseconds * (AV_TIME_BASE / 1000000); // rethink
        int64_t end_target = request->endTimeInMs * (AV_TIME_BASE / 1000000); // rethink

        int64_t minimumTimeRequiredToSeek = 2 * 1000000 * (AV_TIME_BASE / 1000000); // Seek if distance is more than 2 seconds
        AVRational timeframe = pFormatCtx->streams[videoStream]->time_base;
        seek_target = av_rescale_q(seek_target, timeBaseQ, timeframe);
        minimumTimeRequiredToSeek = av_rescale_q(minimumTimeRequiredToSeek, timeBaseQ, pFormatCtx->streams[videoStream]->time_base);
        int64_t seek_distance = seek_target - decodedMinPts(decodeStructure);
        
        int64_t nextPts = decodeStructure->lastPts;

        if (decodeStructure->decodedPackages.size() > 0) {
            DecodedPackage nextElement = *(decodeStructure->decodedPackages.begin());
            nextPts = nextElement.pts;
        }

        int frameTime = (int)ceil((double)decodeStructure->framerate.den / decodeStructure->framerate.num * AV_TIME_BASE);
        int frameTimeInStreamTimebase = av_rescale_q(frameTime, timeBaseQ, timeframe);
        
        DEBUG("Seeking info " << request->startMicroseconds << " current_position=" << nextPts << " expected=" << seek_target << " distance=" << seek_distance << " frameTime=" << frameTimeInStreamTimebase);
        

        if (seek_distance > minimumTimeRequiredToSeek || seek_distance < -frameTimeInStreamTimebase || request->useApproximatePosition)
        {
            DEBUG("Seeking required");
            av_seek_frame(pFormatCtx, videoStream, seek_target, AVSEEK_FLAG_BACKWARD);
            avcodec_flush_buffers(pCodecCtx);
            emptyQueue(decodeStructure);
        }
        else
        {
            DEBUG("No seek, distance " << seek_distance);
        }

        if (request->useApproximatePosition)
        {
            while(av_read_frame(pFormatCtx, &packet)>=0 && i < request->numberOfFrames)
            {
                if(packet.stream_index==videoStream)
                {
                    int decodedFrame = decode_video_frame(pCodecCtx, pFrame, &frameFinished, &packet);

                    if (decodedFrame < 0 && decodedFrame != AVERROR(EAGAIN)) {
                        WARN("Unable to read more frames, statusCode=" << decodedFrame);
                        break;
                    }

                    if(frameFinished)
                    {
                        AVFrame* pFrameRGB = processFrame(decodeStructure, pFrame);

                        copyFrameData(pFrameRGB, request->width, request->height, i, request->frames[i].data);
                        request->frames[i].startTimeInMs = av_rescale_q(pFrame->best_effort_timestamp, timeframe, timeBaseQ);
                        ++i;
                        freeFrame(pFrameRGB);

                        if (request->frames[i].startTimeInMs >= end_target) {
                            break;
                        }
                    }
                    decodeStructure->lastPts = packet.pts;
                    //DEBUG("Read video package " << packet.dts << " " <<  packet.pts << " (" << (av_frame_get_best_effort_timestamp(pFrame) * av_q2d(pFormatCtx->streams[videoStream]->time_base)) << ")");
                }
                // Free the packet that was allocated by av_read_frame
                av_packet_unref(&packet);
            }
        }
        else
        {
            fillQueue(decodeStructure);
            while (i < request->numberOfFrames - 1 && decodeStructure->decodedPackages.size() > 0)
            {
                DecodedPackage element = *(decodeStructure->decodedPackages.begin());
                long time = av_rescale_q(element.timestamp, timeframe, timeBaseQ);

                if (element.timestamp < seek_target)
                {
                    DEBUG("Skipping package " << element.timestamp);
                }
                else if (time < end_target)
                {
                    copyFrameData(element.pFrame, request->width, request->height, i, request->frames[i].data);
                    request->frames[i].startTimeInMs = time;
                   // std::cout << "########### reading: " << i << " " << time << " " << element.timestamp << " " << end_target << " " << timeframe.num << "/" << timeframe.den << std::endl;
                    ++i;
                } else {
                   // std::cout << "End reading " << time << std::endl;
                    if (decodeStructure->decodedPackages.size() == 0) {
                        //  fillQueue(decodeStructure);
                    }
                    if (decodeStructure->decodedPackages.size() > 0) {
                        DecodedPackage nextFrame = *decodeStructure->decodedPackages.begin();
                        long long nextFrameTime = av_rescale_q(nextFrame.timestamp, timeframe, timeBaseQ);
                        copyFrameData(nextFrame.pFrame, request->width, request->height, i, request->frames[i].data);
                        request->frames[i].startTimeInMs = nextFrameTime;
                        request->endTimeInMs = nextFrameTime;
                        ++i;
                    }

                    break;
                }
                decodeStructure->decodedPackages.erase(decodeStructure->decodedPackages.begin());
                fillQueue(decodeStructure);
                freeFrame(element.pFrame);
            }
        }
        request->actualNumberOfFramesRead = i;

        DEBUG("Queue size: " << decodeStructure->decodedPackages.size());
    }


    DecodeStructure* openFile(FFmpegImageRequest* request)
    {
        DEBUG("Opening file " << request->path << " " << request->width << " " << request->height);

        // Initalizing these to NULL prevents segfaults!
        AVFormatContext   *pFormatCtx = NULL;
        int               videoStream;
        AVCodecParameters    *pCodecCtxOrig = NULL;
        AVCodecContext    *pCodecCtx = NULL;
        const AVCodec           *pCodec = NULL;
        AVFrame           *pFrame = NULL;
        AVPacket          packet;
        uint8_t           *buffer = NULL;
        struct SwsContext *sws_ctx = NULL;

        // HW fields
        AVHWDeviceType hwDeviceType = AV_HWDEVICE_TYPE_NONE;
        AVPixelFormat hw_pix_fmt = AV_PIX_FMT_NONE;
        HwDeviceScaleCapability capability;
        SwsContext* hwSwsContext = NULL;

        if (request->useHardwareDecoding) {
            hwDeviceType = av_hwdevice_find_type_by_name("vaapi");
            if (hwDeviceType == AV_HWDEVICE_TYPE_NONE) {
                INFO("VAAPI HW acceleration not available for " << request->path);
            }
        }

        if(avformat_open_input(&pFormatCtx, request->path, NULL, NULL)!=0)
        {
            ERROR("Cannot open input ");
            return NULL;
        }

        if(avformat_find_stream_info(pFormatCtx, NULL)<0)
        {
            ERROR("Cannot find stream info ");
            return NULL;
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
            ERROR("No video stream found in " << request->path);
            return NULL;
        }

        pCodecCtxOrig=pFormatCtx->streams[videoStream]->codecpar;


        if (!(pCodecCtx = avcodec_alloc_context3(pCodec))) {
            ERROR("Cannot create codec context " << request->path);
            return NULL;
        }
        if (avcodec_parameters_to_context(pCodecCtx, pCodecCtxOrig) < 0) {
            ERROR("Cannot create codec context " << request->path);
            return NULL;
        }
        int ret = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pCodec, 0);

        if(pCodec==NULL || ret < 0)
        {
            ERROR("Unsupported codec: " << pCodecCtxOrig->codec_id);
            return NULL;
        }
        DEBUG("Using codec " << pCodec->name << " for " << request->path);

        if (pCodec->capabilities & AV_CODEC_CAP_FRAME_THREADS) {
            INFO("Codec has frame thread level threading for file " << request->path);
        }
        if (pCodec->capabilities & AV_CODEC_CAP_SLICE_THREADS) {
            INFO("Codec has frame slice level threading for file " << request->path);
        }

        pCodecCtx->thread_count = 0; // 0 = automatic
        pCodecCtx->thread_type = (FF_THREAD_FRAME | FF_THREAD_SLICE);


        hw_pix_fmt = getPixelFormatAndSetCallback(pCodecCtx, hwDeviceType, pCodec);
        if (hw_pix_fmt == AV_PIX_FMT_NONE) {
            hwDeviceType = AV_HWDEVICE_TYPE_NONE;
        }



        if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
        {
            ERROR("Cannot open codec context");
            return NULL;
        }

        pFrame=av_frame_alloc();

        INFO("Opening file with size " << request->width << "x" << request->height);

        sws_ctx = sws_getContext(pCodecCtxOrig->width,
                                 pCodecCtxOrig->height,
                                 pCodecCtx->pix_fmt,
                                 request->width,
                                 request->height,
                                 AV_PIX_FMT_RGBA,
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
        element->packet = packet;
        element->buffer = buffer;
        element->sws_ctx = sws_ctx;
        element->width = request->width;
        element->height = request->height;
        element->framerate = findFramerate(pFormatCtx->streams[videoStream]);
        element->hw_pix_fmt = hw_pix_fmt;
        element->hwDeviceType = hwDeviceType;
        element->hwScaleCapability = capability;
        element->hwSwsContext = hwSwsContext;

        pCodecCtx->opaque = (void*)(int)hw_pix_fmt;

        std::string key = createKey(request->path, request->width, request->height, request->useHardwareDecoding);

        idTodecodeStructureMap.insert(std::pair<std::string, DecodeStructure*>(std::string(key), element));

        return element;
    }


static void ppm_save(unsigned char* buf, int wrap, int xsize, int ysize, char* filename)
{
    FILE* f;
    int i;

    f = fopen(filename, "wb");
    fprintf(f, "P3\n%d %d\n%d\n", xsize, ysize, 255);

    for (int y = 0; y < ysize; ++y) {
        for (int x = 0; x < xsize; ++x) {
            unsigned char* basePtr = buf + y * wrap + x * 4;
            fprintf(f, "%d %d %d\n", basePtr[0], basePtr[1], basePtr[2]);
        }
    }

    fclose(f);
}


#ifdef DEBUG_BUILD

int main() {
    char* path = "/tmp/tactview_render_test.wmv";

    MediaMetadata result = readMediaMetadata(path);

    INFO("METADATA: width=" << result.width << " height=" << result.height << " fps=" << result.fps << " lengthInMs=" << result.lengthInMicroseconds << " bitRate=" << result.bitRate << " hwAccel=" << result.hwDecodingSupported);

    const int framesPerRequest = 30;

    const int outWidth = 412;
    const int outHeight = 301;

    long long start = time(NULL);
    long startTime = 0;
    int framesRead = 0;
    
    FFMpegFrame* frames = new FFMpegFrame[framesPerRequest];

    if (result.width < 0) {
        INFO("File not supported");
        return 0;
    }

    for (int j = 0; j < framesPerRequest; ++j) {
        frames[j].data = new char[outWidth * outHeight * 4];
    }

    int lengthInMs = (framesPerRequest * (1.0/result.fps) * 1000000);

    for (int i = 0; i < 50 && startTime + lengthInMs < result.lengthInMicroseconds; ++i) {

        int frameToRequest = i * framesPerRequest;

        FFmpegImageRequest* request = new FFmpegImageRequest();
        request->width = outWidth;
        request->height = outHeight;
        request->numberOfFrames = framesPerRequest;
        request->path = path;
        request->startMicroseconds = startTime;
        request->endTimeInMs = startTime + lengthInMs;
        request->useApproximatePosition = false;
        request->useHardwareDecoding = 1;
        request->frames = frames;
        request->useHardwareDecoding = result.hwDecodingSupported;


        readFrames(request);

        if (i % 1 == 0) {
            char buf[100];
            snprintf(buf, sizeof(buf), "/tmp/%s_%03d_%03d_%03d.ppm", "decode", i, outWidth, outHeight);
            ppm_save((unsigned char*)request->frames[0].data, request->width * 4, request->width, request->height, buf);
        }

        int newFramesRead = request->actualNumberOfFramesRead + framesRead;

        startTime = request->endTimeInMs;

        delete request;
        INFO("read frames " << framesRead << " " << newFramesRead);
        framesRead = newFramesRead;
    }
    for (int j = 0; j < framesPerRequest; ++j) {
        delete[] frames[j].data;
    }

    long long end = time(NULL);

    long took = (end - start);

    std::cout << ((double)framesRead / took) << " fps; took: " << took << " seconds" << std::endl;
}
#endif

}