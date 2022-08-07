package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class BooleanProvider extends KeyframeableEffect<Boolean> {
    DoubleInterpolator doubleInterpolator;

    public BooleanProvider(DoubleInterpolator doubleInterpolator) {
        this.doubleInterpolator = doubleInterpolator;
    }

    @Override
    public Boolean getValueWithoutScriptAt(TimelinePosition position) {
        Double value = doubleInterpolator.valueAt(position);
        return value > 0.5;
    }

    @Override
    public Boolean getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            Boolean expressionResult = evaluationContext.evaluateExpression(expression, position, Boolean.class);
            if (expressionResult == null) {
                return getValueWithoutScriptAt(position);
            } else {
                return expressionResult;
            }
        } else {
            return getValueWithoutScriptAt(position);
        }
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, Boolean value) {
        if (doubleInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
            KeyframeSupportingDoubleInterpolator keyframeInterpolator = ((KeyframeSupportingDoubleInterpolator) doubleInterpolator);
            if (value) {
                keyframeInterpolator.valueAddedInternal(globalTimelinePosition, 1.0);
            } else {
                keyframeInterpolator.valueAddedInternal(globalTimelinePosition, 0.0);
            }
        }
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        if (doubleInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
            KeyframeSupportingDoubleInterpolator keyframeInterpolator = ((KeyframeSupportingDoubleInterpolator) doubleInterpolator);
            keyframeInterpolator.valueRemoved(globalTimelinePosition);
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public BooleanProvider deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new BooleanProvider(doubleInterpolator.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<Boolean>>> generateSerializableContent() {
        return BooleanProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return (doubleInterpolator instanceof KeyframeSupportingInterpolator && ((KeyframeSupportingDoubleInterpolator) doubleInterpolator).supportsKeyframes());
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) doubleInterpolator).isUsingKeyframes();
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) doubleInterpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public void setInterpolator(Object interpolator) {
        this.doubleInterpolator = (DoubleInterpolator) interpolator;
    }

    @Override
    public EffectInterpolator getInterpolator() {
        return doubleInterpolator;
    }

    @Override
    public BooleanProvider deepClone(CloneRequestMetadata cloneRequestMetadata) {
        return (BooleanProvider) super.deepClone(cloneRequestMetadata);
    }

    @Override
    public Class<?> getProvidedType() {
        return Boolean.class;
    }
}
