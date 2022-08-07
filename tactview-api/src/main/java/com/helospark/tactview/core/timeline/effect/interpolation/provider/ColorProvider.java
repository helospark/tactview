package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class ColorProvider extends CompositeKeyframeableEffect<Color> {
    protected DoubleProvider redProvider;
    protected DoubleProvider greenProvider;
    protected DoubleProvider blueProvider;

    public ColorProvider(DoubleProvider redProvider, DoubleProvider greenProvider, DoubleProvider blueProvider) {
        super(List.of(redProvider, greenProvider, blueProvider));
        this.redProvider = redProvider;
        this.greenProvider = greenProvider;
        this.blueProvider = blueProvider;
    }

    @Override
    public Color getValueAt(TimelinePosition position) {
        return new Color(redProvider.getValueAt(position), greenProvider.getValueAt(position), blueProvider.getValueAt(position));
    }

    @Override
    public Color getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            Color expressionResult = evaluationContext.evaluateExpression(expression, position, Color.class);
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
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect<?>> getChildren() {
        return Arrays.asList(redProvider, greenProvider, blueProvider);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, Color value) {
        this.redProvider.keyframeAdded(globalTimelinePosition, value.red);
        this.greenProvider.keyframeAdded(globalTimelinePosition, value.green);
        this.blueProvider.keyframeAdded(globalTimelinePosition, value.blue);
    }

    @Override
    public KeyframeableEffect<Color> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new ColorProvider(redProvider.deepClone(cloneRequestMetadata),
                greenProvider.deepClone(cloneRequestMetadata),
                blueProvider.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<Color>>> generateSerializableContent() {
        return ColorProviderFactory.class;
    }

    public static ColorProvider fromDefaultValue(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public ColorProvider deepClone(CloneRequestMetadata cloneRequestMetadata) {
        return (ColorProvider) super.deepClone(cloneRequestMetadata);
    }

    @Override
    public Class<?> getProvidedType() {
        return Color.class;
    }
}
