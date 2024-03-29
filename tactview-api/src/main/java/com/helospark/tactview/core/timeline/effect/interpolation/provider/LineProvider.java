package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class LineProvider extends CompositeKeyframeableEffect<InterpolationLine> {
    PointProvider startPointProvider;
    PointProvider endPointProvider;

    public LineProvider(PointProvider startPointProvider, PointProvider endPointProvider) {
        super(List.of(startPointProvider, endPointProvider));
        this.startPointProvider = startPointProvider;
        this.endPointProvider = endPointProvider;
    }

    @Override
    public InterpolationLine getValueWithoutScriptAt(TimelinePosition position) {
        return getValueAt(position, null);
    }

    @Override
    public InterpolationLine getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        InterpolationLine result = null;
        if (expression != null && evaluationContext != null) {
            result = evaluationContext.evaluateExpression(expression, position, InterpolationLine.class);
        }
        if (result == null) {
            Point x = startPointProvider.getValueAt(position, evaluationContext);
            Point y = endPointProvider.getValueAt(position, evaluationContext);
            result = new InterpolationLine(x, y);
        }
        return result;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, InterpolationLine value) {
        this.startPointProvider.keyframeAdded(globalTimelinePosition, value.start);
        this.endPointProvider.keyframeAdded(globalTimelinePosition, value.end);
    }

    @Override
    public List<KeyframeableEffect<?>> getChildren() {
        return Arrays.asList(startPointProvider, endPointProvider);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return startPointProvider.getSizeFunction();
    }

    @Override
    public KeyframeableEffect<InterpolationLine> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new LineProvider(startPointProvider.deepClone(cloneRequestMetadata), endPointProvider.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<InterpolationLine>>> generateSerializableContent() {
        return LineProviderFactory.class;
    }

    public static LineProvider ofNormalizedScreenCoordinates(double x1, double y1, double x2, double y2) {
        return new LineProvider(PointProvider.ofNormalizedImagePosition(x1, y1), PointProvider.ofNormalizedImagePosition(x2, y2));
    }

    @Override
    public Class<?> getProvidedType() {
        return InterpolationLine.class;
    }

}
