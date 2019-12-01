package com.helospark.tactview.core.render.proxy.compression;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.imagesequence.ImageSequenceDecoderDecorator;
import com.helospark.tactview.core.render.proxy.ffmpeg.FFmpegFrameWithFrameNumber;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;

@Component
public class JpgCompressedImageWriter implements CompressedImageWriter {
    private static final String FILENAME = "image_";
    private static final String EXTENSION = ".jpg";

    private ImageSequenceDecoderDecorator imageSequenceDecoderDecorator;
    private ByteBufferToImageConverter byteBufferToImageConverter;

    public JpgCompressedImageWriter(@Qualifier("regularImageSequenceDecoderDecorator") ImageSequenceDecoderDecorator imageSequenceDecoderDecorator,
            ByteBufferToImageConverter byteBufferToImageConverter) {
        this.imageSequenceDecoderDecorator = imageSequenceDecoderDecorator;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
    }

    @Override
    public void writeCompressedFrame(FFmpegFrameWithFrameNumber frame, File proxyFolder, int width, int height) {
        File file = new File(proxyFolder, FILENAME + frame.frame + EXTENSION);

        BufferedImage image = byteBufferToImageConverter.byteBufferToBufferedImage(frame.data, width, height);

        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

    }

    @Override
    public String getImageNamePattern() {
        return FILENAME + "(\\d+)" + EXTENSION;
    }

    @Override
    public ImageSequenceDecoderDecorator getImageSequenceDecoderDecorator() {
        return imageSequenceDecoderDecorator;
    }

}
