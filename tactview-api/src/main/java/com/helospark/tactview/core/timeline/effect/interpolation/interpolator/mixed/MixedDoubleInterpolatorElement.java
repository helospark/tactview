package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MixedDoubleInterpolatorElement {
    double value;
    EaseFunction easeFunction;

    public MixedDoubleInterpolatorElement(@JsonProperty("value") double value, @JsonProperty("easeFunction") EaseFunction easeFunction) {
        this.value = value;
        this.easeFunction = easeFunction;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public EaseFunction getEaseFunction() {
        return easeFunction;
    }

    public void setEaseFunction(EaseFunction easeFunction) {
        this.easeFunction = easeFunction;
    }

    public MixedDoubleInterpolatorElement butWithPoint(double newValue) {
        return new MixedDoubleInterpolatorElement(newValue, easeFunction);
    }

    public MixedDoubleInterpolatorElement deepClone() {
        return new MixedDoubleInterpolatorElement(value, easeFunction);
    }

    @Override
    public String toString() {
        return "MixedDoubleInterpolatorElement [value=" + value + ", easeFunction=" + easeFunction + "]";
    }

}
