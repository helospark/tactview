package com.helospark.tactview.ui.javafx.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

import com.helospark.lightdi.annotation.Component;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

@Component
public class ByteBufferToJavaFxImageConverter {

    public Image convertToJavafxImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public Image convertToJavafxImage(ByteBuffer frame, int width, int height) {
        ByteBuffer bgraImage;
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) && ((width * height * 4) % 8 == 0)) {
            bgraImage = longRgbaToBgra(frame, width, height);

        } else {
            bgraImage = rgbaToBgra(frame, width, height);
        }

        return new WritableImage(new PixelBuffer<>(width, height, bgraImage, WritablePixelFormat.getByteBgraPreInstance()));
    }

    private ByteBuffer rgbaToBgra(ByteBuffer frame, int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);

        for (int i = 0; i < frame.capacity(); i += 4) {
            buffer.put(i + 0, frame.get(i + 2));
            buffer.put(i + 1, frame.get(i + 1));
            buffer.put(i + 2, frame.get(i + 0));
            buffer.put(i + 3, frame.get(i + 3));
        }
        buffer.position(0);
        return buffer;
    }

    // Roughly 30-50% faster than the byte based implementation
    private ByteBuffer longRgbaToBgra(ByteBuffer frame, int width, int height) {
        ByteBuffer byteDstBuffer = ByteBuffer.allocate(width * height * 4);
        byteDstBuffer.order(ByteOrder.nativeOrder());
        LongBuffer longDstBuffer = byteDstBuffer.asLongBuffer();
        LongBuffer longSrcBuffer = frame.asLongBuffer();

        for (int i = 0; i < width * height / 2; ++i) {
            long data = longSrcBuffer.get(i);
            long result = ((data & 0xFF00FF00_FF00FF00L)) |
                    ((data & 0x000000FF_000000FFL) << 16) |
                    ((data & 0x00FF0000_00FF0000L) >> 16);
            longDstBuffer.put(result);
        }

        longDstBuffer.position(0);
        return byteDstBuffer;
    }

}
