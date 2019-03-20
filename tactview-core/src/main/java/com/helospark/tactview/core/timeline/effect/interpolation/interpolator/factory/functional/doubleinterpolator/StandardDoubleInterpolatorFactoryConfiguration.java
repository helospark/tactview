package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.math.BigDecimal;
import java.util.TreeMap;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.ConstantInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.RandomDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.EaseFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolatorElement;

@Configuration
public class StandardDoubleInterpolatorFactoryConfiguration {

    @Bean
    public StandardDoubleInterpolatorFactory constantInterpolator() {
        return new StandardDoubleInterpolatorFactory("constantInterpolator", previous -> new ConstantInterpolator(0));
    }

    @Bean
    public StandardDoubleInterpolatorFactory randomDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("randomDoubleInterpolator", previous -> new RandomDoubleInterpolator(previous.getMin(), previous.getMax(), BigDecimal.valueOf(1)));
    }

    @Bean
    public StandardDoubleInterpolatorFactory mixedDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("mixedDoubleInterpolator", previous -> {
            TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> values = new TreeMap<>();
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                for (var entry : previous.getValues().entrySet()) {
                    values.put(entry.getKey(), new MixedDoubleInterpolatorElement((Double) entry.getValue(), EaseFunction.LINEAR));
                }
            }
            MixedDoubleInterpolator result = new MixedDoubleInterpolator(values);
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                result.setUseKeyframes(previous.keyframesEnabled());
            }
            return result;
        });
    }

}
