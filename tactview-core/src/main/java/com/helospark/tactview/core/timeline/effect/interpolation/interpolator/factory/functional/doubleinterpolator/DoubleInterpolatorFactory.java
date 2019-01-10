package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public interface DoubleInterpolatorFactory {

    public DoubleInterpolator createInterpolator(DoubleProvider previousInterpolator);

    public String getId();

    public boolean doesSuppert(String id);

}
