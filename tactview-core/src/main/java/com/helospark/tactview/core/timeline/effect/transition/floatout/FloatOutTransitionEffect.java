package com.helospark.tactview.core.timeline.effect.transition.floatout;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class FloatOutTransitionEffect extends AbstractVideoTransitionEffect {
    private IndependentPixelOperation independentPixelOperation;
    private ValueListProvider<ValueListElement> directionProvider;
    private ValueListProvider<ValueListElement> floatOrWipeProvider;

    public FloatOutTransitionEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public FloatOutTransitionEffect(FloatOutTransitionEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public FloatOutTransitionEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        double progress = request.getProgress();
        ReadOnlyClipImage firstFrame = request.getFirstFrame();
        ReadOnlyClipImage secondFrame = request.getSecondFrame();

        ClipImage result = ClipImage.sameSizeAs(request.getFirstFrame());

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

        String type = floatOrWipeProvider.getValueAt(request.getEffectPosition()).getId();

        int xOffset = xPosition;
        int yOffset = yPosition;

        independentPixelOperation.executePixelTransformation(firstFrame.getWidth(), firstFrame.getHeight(), (x, y) -> {

            if (type.equals("float")) {
                int fromX = x + xOffset;
                int fromY = y + yOffset;
                if (fromX >= 0 && fromY >= 0 && fromX < firstFrame.getWidth() && fromY < firstFrame.getHeight()) {
                    copyColor(firstFrame, result, fromX, fromY, x, y);
                } else {
                    copyColor(secondFrame, result, x, y, x, y);
                }
            } else if (type.equals("wipe")) {
                ReadOnlyClipImage imageToUse = secondFrame;
                if (direction.getId().equals("left")) {
                    if (x < firstFrame.getWidth() - xOffset - 1) {
                        imageToUse = firstFrame;
                    }
                } else if (direction.getId().equals("up")) {
                    if (y < firstFrame.getHeight() - yOffset - 1) {
                        imageToUse = firstFrame;
                    }
                } else if (direction.getId().equals("right")) {
                    if (x > xOffset * -1) {
                        imageToUse = firstFrame;
                    }
                } else if (direction.getId().equals("down")) {
                    if (y > yOffset * -1) {
                        imageToUse = firstFrame;
                    }
                }

                copyColor(imageToUse, result, x, y, x, y);
            }
        });
        return result;
    }

    private void copyColor(ReadOnlyClipImage firstFrame, ClipImage result, int fromX, int fromY, int toX, int toY) {
        for (int imageChannel = 0; imageChannel < 4; ++imageChannel) {
            int color = firstFrame.getColorComponentWithOffset(fromX, fromY, imageChannel);
            result.setColorComponentByOffset(color, toX, toY, imageChannel);
        }
    }

    @Override
    public void initializeValueProvider() {
        super.initializeValueProvider();
        directionProvider = new ValueListProvider<>(createDirections(), new StringInterpolator("right"));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> valueProviders = super.getValueProviders();

        ValueProviderDescriptor directionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(directionProvider)
                .withName("direction")
                .build();

        floatOrWipeProvider = new ValueListProvider<>(createFloatOrWipe(), new StringInterpolator("float"));

        ValueProviderDescriptor floatOrWipeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(floatOrWipeProvider)
                .withName("type")
                .build();

        valueProviders.add(directionDescriptor);
        valueProviders.add(floatOrWipeProviderDescriptor);

        return valueProviders;
    }

    private List<ValueListElement> createFloatOrWipe() {
        return List.of(new ValueListElement("float", "float"),
                new ValueListElement("wipe", "wipe"));
    }

    private List<ValueListElement> createDirections() {
        return List.of(new ValueListElement("left", "left"),
                new ValueListElement("right", "right"),
                new ValueListElement("up", "up"),
                new ValueListElement("down", "down"));
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new FloatOutTransitionEffect(this);
    }

}
