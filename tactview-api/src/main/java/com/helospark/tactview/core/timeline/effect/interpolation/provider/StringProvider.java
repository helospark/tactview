package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class StringProvider extends KeyframeableEffect<String> {
    StringInterpolator stringInterpolator;

    public StringProvider(StringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
    }

    @Override
    public String getValueAt(TimelinePosition position) {
        return stringInterpolator.valueAt(position);
    }

    @Override
    public String getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null) {
            String expressionResult = evaluationContext.evaluateExpression(expression, position, String.class);
            if (expressionResult == null) {
                return getValueAt(position);
            } else {
                return expressionResult;
            }
        } else {
            return getValueAt(position);
        }
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

    @Override
    public KeyframeableEffect deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new StringProvider(stringInterpolator.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return StringProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return stringInterpolator instanceof KeyframeSupportingInterpolator;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) stringInterpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) stringInterpolator).isUsingKeyframes();
    }

    @Override
    public void setInterpolator(Object previousInterpolator) {
        this.stringInterpolator = (StringInterpolator) previousInterpolator;
    }

    @Override
    public StringInterpolator getInterpolatorClone() {
        return this.stringInterpolator;
    }
}
