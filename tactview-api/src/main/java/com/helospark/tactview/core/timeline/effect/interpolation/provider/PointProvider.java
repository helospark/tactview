package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class PointProvider extends CompositeKeyframeableEffect<Point> {
    DoubleProvider xProvider;
    DoubleProvider yProvider;

    public PointProvider(DoubleProvider xProvider, DoubleProvider yProvider) {
        super(List.of(xProvider, yProvider));
        this.xProvider = xProvider;
        this.yProvider = yProvider;
    }

    @Override
    public Point getValueAt(TimelinePosition position) {
        double x = xProvider.getValueAt(position);
        double y = yProvider.getValueAt(position);
        return new Point(x, y);
    }

    @Override
    public Point getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            Point expressionResult = evaluationContext.evaluateExpression(expression, position, Point.class);
            if (expressionResult == null) {
                return getValueAt(position);
            } else {
                return expressionResult;
            }
        } else {
            return getValueAt(position);
        }
    }

    public DoubleProvider getxProvider() {
        return xProvider;
    }

    public DoubleProvider getyProvider() {
        return yProvider;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect<?>> getChildren() {
        return Arrays.asList(xProvider, yProvider);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, Point value) {
        this.xProvider.keyframeAdded(globalTimelinePosition, value.x);
        this.yProvider.keyframeAdded(globalTimelinePosition, value.y);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return xProvider.getSizeFunction();
    }

    @Override
    public PointProvider deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new PointProvider(xProvider.deepClone(cloneRequestMetadata), yProvider.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<Point>>> generateSerializableContent() {
        return PointProviderFactory.class;
    }

    public static PointProvider ofNormalizedImagePosition(double d, double e) {
        return new PointProvider(new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new BezierDoubleInterpolator(d)),
                new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new BezierDoubleInterpolator(e)));
    }

    public static PointProvider of(double x, double y) {
        return new PointProvider(new DoubleProvider(new BezierDoubleInterpolator(x)),
                new DoubleProvider(new BezierDoubleInterpolator(y)));
    }

    @Override
    public PointProvider deepClone(CloneRequestMetadata cloneRequestMetadata) {
        return (PointProvider) super.deepClone(cloneRequestMetadata);
    }

    @Override
    public Class<?> getProvidedType() {
        return Point.class;
    }

}
