package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.ConstantInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.RandomDoubleInterpolator;

@Configuration
public class StandardDoubleInterpolatorFactoryConfiguration {

    @Bean
    public StandardDoubleInterpolatorFactory constantInterpolator() {
        return new StandardDoubleInterpolatorFactory("constantInterpolator", previous -> new ConstantInterpolator(0));
    }

    @Bean
    public StandardDoubleInterpolatorFactory randomDoubleInterpolator() {
        return new StandardDoubleInterpolatorFactory("randomDoubleInterpolator", previous -> new RandomDoubleInterpolator(0, 100, BigDecimal.valueOf(1)));
    }

}
