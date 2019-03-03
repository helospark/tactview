package com.helospark.tactview.core.decoder;

import java.nio.ByteBuffer;
import java.util.List;

public class MediaDataResponse {
    private List<ByteBuffer> frames;

    public MediaDataResponse(ByteBuffer frame) {
        this.frames = List.of(frame);
    }

    public MediaDataResponse(List<ByteBuffer> frame) {
        this.frames = frame;
    }

    public ByteBuffer getFrame() {
        return frames.get(0);
    }

    public List<ByteBuffer> getFrames() {
        return frames;
    }

}
