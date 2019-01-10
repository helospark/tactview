package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;

public interface StringInterpolatorFactory {

    public StringInterpolator createInterpolator(StringProvider previousInterpolator);

    public String getId();

    public boolean doesSuppert(String id);
}
