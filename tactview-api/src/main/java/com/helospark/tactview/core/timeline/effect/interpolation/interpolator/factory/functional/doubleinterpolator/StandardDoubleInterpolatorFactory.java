package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.util.function.Function;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public class StandardDoubleInterpolatorFactory<T extends DoubleInterpolator> implements DoubleInterpolatorFactory {
    private String id;
    private Function<DoubleProvider, T> supplier;
    private Class<T> createdType;

    public StandardDoubleInterpolatorFactory(String id, Class<T> createdType, Function<DoubleProvider, T> supplier) {
        this.id = id;
        this.supplier = supplier;
        this.createdType = createdType;

    }

    @Override
    public DoubleInterpolator createInterpolator(DoubleProvider previousInterpolator) {
        return supplier.apply(previousInterpolator);
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
