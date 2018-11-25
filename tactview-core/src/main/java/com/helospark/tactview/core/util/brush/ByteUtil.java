package com.helospark.tactview.core.util.brush;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {

    public static int readInt(InputStream inputStream, ByteOrder byteOrder) throws IOException {
        byte[] bytes = new byte[4];
        inputStream.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(byteOrder);
        return buffer.getInt(0);
    }

}
