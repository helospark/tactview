package com.helospark.tactview.core.timeline.effect.crop;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.Arrays;
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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class CropEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider strengthProvider;
    private PointProvider topLeftPointProvider;
    private PointProvider bottomRightPointProvider;

    public CropEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public CropEffect(CropEffect cropEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(cropEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cropEffect, this, cloneRequestMetadata);
    }

    public CropEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Point topLeft = topLeftPointProvider.getValueAt(request.getEffectPosition());
        Point bottomRight = bottomRightPointProvider.getValueAt(request.getEffectPosition());
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            double x = (double) pixelRequest.x / request.getCurrentFrame().getWidth();
            double y = (double) pixelRequest.y / request.getCurrentFrame().getHeight();
            if (inCroppedRegion(topLeft, bottomRight, x, y)) {
                for (int i = 0; i < 4; ++i) {
                    pixelRequest.output[i] = pixelRequest.input[i];
                }
            } else {
                for (int i = 0; i < 3; ++i) {
                    pixelRequest.output[i] = pixelRequest.input[i];
                }
                pixelRequest.output[3] = 0;
            }
        });
    }

    private boolean inCroppedRegion(Point topLeft, Point bottomRight, double x, double y) {
        if (x < topLeft.x) {
            return false;
        }
        if (x > bottomRight.x) {
            return false;
        }
        if (y < topLeft.y) {
            return false;
        }
        if (y > bottomRight.y) {
            return false;
        }
        return true;
    }

    @Override
    protected void initializeValueProviderInternal() {
        strengthProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));

        topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.3), doubleProviderWithDefaultValue(0.3));
        bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.7), doubleProviderWithDefaultValue(0.7));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        LineProvider lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("area")
                .build();

        ValueProviderDescriptor strengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(strengthProvider)
                .withName("strength")
                .build();

        return Arrays.asList(strengthProviderDescriptor, sizeDescriptor);
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new CropEffect(this, cloneRequestMetadata);
    }

}
