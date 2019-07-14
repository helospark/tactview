package com.helospark.tactview.core.util.brush;

import com.helospark.tactview.core.timeline.image.ClipImage;

public interface ScaledBrushProviderInterface {

    ClipImage getBrushImage(GetBrushRequest brushRequest);

}