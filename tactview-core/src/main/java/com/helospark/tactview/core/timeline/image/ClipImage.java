package com.helospark.tactview.core.timeline.image;

import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.util.MathUtil;

public class ClipImage implements ReadOnlyClipImage {
    private ByteBuffer buffer;
    private int width;
    private int height;

    public ClipImage(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
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

    public static ClipImage sameSizeAs(ReadOnlyClipImage currentFrame) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(currentFrame.getWidth() * currentFrame.getHeight() * 4);
        return new ClipImage(result, currentFrame.getWidth(), currentFrame.getHeight());
    }

    public static ClipImage fromSize(int tempImageWidth, int tempImageHeight) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(tempImageWidth * tempImageHeight * 4);
        return new ClipImage(result, tempImageWidth, tempImageHeight);
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

    @Override
    public int getRed(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 0));
    }

    @Override
    public int getGreen(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 1));
    }

    @Override
    public int getBlue(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 2));
    }

    @Override
    public int getColorComponentWithOffset(int x, int y, int index) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + index));
    }

    @Override
    public int getColorComponentWithOffsetUsingInterpolation(double x, double y, int index) {
        if (!inBounds((int) x, (int) y)) {
            return 0;
        }
        int lowX = (int) x;
        int lowY = (int) y;

        int highX = (int) Math.ceil(x);
        int highY = (int) Math.ceil(y);

        double xDistanceNormalized = x - lowX;
        double yDistanceNormalized = y - lowY;

        int topLeftColor = getColorComponentWithOffset(lowX, lowY, index);
        int bottomLeftColor = highY < height ? getColorComponentWithOffset(lowX, highY, index) : topLeftColor;
        int topRightColor = highX < width ? getColorComponentWithOffset(highX, lowY, index) : topLeftColor;
        int bottomRightColor = highX < width && highY < height ? getColorComponentWithOffset(highX, highY, index) : topRightColor;

        return (int) MathUtil.bilinearInterpolate(topLeftColor, topRightColor, bottomLeftColor, bottomRightColor, xDistanceNormalized, yDistanceNormalized);
    }

    @Override
    public int getAlpha(int x, int y) {
        return signedToUnsignedByte(buffer.get(y * width * 4 + x * 4 + 3));
    }

    @Override
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public void setColorComponentByOffset(int color, int x, int y, int offset) {
        byte value = (byte) (saturateIfNeeded(color) & 0xFF);
        buffer.put(y * width * 4 + x * 4 + offset, value);
    }

    @Override
    public boolean isSameSizeAs(ReadOnlyClipImage displacementMap) {
        return width == displacementMap.getWidth() && height == displacementMap.getHeight();
    }

    public ClipImage copyFrom(ReadOnlyClipImage currentFrame) {
        if (!isSameSizeAs(currentFrame)) {
            throw new IllegalArgumentException("Copy requires images to be the same size");
        }
        this.buffer.position(0);
        currentFrame.getBuffer().position(0); // is it really readonly?
        this.buffer.put(currentFrame.getBuffer());
        return this;
    }

    public void copyColorFromTo(ReadOnlyClipImage from, int fromX, int fromY, ClipImage to, int toX, int toY) {
        for (int i = 0; i < 4; ++i) {
            byte component = from.getBuffer().get(fromY * width * 4 + fromX * 4 + i);
            to.buffer.put(toY * to.width * 4 + toX * 4 + i, component);
        }
    }

    public void returnBuffer() {
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(buffer);
    }

}
