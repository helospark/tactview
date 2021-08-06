package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.util.function.BiFunction;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public class StandardDoubleInterpolatorFactory<T extends DoubleInterpolator> implements DoubleInterpolatorFactory {
    private String id;
    private BiFunction<KeyframeableEffect<?>, DoubleInterpolator, T> supplier;
    private Class<T> createdType;

    public StandardDoubleInterpolatorFactory(String id, Class<T> createdType, BiFunction<KeyframeableEffect<?>, DoubleInterpolator, T> supplier) {
        this.id = id;
        this.supplier = supplier;
        this.createdType = createdType;

    }

    @Override
    public DoubleInterpolator createInterpolator(KeyframeableEffect<?> previousInterpolator, DoubleInterpolator doubleInterpolator) {
        return supplier.apply(previousInterpolator, doubleInterpolator);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean doesSuppert(String id) {
        return this.id.equals(id);
    }

    @Override
    public Class<T> getCreatedType() {
        return createdType;
    }

}
