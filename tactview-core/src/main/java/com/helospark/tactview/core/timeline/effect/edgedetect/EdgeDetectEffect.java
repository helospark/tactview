package com.helospark.tactview.core.timeline.effect.edgedetect;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.edgedetect.opencv.OpenCVEdgeDetectImplementation;
import com.helospark.tactview.core.timeline.effect.edgedetect.opencv.OpenCVEdgeDetectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class EdgeDetectEffect extends StatelessVideoEffect {
    private OpenCVEdgeDetectImplementation implementation;

    private DoubleProvider lowThresholdProvider;
    private DoubleProvider highThresholdMultiplier;
    private IntegerProvider apertureSizeProvider;

    public EdgeDetectEffect(TimelineInterval interval, OpenCVEdgeDetectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public EdgeDetectEffect(EdgeDetectEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public EdgeDetectEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVEdgeDetectImplementation openCVEdgeDetectImplementation) {
        super(node, loadMetadata);
        this.implementation = openCVEdgeDetectImplementation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        OpenCVEdgeDetectRequest nativeRequest = new OpenCVEdgeDetectRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.apertureSize = apertureSizeProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        nativeRequest.lowThreshold = lowThresholdProvider.getValueAt(request.getEffectPosition());
        nativeRequest.highThresholdMultiplier = highThresholdMultiplier.getValueAt(request.getEffectPosition());

        implementation.edgeDetect(nativeRequest);

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        lowThresholdProvider = new DoubleProvider(1.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(30.0));
        highThresholdMultiplier = new DoubleProvider(1.0, 5.0, new MultiKeyframeBasedDoubleInterpolator(3.0));
        apertureSizeProvider = new IntegerProvider(1, 3, new MultiKeyframeBasedDoubleInterpolator(3.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor lowThresholdProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lowThresholdProvider)
                .withName("Low threshold")
                .build();

        ValueProviderDescriptor highThresholdMultiplierDescritor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(highThresholdMultiplier)
                .withName("High threshold multiplier")
                .build();

        ValueProviderDescriptor apertureSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(apertureSizeProvider)
                .withName("Aperture")
                .build();

        return List.of(lowThresholdProviderDescriptor, highThresholdMultiplierDescritor, apertureSizeProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new EdgeDetectEffect(this, cloneRequestMetadata);
    }

}
