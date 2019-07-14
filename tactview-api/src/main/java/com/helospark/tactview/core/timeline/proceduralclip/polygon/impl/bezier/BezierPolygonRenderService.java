package com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface BezierPolygonRenderService {
    public ReadOnlyClipImage drawBezierPolygon(BezierPolygonRenderServiceRequest request);
}