package com.helospark.tactview.core.timeline.effect.denoise;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.denoise.opencv.OpenCVBasedDenoiseEffect;
import com.helospark.tactview.core.timeline.effect.denoise.opencv.OpenCVDenoiseRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;

public class DenoiseEffect extends StatelessVideoEffect {
    private IntegerProvider templateWindowSizeProvider;
    private IntegerProvider searchWindowSizeProvider;
    private DoubleProvider strengthProvider;

    private OpenCVBasedDenoiseEffect openCVBasedDenoiseEffect;

    public DenoiseEffect(TimelineInterval interval, OpenCVBasedDenoiseEffect openCVBasedDenoiseEffect) {
        super(interval);
        this.openCVBasedDenoiseEffect = openCVBasedDenoiseEffect;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult result = ClipFrameResult.sameSizeAs(request.getCurrentFrame());
        OpenCVDenoiseRequest denoiseRequest = new OpenCVDenoiseRequest();

        denoiseRequest.width = request.getCurrentFrame().getWidth();
        denoiseRequest.height = request.getCurrentFrame().getHeight();
        denoiseRequest.input = request.getCurrentFrame().getBuffer();
        denoiseRequest.output = result.getBuffer();
        denoiseRequest.searchWindowSize = searchWindowSizeProvider.getValueAt(request.getEffectPosition()) * 2 + 1;
        denoiseRequest.strength = strengthProvider.getValueAt(request.getEffectPosition());
        denoiseRequest.templateWindowSize = templateWindowSizeProvider.getValueAt(request.getEffectPosition()) * 2 + 1;

        openCVBasedDenoiseEffect.denoise(denoiseRequest);

        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        templateWindowSizeProvider = new IntegerProvider(0, 30, new DoubleInterpolator(3.0));
        searchWindowSizeProvider = new IntegerProvider(0, 50, new DoubleInterpolator(10.0));
        strengthProvider = new DoubleProvider(0, 50, new DoubleInterpolator(10.0));

        ValueProviderDescriptor templateWindowSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(templateWindowSizeProvider)
                .withName("Template window")
                .build();
        ValueProviderDescriptor searchWindowSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(searchWindowSizeProvider)
                .withName("Search window")
                .build();
        ValueProviderDescriptor strengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(strengthProvider)
                .withName("Strength")
                .build();

        return List.of(templateWindowSizeProviderDescriptor, searchWindowSizeProviderDescriptor, strengthProviderDescriptor);
    }

}
