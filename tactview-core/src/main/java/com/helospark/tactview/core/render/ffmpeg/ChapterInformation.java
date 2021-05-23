package com.helospark.tactview.core.render.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class ChapterInformation extends Structure implements Structure.ByReference {
    public long timeInMicroseconds;
    public String name;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("timeInMicroseconds", "name");
    }
}
