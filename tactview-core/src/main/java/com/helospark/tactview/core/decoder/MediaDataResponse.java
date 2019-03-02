package com.helospark.tactview.core.decoder;

import java.nio.ByteBuffer;
import java.util.List;

public class MediaDataResponse {
    private List<ByteBuffer> frames;

    public MediaDataResponse(List<ByteBuffer> videoFrames) {
        this.frames = videoFrames;
    }

    public MediaDataResponse(ByteBuffer frame) {
        frames = List.of(frame);
    }

    public List<ByteBuffer> getFrames() {
        return frames;
    }

}
