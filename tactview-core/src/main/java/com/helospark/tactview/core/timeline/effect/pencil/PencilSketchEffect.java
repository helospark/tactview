package com.helospark.tactview.core.timeline.effect.pencil;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchImplementation;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

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

    public PencilSketchEffect(PencilSketchEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public PencilSketchEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVPencilSketchImplementation implementation2) {
        super(node, loadMetadata);
        this.implementation = implementation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

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
    protected void initializeValueProviderInternal() {
        sigmaSProvider = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(60.0));
        sigmaRProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.07));
        shadeFactorProvider = new DoubleProvider(0, 0.1, new MultiKeyframeBasedDoubleInterpolator(0.02));
        colorProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

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

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PencilSketchEffect(this, cloneRequestMetadata);
    }
}
