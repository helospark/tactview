package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface ByteBufferToImageConverter {

    BufferedImage frameToBufferedImage(ReadOnlyClipImage currentFrame);

    BufferedImage byteBufferToBufferedImage(ByteBuffer byteBuffer, int width, int height);

    BufferedImage byteBufferToBufferedImageWithAlpha(ByteBuffer byteBuffer, int width, int height);

    int printByte(byte b);

}