package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed;

public class MixedDoubleInterpolatorElement {
    double value;
    EaseFunction easeFunction;

    public MixedDoubleInterpolatorElement(double value, EaseFunction easeIn) {
        this.value = value;
        this.easeFunction = easeIn;
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

}
