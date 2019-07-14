package com.helospark.tactview.core.timeline.effect.colorchannelchange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.IntPoint;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class FloodFillEffect extends StatelessVideoEffect {
    private PointProvider startPointProvider;
    private DoubleProvider maximumColorDifferenceProvider;
    private ColorProvider newColorProvider;

    public FloodFillEffect(TimelineInterval interval) {
        super(interval);
    }

    public FloodFillEffect(FloodFillEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public FloodFillEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        List<IntPoint> pointQueue = new ArrayList<>();
        Set<IntPoint> alreadyVisitedPoints = new HashSet<>();
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        ClipImage resultImage = ClipImage.sameSizeAs(currentFrame);
        resultImage.copyFrom(currentFrame);

        IntPoint[] offsets = new IntPoint[]{
                new IntPoint(-1, 0),
                new IntPoint(1, 0),
                new IntPoint(0, 1),
                new IntPoint(0, -1)
        };

        Color newColor = newColorProvider.getValueAt(request.getEffectPosition()).multiplyComponents(255);

        IntPoint center = IntPoint.fromPoint(startPointProvider.getValueAt(request.getEffectPosition()).multiply(currentFrame.getWidth(), currentFrame.getHeight()));
        Color color = new Color(0, 0, 0);
        color.red = currentFrame.getRed(center.x, center.y);
        color.green = currentFrame.getGreen(center.x, center.y);
        color.blue = currentFrame.getBlue(center.x, center.y);

        double maxColorDifference = maximumColorDifferenceProvider.getValueAt(request.getEffectPosition());

        pointQueue.add(center);

        while (!pointQueue.isEmpty()) {
            IntPoint element = pointQueue.remove(pointQueue.size() - 1);
            alreadyVisitedPoints.add(element);
            setColor(resultImage, element, newColor);

            for (int i = 0; i < offsets.length; ++i) {
                IntPoint offsetedElement = element.offset(offsets[i]);
                if (inFrame(currentFrame, offsetedElement)
                        && !alreadyVisitedPoints.contains(offsetedElement)
                        && isColorMatch(currentFrame, offsetedElement, color, maxColorDifference)) {
                    pointQueue.add(offsetedElement);
                }
            }

        }

        return resultImage;

    }

    private void setColor(ClipImage resultImage, IntPoint offsetedElement, Color newColor) {
        resultImage.setRed((int) newColor.red, offsetedElement.x, offsetedElement.y);
        resultImage.setGreen((int) newColor.green, offsetedElement.x, offsetedElement.y);
        resultImage.setBlue((int) newColor.blue, offsetedElement.x, offsetedElement.y);
        resultImage.setAlpha(255, offsetedElement.x, offsetedElement.y);
    }

    private boolean isColorMatch(ReadOnlyClipImage image, IntPoint offsetedElement, Color color, double maxColorDifference) {
        int redDiff = (int) Math.abs(image.getRed(offsetedElement.x, offsetedElement.y) - color.red);
        int greenDiff = (int) Math.abs(image.getGreen(offsetedElement.x, offsetedElement.y) - color.green);
        int blueDiff = (int) Math.abs(image.getBlue(offsetedElement.x, offsetedElement.y) - color.blue);

        double allDiff = (redDiff + greenDiff + blueDiff) / 255.0;

        return allDiff < maxColorDifference;
    }

    private boolean inFrame(ReadOnlyClipImage currentFrame, IntPoint offsetedElement) {
        return offsetedElement.x >= 0 && offsetedElement.x < currentFrame.getWidth() && offsetedElement.y >= 0 && offsetedElement.y < currentFrame.getHeight();
    }

    @Override
    public void initializeValueProvider() {
        startPointProvider = PointProvider.ofNormalizedImagePosition(0.5, 0.5);
        newColorProvider = ColorProvider.fromDefaultValue(0.0, 1.0, 0.0);
        maximumColorDifferenceProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor centerDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startPointProvider)
                .withName("fill center")
                .build();
        ValueProviderDescriptor newColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(newColorProvider)
                .withName("new color")
                .build();
        ValueProviderDescriptor maxDiffDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maximumColorDifferenceProvider)
                .withName("max diff")
                .build();

        return List.of(centerDescriptor, newColorDescriptor, maxDiffDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new FloodFillEffect(this, cloneRequestMetadata);
    }

}
