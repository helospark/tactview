package com.helospark.tactview.ui.javafx.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

@Component
public class ByteBufferToJavaFxImageConverter {
    private ByteBufferToImageConverter converter;

    public ByteBufferToJavaFxImageConverter(ByteBufferToImageConverter converter) {
        this.converter = converter;
    }

    public Image convertToJavafxImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public Image convertToJavafxImage(ByteBuffer frame, int width, int height) {
        BufferedImage bufferedImage = converter.byteBufferToBufferedImage(frame, width, height);
        return convertToJavafxImage(bufferedImage);
    }

}
