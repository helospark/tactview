package com.helospark.tactview.core.timeline.effect.mozaic;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class MozaicEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider xProvider;
    private DoubleProvider yProvider;

    public MozaicEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MozaicEffect(MozaicEffect mozaicEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(mozaicEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(mozaicEffect, this);
    }

    public MozaicEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        int width = result.getWidth();
        int height = result.getHeight();

        double xMultiplier = xProvider.getValueAt(request.getEffectPosition());
        double yMultiplier = yProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            double normalizedX = (((double) x / width) * xMultiplier);
            double normalizedY = (((double) y / height) * yMultiplier);

            normalizedX = (normalizedX - (int) normalizedX) * width;
            normalizedY = (normalizedY - (int) normalizedY) * height;

            for (int i = 0; i < 4; ++i) {
                int color = currentFrame.getColorComponentWithOffsetUsingInterpolation(normalizedX, normalizedY, i);
                result.setColorComponentByOffset(color, x, y, i);
            }
        });
        return result;
    }

    @Override
    public void initializeValueProvider() {
        xProvider = new DoubleProvider(1.0, 30.0, new MultiKeyframeBasedDoubleInterpolator(2.0));
        yProvider = new DoubleProvider(1.0, 30.0, new MultiKeyframeBasedDoubleInterpolator(2.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor xProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xProvider)
                .withName("x")
                .build();
        ValueProviderDescriptor yProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yProvider)
                .withName("y")
                .build();

        return List.of(xProviderDescriptor, yProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new MozaicEffect(this, cloneRequestMetadata);
    }

}
