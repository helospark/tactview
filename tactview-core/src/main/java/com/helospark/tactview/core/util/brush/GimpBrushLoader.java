package com.helospark.tactview.core.util.brush;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class GimpBrushLoader {
    public static final int GIMP_BRUSH_MAGIC = (('G' << 24) + ('I' << 16) + ('M' << 8) + ('P' << 0));
    public static final int GIMP_BRUSH_MAX_SIZE = 10000;
    public static final int GIMP_BRUSH_MAX_NAME = 256;

    @Cacheable(cacheTimeInMilliseconds = 100000, size = 100)
    public GimpBrushHeader loadBrush(String filename) {
        try {
            InputStream is = new FileInputStream(new File(filename)); // "/media/dmeg/Brushimages_ALL_GBR/Fuzzy/Fuzzy_001.gbr"

            GimpBrushHeader bh = new GimpBrushHeader();
            bh.header_size = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.version = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.width = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.height = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.bytes = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.magic_number = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);
            bh.spacing = ByteUtil.readInt(is, ByteOrder.BIG_ENDIAN);

            if ((bh.width == 0) || (bh.width > GIMP_BRUSH_MAX_SIZE) ||
                    (bh.height == 0) || (bh.height > GIMP_BRUSH_MAX_SIZE) ||
                    ((bh.bytes != 1) && (bh.bytes != 4))) {
                throw new IllegalArgumentException("Corrupted file");
            }

            int dataSize = bh.width * bh.height * bh.bytes;

            byte[] data = new byte[dataSize];
            is.read(data);

            is.close();
            bh.data = data;

            System.out.println(bh);

            return bh;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
