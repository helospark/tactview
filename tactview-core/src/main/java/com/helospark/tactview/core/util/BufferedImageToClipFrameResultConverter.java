package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;

@Component
public class BufferedImageToClipFrameResultConverter {
    private IndependentPixelOperation independentPixelOperation;

    public BufferedImageToClipFrameResultConverter(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipFrameResult convertFromAbgr(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4);
        ClipFrameResult frameResult = new ClipFrameResult(buffer, width, height);

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

}
