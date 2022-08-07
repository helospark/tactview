package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class DoubleRangeProvider extends CompositeKeyframeableEffect<DoubleRange> {
    DoubleProvider lowEndProvider;
    DoubleProvider highEndProvider;

    public DoubleRangeProvider(DoubleProvider lowEnd, DoubleProvider highEnd) {
        super(List.of(lowEnd, highEnd));
        this.lowEndProvider = lowEnd;
        this.highEndProvider = highEnd;
    }

    public DoubleProvider getLowEnd() {
        return lowEndProvider;
    }

    public DoubleProvider getHighEnd() {
        return highEndProvider;
    }

    public double getMin() {
        return lowEndProvider.getMin();
    }

    public double getMax() {
        return lowEndProvider.getMax();
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<DoubleRange>>> generateSerializableContent() {
        return DoubleRangeProviderFactory.class;
    }

    @Override
    public DoubleRange getValueWithoutScriptAt(TimelinePosition position) {
        return getValueAt(position, null);
    }

    @Override
    public DoubleRange getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            DoubleRange expressionResult = evaluationContext.evaluateExpression(expression, position, DoubleRange.class);
            if (expressionResult == null) {
                return getValueWithoutScriptAt(position);
            } else {
                return expressionResult;
            }
        } else {
            return new DoubleRange(lowEndProvider.getValueAt(position, evaluationContext), highEndProvider.getValueAt(position, evaluationContext));
        }
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, DoubleRange value) {
        lowEndProvider.keyframeAdded(globalTimelinePosition, value.lowEnd);
        highEndProvider.keyframeAdded(globalTimelinePosition, value.highEnd);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public KeyframeableEffect<DoubleRange> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new DoubleRangeProvider(lowEndProvider.deepClone(cloneRequestMetadata), highEndProvider.deepClone(cloneRequestMetadata));
    }

    @Override
    public List<KeyframeableEffect<?>> getChildren() {
        return Arrays.asList(lowEndProvider, highEndProvider);
    }

    public static DoubleRangeProvider createDefaultDoubleRangeProvider(double min, double max, double defaultLow, double defaultHigh) {
        DoubleProvider valueMapperMin = new DoubleProvider(min, max, new MultiKeyframeBasedDoubleInterpolator(defaultLow));
        DoubleProvider valueMapperMax = new DoubleProvider(min, max, new MultiKeyframeBasedDoubleInterpolator(defaultHigh));

        return new DoubleRangeProvider(valueMapperMin, valueMapperMax);
    }

    @Override
    public Class<?> getProvidedType() {
        return DoubleRange.class;
    }
}
