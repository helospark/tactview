package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.line;

import java.math.BigDecimal;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class LineDoubleInterpolator implements DoubleInterpolator {
    BigDecimal tangent;
    BigDecimal startValue;

    BigDecimal initialTangent;
    BigDecimal initialStartValue;

    public LineDoubleInterpolator(BigDecimal tangent, BigDecimal startValue) {
        this.tangent = tangent;
        this.startValue = startValue;

        this.initialStartValue = startValue;
        this.tangent = tangent;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return LineInterpolatorFactory.class;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        return position.getSeconds().multiply(tangent).add(startValue).doubleValue();
    }

    @Override
    public DoubleInterpolator deepClone(CloneRequestMetadata cloneRequestMetadata) {
        return new LineDoubleInterpolator(tangent, startValue);
    }

    public BigDecimal getTangent() {
        return tangent;
    }

    public void setTangent(BigDecimal tangent) {
        this.tangent = tangent;
    }

    public BigDecimal getStartValue() {
        return startValue;
    }

    public void setStartValue(BigDecimal startValue) {
        this.startValue = startValue;
    }

    @Override
    public void resetToDefaultValue() {
        this.startValue = initialStartValue;
        this.tangent = initialTangent;
    }

}
