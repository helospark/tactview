package com.helospark.tactview.core.timeline.effect.transition.floatout;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class FloatOutTransitionEffect extends AbstractVideoTransitionEffect {
    private IndependentPixelOperation independentPixelOperation;
    private ValueListProvider<ValueListElement> directionProvider;

    public FloatOutTransitionEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    protected ClipFrameResult applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        double progress = request.getProgress();
        ClipFrameResult firstFrame = request.getFirstFrame();
        ClipFrameResult secondFrame = request.getSecondFrame();

        ClipFrameResult result = ClipFrameResult.sameSizeAs(request.getFirstFrame());

        int xPosition = 0;
        int yPosition = 0;

        ValueListElement direction = directionProvider.getValueAt(request.getEffectPosition());

        if (direction.getId().equals("right")) {
            xPosition = (int) (progress * firstFrame.getWidth() * -1);
        } else if (direction.getId().equals("left")) {
            xPosition = (int) (progress * firstFrame.getWidth());
        } else if (direction.getId().equals("up")) {
            yPosition = (int) (progress * firstFrame.getHeight());
        } else {
            yPosition = (int) (progress * firstFrame.getHeight() * -1);
        }

        int xOffset = xPosition;
        int yOffset = yPosition;

        independentPixelOperation.executePixelTransformation(firstFrame.getWidth(), firstFrame.getHeight(), (x, y) -> {
            int fromX = x + xOffset;
            int fromY = y + yOffset;

            if (fromX >= 0 && fromY >= 0 && fromX < firstFrame.getWidth() && fromY < firstFrame.getHeight()) {
                copyColor(firstFrame, result, fromX, fromY, x, y);
            } else {
                copyColor(secondFrame, result, x, y, x, y);
            }
        });
        return result;
    }

    private void copyColor(ClipFrameResult firstFrame, ClipFrameResult result, int fromX, int fromY, int toX, int toY) {
        for (int imageChannel = 0; imageChannel < 4; ++imageChannel) {
            int color = firstFrame.getColorComponentWithOffset(fromX, fromY, imageChannel);
            result.setColorComponentByOffset(color, toX, toY, imageChannel);
        }
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> valueProviders = super.getValueProviders();

        directionProvider = new ValueListProvider<>(createDirections(), new StringInterpolator("right"));

        ValueProviderDescriptor directionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(directionProvider)
                .withName("direction")
                .build();

        valueProviders.add(directionDescriptor);

        return valueProviders;
    }

    private List<ValueListElement> createDirections() {
        return List.of(new ValueListElement("left", "left"),
                new ValueListElement("right", "right"),
                new ValueListElement("up", "up"),
                new ValueListElement("down", "down"));
    }

}
