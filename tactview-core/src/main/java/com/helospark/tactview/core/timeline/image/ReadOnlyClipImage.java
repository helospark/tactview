package com.helospark.tactview.core.timeline.image;

import java.nio.ByteBuffer;

public interface ReadOnlyClipImage {

    // Offered only for reading
    ByteBuffer getBuffer();

    int getWidth();

    int getHeight();

    void getPixelComponents(int[] result, int x, int y);

    int getRed(int x, int y);

    int getGreen(int x, int y);

    int getBlue(int x, int y);

    int getColorComponentWithOffset(int x, int y, int index);

    int getAlpha(int x, int y);

    boolean inBounds(int x, int y);

    boolean isSameSizeAs(ReadOnlyClipImage displacementMap);

}