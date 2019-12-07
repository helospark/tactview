package com.helospark.tactview.core.timeline.effect.threshold;

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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdImplementation;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class AdaptiveThresholdEffect extends StatelessVideoEffect {
    private IntegerProvider addedConstantProvider;
    private IntegerProvider blockSizeProvider;

    private OpenCVThresholdImplementation thresholdImplementation;

    public AdaptiveThresholdEffect(TimelineInterval interval, OpenCVThresholdImplementation thresholdImplementation) {
        super(interval);
        this.thresholdImplementation = thresholdImplementation;
    }

    public AdaptiveThresholdEffect(AdaptiveThresholdEffect adaptiveThresholdEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(adaptiveThresholdEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(adaptiveThresholdEffect, this);
    }

    public AdaptiveThresholdEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVThresholdImplementation openCVThresholdImplementation) {
        super(node, loadMetadata);
        this.thresholdImplementation = openCVThresholdImplementation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());

        OpenCVThresholdRequest nativeRequest = new OpenCVThresholdRequest();
        nativeRequest.addedConstant = addedConstantProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        nativeRequest.blockSize = blockSizeProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        nativeRequest.height = request.getCurrentFrame().getHeight();
        nativeRequest.width = request.getCurrentFrame().getWidth();
        nativeRequest.input = request.getCurrentFrame().getBuffer();
        nativeRequest.output = result.getBuffer();

        thresholdImplementation.threshold(nativeRequest);

        return result;
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor addedConstantDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(addedConstantProvider)
                .withName("Added constant")
                .build();
        ValueProviderDescriptor blockSizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blockSizeProvider)
                .withName("Block size")
                .build();
        return List.of(blockSizeDescriptor, addedConstantDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new AdaptiveThresholdEffect(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        addedConstantProvider = new IntegerProvider(-50, 50, new MultiKeyframeBasedDoubleInterpolator(0.0));
        blockSizeProvider = new IntegerProvider(1, 10, new MultiKeyframeBasedDoubleInterpolator(2.0));
    }

}
