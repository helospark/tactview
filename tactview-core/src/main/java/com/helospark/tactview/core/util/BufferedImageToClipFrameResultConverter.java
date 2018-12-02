package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class BufferedImageToClipFrameResultConverter {
    private IndependentPixelOperation independentPixelOperation;

    public BufferedImageToClipFrameResultConverter(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage convertFromAbgr(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4);
        ClipImage frameResult = new ClipImage(buffer, width, height);

        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int a = signedToUnsignedByte(pixels[y * width * 4 + x * 4 + 0]);
            int b = signedToUnsignedByte(pixels[y * width * 4 + x * 4 + 1]);
            int g = signedToUnsignedByte(pixels[y * width * 4 + x * 4 + 2]);
            int r = signedToUnsignedByte(pixels[y * width * 4 + x * 4 + 3]);

            frameResult.setRed(r, x, y);
            frameResult.setGreen(g, x, y);
            frameResult.setBlue(b, x, y);
            frameResult.setAlpha(a, x, y);
        });

        return frameResult;
    }

    private int signedToUnsignedByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

    public ReadOnlyClipImage convertFromIntArgb(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4);
        ClipImage frameResult = new ClipImage(buffer, width, height);

        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int pixel = pixels[y * width + x];
            int a = ((pixel & 0x000000ff) >> 0);
            int r = ((pixel & 0x0000ff00) >> 8);
            int g = ((pixel & 0x00ff0000) >> 16);
            int b = ((pixel & 0xff000000) >> 24);

            frameResult.setRed(a, x, y);
            frameResult.setGreen(r, x, y);
            frameResult.setBlue(g, x, y);
            frameResult.setAlpha(255, x, y);
        });

        return frameResult;
    }

}
