package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;

public interface DoubleInterpolator extends EffectInterpolator {

    @Override
    public Double valueAt(TimelinePosition position);

}
