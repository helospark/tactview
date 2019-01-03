package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.impl;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.TypingStringInterpolator;

@Configuration
public class StandardStringInterpolatorFactoryConfiguration {

    @Bean
    public StandardStringInterpolatorFactory stringStepInterpolatorFactory() {
        return new StandardStringInterpolatorFactory("stringStepInterpolator", previous -> {
            return new StepStringInterpolator(previous.getDefaultValue(), getValues(previous), previous.useKeyframes());
        });
    }

    @Bean
    public StandardStringInterpolatorFactory typingStringInterpolatorFactory() {
        return new StandardStringInterpolatorFactory("typingStringInterpolator", previous -> {
            return new TypingStringInterpolator(new StepStringInterpolator(previous.getDefaultValue(), getValues(previous), previous.useKeyframes()));
        });
    }

    private TreeMap<TimelinePosition, String> getValues(StringInterpolator previous) {
        return previous.getValues()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        value -> value.getValue().toString(),
                        (oldValue, newValue) -> newValue,
                        TreeMap::new));
    }

}
