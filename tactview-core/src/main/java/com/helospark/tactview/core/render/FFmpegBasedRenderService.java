package com.helospark.tactview.core.render;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.render.ffmpeg.FFmpegBasedMediaEncoder;
import com.helospark.tactview.core.render.ffmpeg.FFmpegClearEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegEncodeFrameRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegInitEncoderRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
public class FFmpegBasedRenderService extends AbstractRenderService {
    private FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;

    public FFmpegBasedRenderService(TimelineManager timelineManager, FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder) {
        super(timelineManager);
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
    }

    @Override
    public void render(RenderRequest renderRequest) {
        TimelinePosition currentPosition = renderRequest.getStartPosition();

        FFmpegInitEncoderRequest initNativeRequest = new FFmpegInitEncoderRequest();
        initNativeRequest.fileName = renderRequest.getFileName();
        initNativeRequest.framerate = 1 / 30.0;
        initNativeRequest.width = renderRequest.getWidth();
        initNativeRequest.height = renderRequest.getHeight();

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        int frameIndex = 0;
        while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition())) {
            ByteBuffer frame = queryFrameAt(renderRequest, currentPosition);

            FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
            nativeRequest.frames = new FFMpegFrame();
            FFMpegFrame[] array = (FFMpegFrame[]) nativeRequest.frames.toArray(1);
            array[0].data = frame;
            nativeRequest.encoderIndex = encoderIndex;
            nativeRequest.startFrameIndex = frameIndex;

            ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame);
            currentPosition = currentPosition.add(renderRequest.getStep());
            ++frameIndex;
        }

        FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
        clearRequest.encoderIndex = encoderIndex;

        ffmpegBasedMediaEncoder.clearEncoder(clearRequest);
    }

    @Override
    public String getId() {
        return "ffmpegrenderer";
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("mpeg");
    }

    @Override
    public boolean supports(RenderRequest renderRequest) {
        return renderRequest.getFileName().endsWith(".mpeg");
    }

}
