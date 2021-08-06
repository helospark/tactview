package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public interface DoubleInterpolatorFactory {

    public DoubleInterpolator createInterpolator(KeyframeableEffect<?> previousInterpolator, DoubleInterpolator doubleInterpolator);

    public String getId();

    public boolean doesSuppert(String id);

    public Class<?> getCreatedType();

}
