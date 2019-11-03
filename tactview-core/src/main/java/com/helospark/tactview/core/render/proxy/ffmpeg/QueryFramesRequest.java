package com.helospark.tactview.core.render.proxy.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class QueryFramesRequest extends Structure implements Structure.ByReference {
    public int jobId;
    public int numberOfFrames;
    public FFmpegFrameWithFrameNumber frames;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("jobId", "numberOfFrames", "frames");
    }

}
