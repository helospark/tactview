package com.helospark.tactview.core.timeline.effect.pencil;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchImplementation;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchRequest;

public class PencilSketchEffect extends StatelessVideoEffect {
    private OpenCVPencilSketchImplementation implementation;
    private DoubleProvider sigmaSProvider;
    private DoubleProvider sigmaRProvider;
    private DoubleProvider shadeFactorProvider;
    private BooleanProvider colorProvider;

    public PencilSketchEffect(TimelineInterval interval, OpenCVPencilSketchImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        ClipFrameResult result = ClipFrameResult.sameSizeAs(currentFrame);

        OpenCVPencilSketchRequest nativeRequest = new OpenCVPencilSketchRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.sigmaS = sigmaSProvider.getValueAt(request.getEffectPosition());
        nativeRequest.sigmaR = sigmaRProvider.getValueAt(request.getEffectPosition());
        nativeRequest.shadeFactor = shadeFactorProvider.getValueAt(request.getEffectPosition());
        nativeRequest.color = colorProvider.getValueAt(request.getEffectPosition());

        implementation.pencilSketch(nativeRequest);

        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        sigmaSProvider = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(60.0));
        sigmaRProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.07));
        shadeFactorProvider = new DoubleProvider(0, 0.1, new MultiKeyframeBasedDoubleInterpolator(0.02));
        colorProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));

        ValueProviderDescriptor sigmaSProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sigmaSProvider)
                .withName("Sigma S")
                .build();

        ValueProviderDescriptor sigmaRProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sigmaRProvider)
                .withName("Sigma R")
                .build();

        ValueProviderDescriptor shadeFactorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadeFactorProvider)
                .withName("Shade factor")
                .build();

        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Color")
                .build();

        return List.of(sigmaSProviderDescriptor, sigmaRProviderDescriptor, shadeFactorProviderDescriptor, colorProviderDescriptor);
    }

}
