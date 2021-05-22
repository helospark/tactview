package com.helospark.tactview.core.render.proxy;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.render.ffmpeg.FFmpegBasedMediaEncoder;
import com.helospark.tactview.core.render.ffmpeg.FFmpegClearEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegEncodeFrameRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegInitEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.RenderFFMpegFrame;
import com.helospark.tactview.core.timeline.RequestFrameParameter;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressType;
import com.helospark.tactview.core.util.messaging.MessagingService;

// TODO: consider configurable image sequence
@Component
public class LowResolutionProxyCreatorService {
    private ExecutorService executorService;
    private FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;
    private FFmpegBasedMediaDecoderDecorator mediaDecoder;
    private MessagingService messagingService;

    private String path = System.getProperty("java.io.tmpdir");

    public LowResolutionProxyCreatorService(FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder, FFmpegBasedMediaDecoderDecorator mediaDecoder, MessagingService messagingService,
            @Qualifier("longRunningTaskExecutorService") ExecutorService executorService) {
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
        this.mediaDecoder = mediaDecoder;
        this.messagingService = messagingService;
        this.executorService = executorService;
    }

    public void createLowResolutionProxy(VideoClip clip, int proxyWidth, int proxyHeight) {
        executorService.submit(() -> createLowResolutionProxyInternal(clip, proxyWidth, proxyHeight));
    }

    private void createLowResolutionProxyInternal(VideoClip clip, int proxyWidth, int proxyHeight) {
        File proxyFile = new File(path + File.separator + clip.getId() + ".mp4");
        VideoMetadata originalMetadata = (VideoMetadata) clip.getMediaMetadata();

        BigDecimal frameTime = BigDecimal.ONE.divide(new BigDecimal(originalMetadata.getFps()), 10, RoundingMode.HALF_UP);

        FFmpegInitEncoderRequest initNativeRequest = new FFmpegInitEncoderRequest();
        initNativeRequest.fileName = proxyFile.getAbsolutePath();
        initNativeRequest.fps = (int) originalMetadata.getFps(); // why int?
        int width = proxyWidth;
        if (width % 2 == 1) {
            width++;
        }
        int height = proxyHeight;
        if (height % 2 == 1) {
            height++;
        }
        initNativeRequest.renderWidth = width;
        initNativeRequest.renderHeight = height;

        initNativeRequest.actualWidth = width;
        initNativeRequest.actualHeight = height;

        initNativeRequest.bytesPerSample = 0;
        initNativeRequest.audioChannels = 0;
        initNativeRequest.sampleRate = 0;
        initNativeRequest.audioBitRate = 0;
        initNativeRequest.audioSampleRate = 0;

        initNativeRequest.videoBitRate = 320000;
        initNativeRequest.videoCodec = "default";
        initNativeRequest.audioCodec = "none";
        initNativeRequest.videoPixelFormat = "default";
        initNativeRequest.videoPreset = "ultrafast";

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        if (encoderIndex < 0) {
            throw new RuntimeException("Unable to render, statuscode is " + encoderIndex + " , check logs");
        }

        int allJobs = originalMetadata.getLength().getSeconds().divide(frameTime, 10, RoundingMode.HALF_UP).intValue();

        String jobId = UUID.randomUUID().toString();
        messagingService.sendAsyncMessage(new ProgressInitializeMessage(jobId, allJobs, ProgressType.LOW_RES_PROXY_CREATE));

        TimelinePosition position = TimelinePosition.ofZero();

        int frameIndex = 0;
        while (position.isLessOrEqualToThan(originalMetadata.getLength().toPosition())) {
            RequestFrameParameter frameRequest = RequestFrameParameter.builder()
                    .withWidth(width)
                    .withHeight(height)
                    .withPosition(position)
                    .withLowResolutionPreview(false)
                    .withUseApproximatePosition(false)
                    .build();
            ByteBuffer frame = clip.requestFrame(frameRequest).getBuffer();

            FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
            nativeRequest.frame = new RenderFFMpegFrame();
            RenderFFMpegFrame[] array = (RenderFFMpegFrame[]) nativeRequest.frame.toArray(1);
            array[0].imageData = frame;

            nativeRequest.encoderIndex = encoderIndex;
            nativeRequest.startFrameIndex = frameIndex;

            int encodeResult = ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);

            if (encodeResult < 0) {
                throw new RuntimeException("Cannot encode frames, error code " + encodeResult);
            }

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame);

            messagingService.sendAsyncMessage(new ProgressAdvancedMessage(jobId, 1));
            ++frameIndex;
            position = position.add(frameTime);
        }

        FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
        clearRequest.encoderIndex = encoderIndex;
        ffmpegBasedMediaEncoder.clearEncoder(clearRequest);

        VideoMetadata metadata = mediaDecoder.readMetadata(proxyFile);
        VisualMediaSource videoSource = new VisualMediaSource(proxyFile, mediaDecoder);

        VideoClip.LowResolutionProxyData proxyData = new VideoClip.LowResolutionProxyData(videoSource, metadata);

        clip.setLowResolutionProxy(proxyData);
        messagingService.sendAsyncMessage(new ProgressDoneMessage(jobId));
    }

    @PreferenceValue(name = "low resolution proxy path", defaultValue = "java.io.tmpdir", group = "Preview")
    public void setProxyPath(String path) {
        if (path.equals("java.io.tmpdir")) {
            this.path = System.getProperty("java.io.tmpdir");
        } else {
            this.path = path;
        }
    }

}
