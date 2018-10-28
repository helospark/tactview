package com.helospark.tactview.core.timeline.effect.threshold;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdImplementation;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdRequest;

public class AdaptiveThresholdEffect extends StatelessVideoEffect {
    private IntegerProvider addedConstantProvider;
    private IntegerProvider blockSizeProvider;

    private OpenCVThresholdImplementation thresholdImplementation;

    public AdaptiveThresholdEffect(TimelineInterval interval, OpenCVThresholdImplementation thresholdImplementation) {
        super(interval);
        this.thresholdImplementation = thresholdImplementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult result = ClipFrameResult.sameSizeAs(request.getCurrentFrame());

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
    public List<ValueProviderDescriptor> getValueProviders() {
        addedConstantProvider = new IntegerProvider(-50, 50, new DoubleInterpolator(0.0));
        blockSizeProvider = new IntegerProvider(1, 10, new DoubleInterpolator(2.0));

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

}
