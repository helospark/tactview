package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public interface DoubleInterpolatorFactory {

    public DoubleInterpolator createInterpolator(DoubleInterpolator previousInterpolator);

    public String getId();

    public boolean doesSuppert(String id);

}
