package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

/**
 * Use only for debugging purposes by evaluate as expression.
 * @author black
 */
@Deprecated
public class DebugImageRenderer {

    public static void render(ByteBuffer buffer, int width, int height) {
        try {
            ByteBufferToImageConverter converter = new ByteBufferToImageConverter(new IndependentPixelOperation());
            BufferedImage image = converter.byteBufferToBufferedImageWithAlpha(buffer, width, height);
            String filename = "/tmp/debug_" + System.currentTimeMillis();
            ImageIO.write(image, "png", new File(filename));
            System.out.println(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void render(ReadOnlyClipImage image) {
        render(image.getBuffer(), image.getWidth(), image.getHeight());
    }

    public static void render(BufferedImage image) {
        try {
            String filename = "/tmp/debug_" + System.currentTimeMillis();
            File outputfile = new File(filename);
            ImageIO.write(image, "png", outputfile);
            System.out.println(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToString(Object data) {
        String filename = "/tmp/debug_" + System.currentTimeMillis();
        File outputfile = new File(filename);
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile)))) {
            writer.write(data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
