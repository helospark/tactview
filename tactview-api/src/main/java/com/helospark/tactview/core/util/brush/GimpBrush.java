package com.helospark.tactview.core.util.brush;

import java.util.Arrays;

public class GimpBrush {
    public int header_size;
    public int version;
    public int width;
    public int height;
    public int bytes;
    public int magic_number;
    public int spacing;
    public byte[] data;

    @Override
    public String toString() {
        return "GimpBrushHeader [header_size=" + header_size + ", version=" + version + ", width=" + width + ", height=" + height + ", bytes=" + bytes + ", magic_number=" + magic_number + ", spacing="
                + spacing + ", data=" + Arrays.toString(data) + "]";
    }

}
