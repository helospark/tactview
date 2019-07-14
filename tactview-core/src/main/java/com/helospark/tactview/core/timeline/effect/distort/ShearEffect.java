package com.helospark.tactview.core.timeline.effect.distort;

import java.util.List;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class ShearEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider shearXProvider;
    private DoubleProvider shearYProvider;

    private PointProvider offsetProvider;

    public ShearEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public ShearEffect(ShearEffect lensDistortEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lensDistortEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lensDistortEffect, this);
    }

    public ShearEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage frame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(frame);

        int width = frame.getWidth();
        int height = frame.getHeight();

        double shearX = shearXProvider.getValueAt(request.getEffectPosition()) * width;
        double shearY = shearYProvider.getValueAt(request.getEffectPosition()) * height;
        Point offset = offsetProvider.getValueAt(request.getEffectPosition()).multiply(width, height);

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            double mappedX = x + shearX * y - offset.x;
            double mappedY = y + shearY * x - offset.y;

            if (frame.inBounds((int) mappedX, (int) mappedY)) {
                for (int i = 0; i < 4; ++i) {
                    int colorValue = frame.getColorComponentWithOffsetUsingInterpolation(mappedX, mappedY, i);
                    result.setColorComponentByOffset(colorValue, x, y, i);
                }
            }
        });

        return result;
    }

    @Override
    public void initializeValueProvider() {
        shearXProvider = new DoubleProvider(-0.01, 0.01, new MultiKeyframeBasedDoubleInterpolator(0.001));
        shearYProvider = new DoubleProvider(-0.01, 0.01, new MultiKeyframeBasedDoubleInterpolator(0.0));
        offsetProvider = PointProvider.ofNormalizedImagePosition(0.0, 0.0);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor shearXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shearXProvider)
                .withName("Shear X")
                .build();
        ValueProviderDescriptor shearYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shearYProvider)
                .withName("Shear Y")
                .build();
        ValueProviderDescriptor offsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offsetProvider)
                .withName("offset")
                .build();

        return List.of(shearXProviderDescriptor, shearYProviderDescriptor, offsetProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ShearEffect(this, cloneRequestMetadata);
    }

}
