package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class ByteBufferToImageConverterImpl implements ByteBufferToImageConverter {
    private IndependentPixelOperationImpl independentPixelOperation;

    public ByteBufferToImageConverterImpl(IndependentPixelOperationImpl independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ByteBufferToImageConverter#frameToBufferedImage(com.helospark.tactview.core.timeline.image.ReadOnlyClipImage)
     */
    @Override
    public BufferedImage frameToBufferedImage(ReadOnlyClipImage currentFrame) {
        return byteBufferToBufferedImage(currentFrame.getBuffer(), currentFrame.getWidth(), currentFrame.getHeight());
    }

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ByteBufferToImageConverter#byteBufferToBufferedImage(java.nio.ByteBuffer, int, int)
     */
    @Override
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

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ByteBufferToImageConverter#byteBufferToBufferedImageWithAlpha(java.nio.ByteBuffer, int, int)
     */
    @Override
    public BufferedImage byteBufferToBufferedImageWithAlpha(ByteBuffer byteBuffer, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int in = y * width * 4 + (x * 4);
            int r = printByte(byteBuffer.get(in + 0));
            int g = printByte(byteBuffer.get(in + 1));
            int b = printByte(byteBuffer.get(in + 2));
            int a = printByte(byteBuffer.get(in + 3));
            bufferedImage.setRGB(x, y, new java.awt.Color(r, g, b, a).getRGB());
        });
        return bufferedImage;
    }

    /* (non-Javadoc)
     * @see com.helospark.tactview.core.util.ByteBufferToImageConverter#printByte(byte)
     */
    @Override
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
