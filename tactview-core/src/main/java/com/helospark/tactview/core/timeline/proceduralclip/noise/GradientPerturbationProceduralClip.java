package com.helospark.tactview.core.timeline.proceduralclip.noise;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationNoiseService;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationRequestParameter;

public class GradientPerturbationProceduralClip extends ProceduralVisualClip {
    private PerturbationNoiseService perturbationNoiseService;

    private DoubleProvider zPositionProvider;
    private DoubleProvider colorScaleProvider;
    private DoubleProvider frequencyProvider;
    private DoubleProvider gradientPerturbationAmplifier;
    private BooleanProvider isFractalProvider;
    private PointProvider startPointProvider;
    private IntegerProvider seedProvider;

    public GradientPerturbationProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, PerturbationNoiseService perturbationNoiseService) {
        super(visualMediaMetadata, interval);
        this.perturbationNoiseService = perturbationNoiseService;
    }

    public GradientPerturbationProceduralClip(GradientPerturbationProceduralClip noiseProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(noiseProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(noiseProceduralEffect, this);
    }

    public GradientPerturbationProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, PerturbationNoiseService perturbationNoiseService) {
        super(metadata, node, loadMetadata);
        this.perturbationNoiseService = perturbationNoiseService;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        PerturbationRequestParameter perturbationRequest = PerturbationRequestParameter.builder()
                .withColorScale(colorScaleProvider.getValueAt(relativePosition))
                .withFrequency((frequencyProvider.getValueAt(relativePosition).floatValue()))
                .withGradientPerturb(gradientPerturbationAmplifier.getValueAt(relativePosition).floatValue())
                .withHeight(request.getExpectedHeight())
                .withIsFractal(isFractalProvider.getValueAt(relativePosition))
                .withSeed(seedProvider.getValueAt(relativePosition))
                .withStartPoint(startPointProvider.getValueAt(relativePosition))
                .withWidth(request.getExpectedWidth())
                .withZPos(zPositionProvider.getValueAt(relativePosition).floatValue())
                .build();
        return perturbationNoiseService.createPerturbation(perturbationRequest);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        zPositionProvider = new DoubleProvider(-300, 300, new MultiKeyframeBasedDoubleInterpolator(0.0));
        colorScaleProvider = new DoubleProvider(1, 255, new MultiKeyframeBasedDoubleInterpolator(50.0));
        frequencyProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.13));
        gradientPerturbationAmplifier = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(10.0));
        isFractalProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        startPointProvider = PointProvider.ofNormalizedImagePosition(0, 0);
        seedProvider = new IntegerProvider(0, 1000, new MultiKeyframeBasedDoubleInterpolator((double) (new Random().nextInt(1000))));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> descriptors = super.getDescriptorsInternal();

        ValueProviderDescriptor frequencyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(frequencyProvider)
                .withName("frequency")
                .build();
        ValueProviderDescriptor perturbationDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(gradientPerturbationAmplifier)
                .withName("perturbation")
                .build();
        ValueProviderDescriptor isFractalDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(isFractalProvider)
                .withName("Is fractal")
                .build();
        ValueProviderDescriptor zPositionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zPositionProvider)
                .withName("Z position")
                .build();
        ValueProviderDescriptor colorScaleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorScaleProvider)
                .withName("Color scale")
                .build();
        ValueProviderDescriptor startPointDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startPointProvider)
                .withName("Start point")
                .build();
        ValueProviderDescriptor seedDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(seedProvider)
                .withName("Seed")
                .build();

        descriptors.add(frequencyDescriptor);
        descriptors.add(perturbationDescriptor);
        descriptors.add(isFractalDescriptor);
        descriptors.add(zPositionDescriptor);
        descriptors.add(colorScaleDescriptor);
        descriptors.add(startPointDescriptor);
        descriptors.add(seedDescriptor);

        return descriptors;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new GradientPerturbationProceduralClip(this, cloneRequestMetadata);
    }

}
