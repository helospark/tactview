package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;

public class ClipFrameResult {
    private ByteBuffer buffer;
    private int width;
    private int height;

    public ClipFrameResult(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void getPixelComponents(int[] result, int x, int y) {
        int r = getRed(x, y);
        int g = getGreen(x, y);
        int b = getBlue(x, y);
        int a = getAlpha(x, y);
        result[0] = r;
        result[1] = g;
        result[2] = b;
        result[3] = a;
    }

    // TODO: move out
    public static int signedToUnsignedByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

    public void setPixel(int[] resultPixel, Integer x, Integer y) {
        setRed(resultPixel[0], x, y);
        setGreen(resultPixel[1], x, y);
        setBlue(resultPixel[2], x, y);
        setAlpha(resultPixel[3], x, y);
    }

    private int saturateIfNeeded(int i) {
        if (i > 255) {
            return 255;
        } else if (i < 0) {
            return 0;
        } else {
            return i;
        }
    }

    public static ClipFrameResult sameSizeAs(ClipFrameResult currentFrame) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(currentFrame.width * currentFrame.height * 4);
        return new ClipFrameResult(result, currentFrame.width, currentFrame.height);
    }

    public static ClipFrameResult fromSize(int tempImageWidth, int tempImageHeight) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(tempImageWidth * tempImageHeight * 4);
        return new ClipFrameResult(result, tempImageWidth, tempImageHeight);
    }

    public void setRed(int red, int x, int y) {
        byte r = (byte) (saturateIfNeeded(red) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + 0, r);
    }

    public void setGreen(int green, int x, int y) {
        byte g = (byte) (saturateIfNeeded(green) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + 1, g);
    }

    public void setBlue(int blue, int x, int y) {
        byte b = (byte) (saturateIfNeeded(blue) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + 2, b);
    }

    public void setAlpha(int alpha, int x, int y) {
        byte a = (byte) (saturateIfNeeded(alpha) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + 3, a);
    }

    public int getRed(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 0));
    }

    public int getGreen(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 1));
    }

    public int getBlue(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 2));
    }

    public int getColorComponentWithOffset(int x, int y, int index) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + index));
    }

    public int getAlpha(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 3));
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public void setColorComponentByOffset(int color, Integer x, Integer y, int offset) {
        byte value = (byte) (saturateIfNeeded(color) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + offset, value);
    }

    public boolean isSameSizeAs(ClipFrameResult displacementMap) {
        return width == displacementMap.getWidth() && height == displacementMap.getHeight();
    }

    public void copyFrom(ClipFrameResult currentFrame) {
        if (!isSameSizeAs(currentFrame)) {
            throw new IllegalArgumentException("Copy requires images to be the same size");
        }
        this.buffer.position(0);
        currentFrame.buffer.position(0);
        this.buffer.put(currentFrame.buffer);
    }

}
