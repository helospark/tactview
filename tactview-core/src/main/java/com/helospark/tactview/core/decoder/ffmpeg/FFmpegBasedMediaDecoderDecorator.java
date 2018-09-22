package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.timeline.TimelineLength;

@Component
public class FFmpegBasedMediaDecoderDecorator implements MediaDecoder {
    private FFmpegBasedMediaDecoderImplementation implementation;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation) {
        this.implementation = implementation;
    }

    @Override
    public MediaMetadata readMetadata(File file) {
        if (!file.exists()) {
            throw new RuntimeException(file.getAbsolutePath() + " does not exists");
        }

        FFmpegResult result = implementation.readMediaMetadata(file.getAbsolutePath());
        return MediaMetadata.builder()
                .withFps(result.fps)
                .withHeight(result.height)
                .withWidth(result.width)
                .withLengthInMilliseconds(TimelineLength.ofMicroseconds(result.lengthInMicroseconds))
                .build();
    }

    public MediaDataResponse readFrames(MediaDataRequest request) {

        int numberOfFrames = (int) (request.getLength().getSeconds().doubleValue() * request.getMetadata().getFps());

        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();
        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = request.getFile().getAbsolutePath();
        ffmpegRequest.startMicroseconds = request.getStart().getSeconds().multiply(BigDecimal.valueOf(1000000L)).longValue();

        ByteBuffer[] buffers = new ByteBuffer[numberOfFrames];
        ffmpegRequest.frames = new FFMpegFrame();
        FFMpegFrame[] array = (FFMpegFrame[]) ffmpegRequest.frames.toArray(numberOfFrames);
        for (int i = 0; i < numberOfFrames; ++i) {
            array[i].data = ByteBuffer.allocateDirect(ffmpegRequest.width * ffmpegRequest.height * 4);
            buffers[i] = array[i].data;
        }

        implementation.readFrames(ffmpegRequest);
        return new MediaDataResponse(Arrays.asList(buffers));
    }

}
