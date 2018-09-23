package com.helospark.tactview.ui.javafx;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.image.Image;

@Service
public class TimelineImagePatternService {
    private MediaDecoder decoder;

    public TimelineImagePatternService(MediaDecoder decoder) {
        this.decoder = decoder;
    }

    public CompletableFuture<Image> createTimelinePattern(File file, MediaMetadata metadata, int timelineWidth) {
        return CompletableFuture.supplyAsync(() -> doIt(file, metadata, timelineWidth));
    }

    private Image doIt(File file, MediaMetadata metadata, int timelineWidth) {
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
                    .withFile(file)
                    .withStart(start)
                    .withNumberOfFrames(1)
                    .withWidth(scaledWidth)
                    .withHeight(50)
                    .withMetadata(metadata)
                    .withShouldRescale(true)
                    .build();
            BufferedImage bf = ByteBufferToImageConverter.byteBufferToBufferedImage(decoder.readFrames(request).getVideoFrames().get(0), scaledWidth, 50);
            graphics.drawImage(bf, i * scaledWidth, 0, null);
        }
        return ByteBufferToImageConverter.convertToJavafxImage(result);
    }

}
