package com.helospark.tactview.core.render;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.render.ffmpeg.FFmpegBasedMediaEncoder;
import com.helospark.tactview.core.render.ffmpeg.FFmpegClearEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegEncodeFrameRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegInitEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.RenderFFMpegFrame;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FFmpegBasedRenderService extends AbstractRenderService {
    private FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;

    public FFmpegBasedRenderService(TimelineManager timelineManager, FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder, MessagingService messagingService) {
        super(timelineManager, messagingService);
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
    }

    @Override
    public void renderInternal(RenderRequest renderRequest) {
        TimelinePosition currentPosition = renderRequest.getStartPosition();

        FFmpegInitEncoderRequest initNativeRequest = new FFmpegInitEncoderRequest();
        initNativeRequest.fileName = renderRequest.getFileName();
        initNativeRequest.fps = renderRequest.getFps();
        initNativeRequest.renderWidth = renderRequest.getWidth();
        initNativeRequest.renderHeight = renderRequest.getHeight();

        AudioVideoFragment tmpFrame = queryFrameAt(renderRequest, currentPosition);//tmp solution

        initNativeRequest.actualWidth = tmpFrame.getVideoResult().getWidth();
        initNativeRequest.actualHeight = tmpFrame.getVideoResult().getHeight();
        initNativeRequest.bytesPerSample = tmpFrame.getAudioResult().getBytesPerSample();
        initNativeRequest.audioChannels = tmpFrame.getAudioResult().getChannels().size();
        initNativeRequest.sampleRate = tmpFrame.getAudioResult().getSamplePerSecond();
        // frame not freed

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        int frameIndex = 0;
        while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition())) {
            AudioVideoFragment frame = queryFrameAt(renderRequest, currentPosition);

            FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
            nativeRequest.frame = new RenderFFMpegFrame();
            RenderFFMpegFrame[] array = (RenderFFMpegFrame[]) nativeRequest.frame.toArray(1);
            array[0].imageData = frame.getVideoResult().getBuffer();
            if (frame.getAudioResult().getChannels().size() > 0) {
                array[0].audioData = convertAudio(frame.getAudioResult());
                array[0].numberOfAudioSamples = frame.getAudioResult().getNumberSamples();
            }
            nativeRequest.encoderIndex = encoderIndex;
            nativeRequest.startFrameIndex = frameIndex;

            ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getVideoResult().getBuffer());
            for (var buffer : frame.getAudioResult().getChannels()) {
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(buffer);
            }

            currentPosition = currentPosition.add(renderRequest.getStep());
            messagingService.sendAsyncMessage(new ProgressAdvancedMessage(renderRequest.getRenderId(), 1));
            ++frameIndex;
        }

        FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
        clearRequest.encoderIndex = encoderIndex;

        ffmpegBasedMediaEncoder.clearEncoder(clearRequest);
    }

    private ByteBuffer convertAudio(AudioFrameResult audioResult) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(audioResult.getBytesPerSample() * audioResult.getNumberSamples() * audioResult.getChannels().size());

        int index = 0;
        for (int i = 0; i < audioResult.getNumberSamples(); ++i) {
            for (int j = 0; j < audioResult.getChannels().size(); ++j) {
                for (int k = 0; k < audioResult.getBytesPerSample(); ++k) {
                    result.put(index++, audioResult.getChannels().get(j).get(i * audioResult.getBytesPerSample() + k));
                }
            }
        }

        return result;
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
        //        return renderRequest.getFileName().endsWith(".mpeg");
        return true;
    }

}
