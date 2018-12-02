package com.helospark.tactview.core.decoder.gif;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.opencv.OpenCvImageDecorderDecorator;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.madgag.gif.fmsware.GifDecoder;

@Component
public class GifMediaDecoder implements VisualMediaDecoder {
    private static final int INFINITE_LOOP = 0;
    private GifFileReader gifFileReader;
    private BufferedImageToClipFrameResultConverter imageConverter;

    public GifMediaDecoder(GifFileReader gifFileReader, BufferedImageToClipFrameResultConverter imageConverter) {
        this.gifFileReader = gifFileReader;
        this.imageConverter = imageConverter;
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        GifDecoder gifDecoder = gifFileReader.readFile(request.getFile().getAbsolutePath());
        VisualMediaMetadata metadata = request.getMetadata();

        int loopCount = ((GifVideoMetadata) metadata).getLoopCount();

        int positionInMilliseconds;
        BigDecimal startSeconds = request.getStart().getSeconds();
        BigDecimal lengthSeconds = metadata.getLength().getSeconds();

        if (loopCount == INFINITE_LOOP) {
            positionInMilliseconds = getWrappedMilliseconds(startSeconds, lengthSeconds);
        } else {
            int actualLoopCount = startSeconds.divide(lengthSeconds, 1, RoundingMode.FLOOR).intValue();
            if (actualLoopCount < loopCount) {
                positionInMilliseconds = getWrappedMilliseconds(startSeconds, lengthSeconds);
            } else {
                positionInMilliseconds = startSeconds.multiply(BigDecimal.valueOf(1000)).intValue();
            }
        }

        int currentFramePosition = 0;
        for (int i = 0; i < gifDecoder.getFrameCount(); ++i) {
            int newPosition = currentFramePosition + gifDecoder.getDelay(i);
            if (positionInMilliseconds >= currentFramePosition && positionInMilliseconds < newPosition) {
                return getImageAt(gifDecoder, i, request);
            }
            currentFramePosition = newPosition;
        }

        return getImageAt(gifDecoder, gifDecoder.getFrameCount() - 1, request);
    }

    private int getWrappedMilliseconds(BigDecimal startSeconds, BigDecimal lengthSeconds) {
        int positionInMilliseconds;
        BigDecimal wrappedPosition = startSeconds.remainder(lengthSeconds);
        positionInMilliseconds = wrappedPosition.multiply(BigDecimal.valueOf(1000)).intValue();
        return positionInMilliseconds;
    }

    private MediaDataResponse getImageAt(GifDecoder gifDecoder, int i, VideoMediaDataRequest request) {
        BufferedImage bufferedImage = gifDecoder.getFrame(i);
        BufferedImage scaledImage = scaleImage(bufferedImage, request.getWidth(), request.getHeight());
        ReadOnlyClipImage image = imageConverter.convertFromAbgr(scaledImage);
        return new MediaDataResponse(List.of(image.getBuffer()));
    }

    private BufferedImage scaleImage(BufferedImage bufferedImage, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D grph = (Graphics2D) scaledImage.getGraphics();

        grph.drawImage(bufferedImage, 0, 0, width, height, null);
        grph.dispose();

        return scaledImage;
    }

    @Override
    public GifVideoMetadata readMetadata(File file) {
        GifDecoder gifDecoder = gifFileReader.readFile(file.getAbsolutePath());

        return GifVideoMetadata.builder()
                .withWidth(gifDecoder.getImage().getWidth())
                .withHeight(gifDecoder.getImage().getHeight())
                .withLength(calculateLength(gifDecoder))
                .withNumberOfFrames(gifDecoder.getFrameCount())
                .withLoopCount(gifDecoder.getLoopCount())
                .withResizable(true)
                .build();
    }

    private TimelineLength calculateLength(GifDecoder gifDecoder) {
        TimelineLength length;
        if (gifDecoder.getFrameCount() > 1) {
            int lengthInMilliseconds = 0;
            for (int i = 0; i < gifDecoder.getFrameCount(); ++i) {
                lengthInMilliseconds += gifDecoder.getDelay(i);
            }

            length = TimelineLength.ofMillis(lengthInMilliseconds);
        } else {
            length = OpenCvImageDecorderDecorator.IMAGE_LENGTH;
        }
        return length;
    }

}
