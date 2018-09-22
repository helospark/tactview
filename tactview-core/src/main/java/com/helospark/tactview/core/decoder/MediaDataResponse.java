package com.helospark.tactview.core.decoder;

import java.nio.ByteBuffer;
import java.util.List;

public class MediaDataResponse {
    private List<ByteBuffer> videoFrames;

    public MediaDataResponse(List<ByteBuffer> videoFrames) {
        this.videoFrames = videoFrames;
    }

    public List<ByteBuffer> getVideoFrames() {
        return videoFrames;
    }

}
