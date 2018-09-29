package com.helospark.tactview.ui.javafx;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.image.Image;

@Service
public class TimelineImagePatternService {
    private MediaDecoder decoder;

    public TimelineImagePatternService(FFmpegBasedMediaDecoderDecorator decoder) {
        this.decoder = decoder;
    }

    public CompletableFuture<Image> createTimelinePattern(String file, VideoMetadata metadata, int timelineWidth) {
        return CompletableFuture.supplyAsync(() -> doIt(file, metadata, timelineWidth));
    }

    private Image doIt(String file, VideoMetadata metadata, int timelineWidth) {
        int scaledWidth = (int) (((double) metadata.getWidth() / metadata.getHeight()) * 50);
        int numberOfFrames = (int) Math.ceil((double) timelineWidth / scaledWidth);
        numberOfFrames = numberOfFrames <= 0 ? 1 : numberOfFrames;
        long frameGap = metadata.getNumberOfFrames() / numberOfFrames;
        if (frameGap <= 0) {
            frameGap = 1;
        }
        BufferedImage result = new BufferedImage(timelineWidth, 50, TYPE_INT_RGB);
        Graphics graphics = result.getGraphics();
        for (int i = 0; i < numberOfFrames; ++i) {
            TimelinePosition start = TimelinePosition.fromFrameIndexWithFps(i * frameGap, metadata.getFps());
            MediaDataRequest request = MediaDataRequest.builder()
                    .withFile(new File(file))
                    .withStart(start)
                    .withNumberOfFrames(1)
                    .withWidth(320) // same res
                    .withHeight(260)
                    .withMetadata(metadata)
                    .withShouldRescale(true)
                    .build();
            BufferedImage bf = ByteBufferToImageConverter.byteBufferToBufferedImage(decoder.readFrames(request).getVideoFrames().get(0), 320, 260);
            java.awt.Image img = bf.getScaledInstance(scaledWidth, 50, BufferedImage.SCALE_FAST);
            graphics.drawImage(img, i * scaledWidth, 0, null);
        }
        return ByteBufferToImageConverter.convertToJavafxImage(result);
    }

}
