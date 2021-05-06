package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

/**
 * Use only for debugging purposes by evaluate as expression.
 * @author black
 */
@Deprecated
public class DebugImageRenderer {

    public static void render(ByteBuffer buffer, int width, int height) {
        try {
            ByteBufferToImageConverter converter = new ByteBufferToImageConverterImpl(new IndependentPixelOperationImpl());
            BufferedImage image = converter.byteBufferToBufferedImageWithAlpha(buffer, width, height);
            String filename = "/tmp/debug_" + System.currentTimeMillis();
            ImageIO.write(image, "png", new File(filename));
            System.out.println(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void render(ByteBuffer buffer, int width, int height, String filename) {
        try {
            ByteBufferToImageConverter converter = new ByteBufferToImageConverterImpl(new IndependentPixelOperationImpl());
            BufferedImage image = converter.byteBufferToBufferedImageWithAlpha(buffer, width, height);
            ImageIO.write(image, "png", new File("/tmp/" + filename));
            System.out.println(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void render(ReadOnlyClipImage image) {
        render(image.getBuffer(), image.getWidth(), image.getHeight());
    }

    @Deprecated
    public static void render(ReadOnlyClipImage image, String filename) {
        render(image.getBuffer(), image.getWidth(), image.getHeight(), filename);
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

    public static void writeAudioDataToString(AudioFrameResult frame) {
        String filename = "/tmp/debug_" + System.currentTimeMillis() + ".txt";
        File outputfile = new File(filename);
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile)))) {
            for (int c = 0; c < frame.getChannels().size(); ++c) {
                for (int i = 0; i < frame.getNumberSamples(); ++i) {
                    int sample = frame.getSampleAt(c, i);
                    writer.append(sample + " ");
                }
                writer.append("\n");
            }

            writer.append("\n\n\n");
            for (int c = 0; c < frame.getChannels().size(); ++c) {
                for (int i = 0; i < frame.getChannels().get(c).capacity(); ++i) {
                    writer.append(AudioFrameResult.unsignedByteToInt(frame.getChannels().get(c).get(i)) + " ");
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
