package com.helospark.tactview.core.util.brush;
import java.io.InputStream;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class GimpBrushLoader {
    private static final Logger logger = LoggerFactory.getLogger(GimpBrushLoader.class);
    public static final int GIMP_BRUSH_MAGIC = (('G' << 24) + ('I' << 16) + ('M' << 8) + ('P' << 0));
    public static final int GIMP_BRUSH_MAX_SIZE = 10000;
    public static final int GIMP_BRUSH_MAX_NAME = 256;

    @Cacheable(cacheTimeInMilliseconds = 100000, size = 100)
    public GimpBrush loadBrush(InputStream inputStream) {
        try {
            GimpBrush bh = new GimpBrush();
            bh.header_size = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.version = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.width = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.height = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.bytes = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.magic_number = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);
            bh.spacing = ByteUtil.readInt(inputStream, ByteOrder.BIG_ENDIAN);

            if ((bh.width == 0) || (bh.width > GIMP_BRUSH_MAX_SIZE) ||
                    (bh.height == 0) || (bh.height > GIMP_BRUSH_MAX_SIZE) ||
                    ((bh.bytes != 1) && (bh.bytes != 4))) {
                throw new IllegalArgumentException("Corrupted file");
            }

            int dataSize = bh.width * bh.height * bh.bytes;

            byte[] data = new byte[dataSize];
            inputStream.read(data);

            inputStream.close();
            bh.data = data;

            logger.info("Brush loaded", bh);

            return bh;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
