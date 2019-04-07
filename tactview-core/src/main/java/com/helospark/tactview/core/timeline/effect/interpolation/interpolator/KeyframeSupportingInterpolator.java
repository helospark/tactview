package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

public interface KeyframeSupportingInterpolator {

    public void setUseKeyframes(boolean useKeyframes);

    public boolean isUsingKeyframes();

    public default boolean supportsKeyframes() {
        return true;
    }
}
