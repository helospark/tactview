package com.helospark.tactview.core.timeline.effect.warp.rasterizer;

import com.helospark.tactview.core.timeline.image.ClipImage;

public class TriangleRasterizationRequest {
    ClipImage result;
    SimpleTriangle triangle;

    public TriangleRasterizationRequest(ClipImage result, SimpleTriangle triangle) {
        this.result = result;
        this.triangle = triangle;
    }

}
