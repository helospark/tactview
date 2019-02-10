package com.helospark.tactview.core.timeline.effect.warp.rasterizer;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class SimpleTriangle {
    SimpleVertex a;
    SimpleVertex b;
    SimpleVertex c;
    ReadOnlyClipImage texture;

    public SimpleTriangle(SimpleVertex a, SimpleVertex b, SimpleVertex c, ReadOnlyClipImage texture) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.texture = texture;
    }

}
