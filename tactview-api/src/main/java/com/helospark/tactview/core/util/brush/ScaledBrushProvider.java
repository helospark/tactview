package com.helospark.tactview.core.util.brush;

import com.helospark.tactview.core.timeline.image.ClipImage;

public interface ScaledBrushProvider {

    ClipImage getBrushImage(GetBrushRequest brushRequest);

}