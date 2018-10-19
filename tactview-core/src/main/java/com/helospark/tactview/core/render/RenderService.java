package com.helospark.tactview.core.render;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.helospark.tactview.core.render.ffmpeg.FFmpegBasedMediaEncoder;
import com.helospark.tactview.core.render.ffmpeg.FFmpegClearEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegEncodeFrameRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegInitEncoderRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
public class RenderService {
    private TimelineManager timelineManager;
    private FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;

    public RenderService(TimelineManager timelineManager, FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder) {
        this.timelineManager = timelineManager;
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
    }

    public void render(RenderRequest renderRequest) {
        TimelinePosition currentPosition = renderRequest.getStartPosition();

        FFmpegInitEncoderRequest initNativeRequest = new FFmpegInitEncoderRequest();
        initNativeRequest.fileName = "/tmp/test.mpeg";
        initNativeRequest.framerate = 1 / 30.0;
        initNativeRequest.width = renderRequest.getWidth();
        initNativeRequest.height = renderRequest.getHeight();

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        int frameIndex = 0;
        while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition())) {
            TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                    .withFrameBufferSize(1)
                    .withPosition(currentPosition)
                    .withPreviewWidth(renderRequest.getWidth())
                    .withPreviewHeight(renderRequest.getHeight())
                    .withScale(1.0)
                    .build();

            ByteBuffer frame = timelineManager.getSingleFrame(frameRequest);

            FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
            nativeRequest.frames = new FFMpegFrame();
            FFMpegFrame[] array = (FFMpegFrame[]) nativeRequest.frames.toArray(1);
            array[0].data = frame;
            nativeRequest.encoderIndex = encoderIndex;
            nativeRequest.startFrameIndex = frameIndex;

            ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);

            currentPosition = currentPosition.add(renderRequest.getStep());
            ++frameIndex;
        }

        FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
        clearRequest.encoderIndex = encoderIndex;

        ffmpegBasedMediaEncoder.clearEncoder(clearRequest);
    }

}
