package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.math.BigDecimal;
import java.util.TreeMap;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.ConstantInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.RandomDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.EaseFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolatorElement;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.line.LineDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.sine.SineDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.bezier.CubicBezierPoint;

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

    @Bean
    public StandardDoubleInterpolatorFactory bezierDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("bezierDoubleInterpolator", previous -> {
            TreeMap<TimelinePosition, CubicBezierPoint> values = new TreeMap<>();
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                for (var entry : previous.getValues().entrySet()) {
                    values.put(entry.getKey(), new CubicBezierPoint((Double) entry.getValue(), new Point(-1, 0), new Point(1, 0)));
                }
            }
            BezierDoubleInterpolator result = new BezierDoubleInterpolator(values);
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                result.setUseKeyframes(previous.keyframesEnabled());
            }
            return result;
        });
    }

    @Bean
    public StandardDoubleInterpolatorFactory multiKeyframeDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("multiKeyframeDoubleInterpolator", previous -> {
            TreeMap<TimelinePosition, Double> values = new TreeMap<>();
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                for (var entry : previous.getValues().entrySet()) {
                    values.put(entry.getKey(), (double) entry.getValue());
                }
            }
            MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(values);
            if (previous.getInterpolatorClone() instanceof KeyframeSupportingDoubleInterpolator) {
                result.setUseKeyframes(previous.keyframesEnabled());
            }
            return result;
        });
    }

    @Bean
    public StandardDoubleInterpolatorFactory sineDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("sineDoubleInterpolator", previous -> {
            return SineDoubleInterpolator.builder()
                    .withFrequency(1.0)
                    .withMinValue(-1.0)
                    .withMaxValue(1.0)
                    .withStartOffset(0.0)
                    .build();
        });
    }

    @Bean
    public StandardDoubleInterpolatorFactory lineDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("lineDoubleInterpolator", previous -> {
            return new LineDoubleInterpolator(BigDecimal.ONE, BigDecimal.ZERO);
        });
    }
}
