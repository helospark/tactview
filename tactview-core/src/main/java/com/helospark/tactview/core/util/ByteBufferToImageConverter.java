package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;

@Component
public class ByteBufferToImageConverter {
    private IndependentPixelOperation independentPixelOperation;

    public ByteBufferToImageConverter(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public BufferedImage byteBufferToBufferedImage(ByteBuffer byteBuffer, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int in = y * width * 4 + (x * 4);
            int r = printByte(byteBuffer.get(in + 0));
            int g = printByte(byteBuffer.get(in + 1));
            int b = printByte(byteBuffer.get(in + 2));
            bufferedImage.setRGB(x, y, new java.awt.Color(r, g, b).getRGB());
        });
        return bufferedImage;
    }

    public int printByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

}
