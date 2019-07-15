package com.helospark.tactview.core.timeline.effect.blur;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
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

public class BlurEffect extends StatelessVideoEffect {
    private BlurService blurService;

    private IntegerProvider kernelHeightProvider;
    private IntegerProvider kernelWidthProvider;

    private PointProvider topLeftPointProvider;
    private PointProvider bottomRightPointProvider;

    public BlurEffect(TimelineInterval interval, BlurService blurService) {
        super(interval);
        this.blurService = blurService;
    }

    public BlurEffect(BlurEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    public BlurEffect(JsonNode node, LoadMetadata loadMetadata, BlurService blurService2) {
        super(node, loadMetadata);
        this.blurService = blurService2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {

        BlurRequest blurRequest = BlurRequest.builder()
                .withImage(request.getCurrentFrame())
                .withKernelWidth((int) (kernelWidthProvider.getValueAt(request.getEffectPosition()) * request.getScale()))
                .withKernelHeight((int) (kernelHeightProvider.getValueAt(request.getEffectPosition()) * request.getScale()))
                .withRegion(Optional.ofNullable(createBlurRegion(request)))
                .build();

        return blurService.createBlurredImage(blurRequest);
    }

    private Region createBlurRegion(StatelessEffectRequest request) {
        Point topLeft = topLeftPointProvider.getValueAt(request.getEffectPosition());
        Point bottomRight = bottomRightPointProvider.getValueAt(request.getEffectPosition());
        int x = (int) (topLeft.x * request.getCurrentFrame().getWidth());
        int y = (int) (topLeft.y * request.getCurrentFrame().getHeight());

        int rightX = (int) (bottomRight.x * request.getCurrentFrame().getWidth());
        int rightY = (int) (bottomRight.y * request.getCurrentFrame().getHeight());

        int width = rightX - x;
        int height = rightY - y;

        // TODO: better clamping
        if (width < 10) {
            width = 10;
        }
        if (height < 10) {
            height = 10;
        }

        return Region.builder()
                .withx(x)
                .withy(y)
                .withWidth(width)
                .withHeight(height)
                .build();
    }

    @Override
    public void initializeValueProvider() {
        kernelWidthProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));
        kernelHeightProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));
        kernelHeightProvider.setScaleDependent();
        kernelHeightProvider.setScaleDependent();

        topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.0), doubleProviderWithDefaultValue(0.0));
        bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(1.0), doubleProviderWithDefaultValue(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        LineProvider lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("area")
                .build();

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("kernel width")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("kernel height")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor, sizeDescriptor);
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new BlurEffect(this, cloneRequestMetadata);
    }

}
