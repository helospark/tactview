package com.helospark.tactview.core.repository.dynamicvalue;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.DoubleInterpolatorFactory;

public class DynamicInterpolatorFactory implements DoubleInterpolatorFactory {

    @Override
    public DoubleInterpolator createInterpolator(KeyframeableEffect<?> previousInterpolator, DoubleInterpolator doubleInterpolator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean doesSuppert(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<?> getCreatedType() {
        return DynamicInterpolatorFactory.class;
    }

}
