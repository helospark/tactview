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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.square.SquareDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.bezier.CubicBezierPoint;

@Configuration
public class StandardDoubleInterpolatorFactoryConfiguration {

    @Bean
    public StandardDoubleInterpolatorFactory<?> constantInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("constantInterpolator", ConstantInterpolator.class, (previousProvider, previousInterpolator) -> new ConstantInterpolator(0));
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> randomDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("randomDoubleInterpolator", RandomDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    double min, max;
                    if (previousProvider instanceof DoubleProvider) {
                        var dPrev = ((DoubleProvider) previousProvider);
                        min = dPrev.getMin();
                        max = dPrev.getMax();

                        if (!dPrev.hasRangeSet()) {
                            min = 0.0;
                            max = 1.0;
                        }
                    } else {
                        min = 0;
                        max = 256;
                    }

                    return new RandomDoubleInterpolator(min, max, BigDecimal.valueOf(1));
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> mixedDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("mixedDoubleInterpolator", MixedDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    TreeMap<TimelinePosition, MixedDoubleInterpolatorElement> values = new TreeMap<>();
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        for (var entry : ((KeyframeSupportingDoubleInterpolator) previousInterpolator).getValues().entrySet()) {
                            values.put(entry.getKey(), new MixedDoubleInterpolatorElement((Double) entry.getValue(), EaseFunction.LINEAR));
                        }
                    }
                    MixedDoubleInterpolator result = new MixedDoubleInterpolator(values);
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        result.setUseKeyframes(((KeyframeSupportingDoubleInterpolator) previousInterpolator).isUsingKeyframes());
                    }
                    return result;
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> bezierDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("bezierDoubleInterpolator", BezierDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    TreeMap<TimelinePosition, CubicBezierPoint> values = new TreeMap<>();
                    double defaultValue = 0.0;
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        for (var entry : ((KeyframeSupportingDoubleInterpolator) previousInterpolator).getValues().entrySet()) {
                            values.put(entry.getKey(), new CubicBezierPoint((Double) entry.getValue(), new Point(-1, 0), new Point(1, 0)));
                        }
                        defaultValue = ((KeyframeSupportingDoubleInterpolator) previousInterpolator).getDefaultValue();
                    }
                    BezierDoubleInterpolator result = new BezierDoubleInterpolator(defaultValue, values);
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        result.setUseKeyframes(((KeyframeSupportingDoubleInterpolator) previousInterpolator).isUsingKeyframes());
                    }
                    return result;
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> multiKeyframeDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("multiKeyframeDoubleInterpolator", MultiKeyframeBasedDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    TreeMap<TimelinePosition, Double> values = new TreeMap<>();
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        for (var entry : ((KeyframeSupportingDoubleInterpolator) previousInterpolator).getValues().entrySet()) {
                            values.put(entry.getKey(), (double) entry.getValue());
                        }
                    }
                    MultiKeyframeBasedDoubleInterpolator result = new MultiKeyframeBasedDoubleInterpolator(values);
                    if (previousInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
                        result.setUseKeyframes(((KeyframeSupportingDoubleInterpolator) previousInterpolator).isUsingKeyframes());
                    }
                    return result;
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> sineDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("sineDoubleInterpolator", SineDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    return SineDoubleInterpolator.builder()
                            .withFrequency(1.0)
                            .withMinValue(-1.0)
                            .withMaxValue(1.0)
                            .withStartOffset(0.0)
                            .build();
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> squareDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("squareDoubleInterpolator", SquareDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    return SquareDoubleInterpolator.builder()
                            .withMinValue(-1.0)
                            .withMaxValue(1.0)
                            .withOnTime(1.0)
                            .withOffTime(1.0)
                            .build();
                });
    }

    @Bean
    public StandardDoubleInterpolatorFactory<?> lineDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory<>("lineDoubleInterpolator", LineDoubleInterpolator.class,
                (previousProvider, previousInterpolator) -> {
                    return new LineDoubleInterpolator(BigDecimal.ONE, BigDecimal.ZERO);
                });
    }
}
