package com.helospark.tactview.core.timeline.effect.blur;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVGaussianBlurRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;

public class BlurEffect extends StatelessVideoEffect {
    private OpenCVBasedGaussianBlur openCVBasedBlur;
    private IntegerProvider kernelHeightProvider;
    private IntegerProvider kernelWidthProvider;

    public BlurEffect(TimelineInterval interval, OpenCVBasedGaussianBlur openCVBasedBlur) {
        super(interval);
        this.openCVBasedBlur = openCVBasedBlur;
    }

    @Override
    public void fillFrame(ClipFrameResult result, StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        OpenCVGaussianBlurRequest nativeRequest = new OpenCVGaussianBlurRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.kernelWidth = kernelWidthProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        nativeRequest.kernelHeight = kernelHeightProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        openCVBasedBlur.applyGaussianBlur(nativeRequest);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        kernelWidthProvider = new IntegerProvider(0, 30, new DoubleInterpolator(new TreeMap<>(
                Map.of(TimelinePosition.ofZero(), 3.0,
                        new TimelinePosition(5), 15.0))));
        kernelHeightProvider = new IntegerProvider(0, 20, new DoubleInterpolator(TimelinePosition.ofZero(), 3.0));

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("kernelWidth")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("kernelWidth")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor);
    }

}
