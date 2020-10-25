package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class ImageToLoopbackRequest extends Structure implements Structure.ByReference {
    public int width;
    public int height;
    public ByteBuffer image;
    public String loopbackDevice;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("width", "height", "image", "loopbackDevice");
    }

}
