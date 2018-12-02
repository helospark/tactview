package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.helospark.tactview.core.timeline.ClipFrameResult;

/**
 * Use only for debugging purposes by evaluate as expression.
 * @author black
 */
@Deprecated
public class DebugImageRenderer {

    public static void render(ByteBuffer buffer, int width, int height) {
        try {
            ByteBufferToImageConverter converter = new ByteBufferToImageConverter(new IndependentPixelOperation());
            BufferedImage image = converter.byteBufferToBufferedImage(buffer, width, height);
            String filename = "/tmp/debug_" + System.currentTimeMillis();
            ImageIO.write(image, "png", new File(filename));
            System.out.println(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void render(ClipFrameResult result) {
        render(result.getBuffer(), result.getWidth(), result.getHeight());
    }

}
