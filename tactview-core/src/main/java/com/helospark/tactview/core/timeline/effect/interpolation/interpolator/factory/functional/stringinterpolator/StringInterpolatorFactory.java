package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;

public interface StringInterpolatorFactory {

    public StringInterpolator createInterpolator(StringInterpolator previousInterpolator);

    public String getId();

    public boolean doesSuppert(String id);
}
