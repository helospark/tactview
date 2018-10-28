package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;

public interface EffectInterpolator {

    public Object valueAt(TimelinePosition position);

    public EffectInterpolator cloneInterpolator();

}
