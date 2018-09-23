package com.helospark.tactview.ui.javafx;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ByteBufferToImageConverter {

    public static Image convertToJavafxImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static BufferedImage byteBufferToBufferedImage(ByteBuffer byteBuffer, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int in = i * width * 4 + (j * 4);
                int r = printByte(byteBuffer.get(in + 0));
                int g = printByte(byteBuffer.get(in + 1));
                int b = printByte(byteBuffer.get(in + 2));
                bufferedImage.setRGB(j, i, new java.awt.Color(r, g, b).getRGB());
            }
        }
        return bufferedImage;
    }

    public static int printByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

    public static Image convertToJavaxImage(ByteBuffer frame, int width, int height) {
        BufferedImage bufferedImage = byteBufferToBufferedImage(frame, width, height);
        return convertToJavafxImage(bufferedImage);
    }
}
