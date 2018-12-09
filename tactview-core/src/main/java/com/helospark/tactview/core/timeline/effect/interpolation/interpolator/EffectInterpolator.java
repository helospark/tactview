package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.StatefulCloneable;

public interface EffectInterpolator extends StatefulCloneable<EffectInterpolator>, SavedContentAddable<EffectInterpolator> {

    public Object valueAt(TimelinePosition position);

    @Override
    public EffectInterpolator deepClone();

}
