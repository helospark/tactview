package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.util.DesSerFactory;

public class RectangleProvider extends CompositeKeyframeableEffect<Rectangle> {
    List<PointProvider> pointProviders;
    SizeFunction sizeFunction;

    public RectangleProvider(List<PointProvider> pointProviders, SizeFunction sizeFunction) {
        super((List<KeyframeableEffect>) (Object) pointProviders);
        this.pointProviders = pointProviders;
        this.sizeFunction = sizeFunction;
    }

    @Override
    public Rectangle getValueAt(TimelinePosition position) {
        List<Point> points = pointProviders.stream()
                .map(provider -> provider.getValueAt(position))
                .collect(Collectors.toList());
        return new Rectangle(points);
    }

    @Override
    public Rectangle getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            Rectangle expressionResult = evaluationContext.evaluateExpression(expression, position, Rectangle.class);
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
    public void keyframeAdded(TimelinePosition globalTimelinePosition, Rectangle value) {
        for (int i = 0; i < 4; ++i) {
            pointProviders.get(i).keyframeAdded(globalTimelinePosition, value.points.get(i));
        }
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect<?>> getChildren() {
        return (List<KeyframeableEffect<?>>) (Object) pointProviders;
    }

    @Override
    public KeyframeableEffect<Rectangle> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        List<PointProvider> clonedList = pointProviders.stream()
                .map(a -> a.deepClone(cloneRequestMetadata))
                .collect(Collectors.toList());
        return new RectangleProvider(clonedList, sizeFunction);
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<Rectangle>>> generateSerializableContent() {
        return RectangleProviderFactory.class;
    }

    public static RectangleProvider createDefaultFullImageWithNormalizedPosition() {
        PointProvider a = PointProvider.ofNormalizedImagePosition(0.0, 0.0);
        PointProvider b = PointProvider.ofNormalizedImagePosition(1.0, 0.0);
        PointProvider c = PointProvider.ofNormalizedImagePosition(1.0, 1.0);
        PointProvider d = PointProvider.ofNormalizedImagePosition(0.0, 1.0);
        return new RectangleProvider(List.of(a, b, c, d), SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE);
    }

    public static RectangleProvider createDefaultFullImageWithNormalizedCenterAndSize(Point center, double width, double height) {
        PointProvider a = PointProvider.ofNormalizedImagePosition(center.x - width, center.y - height);
        PointProvider b = PointProvider.ofNormalizedImagePosition(center.x + width, center.y - height);
        PointProvider c = PointProvider.ofNormalizedImagePosition(center.x + width, center.y + height);
        PointProvider d = PointProvider.ofNormalizedImagePosition(center.x - width, center.y + height);
        return new RectangleProvider(List.of(a, b, c, d), SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return sizeFunction;
    }

    @Override
    public Class<?> getProvidedType() {
        return Rectangle.class;
    }
}
