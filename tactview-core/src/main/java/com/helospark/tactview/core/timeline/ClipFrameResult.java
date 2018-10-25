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
        int r = signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 0));
        int g = signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 1));
        int b = signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 2));
        int a = signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 3));
        result[0] = r;
        result[1] = g;
        result[2] = b;
        result[3] = a;
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

    public void setPixel(int[] resultPixel, Integer x, Integer y) {
        byte r = (byte) (saturateIfNeeded(resultPixel[0]) & 0xFF);
        byte g = (byte) (saturateIfNeeded(resultPixel[1]) & 0xFF);
        byte b = (byte) (saturateIfNeeded(resultPixel[2]) & 0xFF);
        byte a = (byte) (saturateIfNeeded(resultPixel[3]) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + 0, r);
        buffer.put(y * width * 4 + x * 4 + 1, g);
        buffer.put(y * width * 4 + x * 4 + 2, b);
        buffer.put(y * width * 4 + x * 4 + 3, a);
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
}
