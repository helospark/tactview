package com.helospark.tactview.core.timeline.effect.distort;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.displacementmap.service.ApplyDisplacementMapRequest;
import com.helospark.tactview.core.timeline.effect.displacementmap.service.DisplacementMapService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationNoiseService;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationRequestParameter;

public class PerturbationDistortEffect extends StatelessVideoEffect {
    private PerturbationNoiseService perturbationNoiseService;
    private DisplacementMapService displacementService;

    private DoubleProvider zPositionProvider;
    private DoubleProvider colorScaleProvider;
    private DoubleProvider frequencyProvider;
    private DoubleProvider gradientPerturbationAmplifier;
    private BooleanProvider isFractalProvider;
    private PointProvider startPointProvider;
    private IntegerProvider seedProvider;

    private DoubleProvider verticalMultiplierProvider;
    private DoubleProvider horizontalMultiplierProvider;

    public PerturbationDistortEffect(TimelineInterval interval, PerturbationNoiseService perturbationNoiseService, DisplacementMapService displacementService) {
        super(interval);
        this.perturbationNoiseService = perturbationNoiseService;
        this.displacementService = displacementService;
    }

    public PerturbationDistortEffect(PerturbationDistortEffect lensDistortEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lensDistortEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lensDistortEffect, this);
    }

    public PerturbationDistortEffect(JsonNode node, LoadMetadata loadMetadata, PerturbationNoiseService perturbationNoiseService, DisplacementMapService displacementService) {
        super(node, loadMetadata);
        this.perturbationNoiseService = perturbationNoiseService;
        this.displacementService = displacementService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        TimelinePosition relativePosition = request.getEffectPosition();
        int width = request.getCurrentFrame().getWidth();
        int height = request.getCurrentFrame().getHeight();

        PerturbationRequestParameter perturbationRequest = PerturbationRequestParameter.builder()
                .withColorScale(colorScaleProvider.getValueAt(relativePosition))
                .withFrequency((frequencyProvider.getValueAt(relativePosition).floatValue()))
                .withGradientPerturb(gradientPerturbationAmplifier.getValueAt(relativePosition).floatValue())
                .withHeight(height)
                .withIsFractal(isFractalProvider.getValueAt(relativePosition))
                .withSeed(seedProvider.getValueAt(relativePosition))
                .withStartPoint(startPointProvider.getValueAt(relativePosition))
                .withWidth(width)
                .withZPos(zPositionProvider.getValueAt(relativePosition).floatValue())
                .build();
        ClipImage perturbation = perturbationNoiseService.createPerturbation(perturbationRequest);

        ApplyDisplacementMapRequest displacementMapRequest = ApplyDisplacementMapRequest.builder()
                .withCurrentFrame(request.getCurrentFrame())
                .withDisplacementMap(perturbation)
                .withHorizontalMultiplier(horizontalMultiplierProvider.getValueAt(relativePosition) * request.getScale())
                .withVerticalMultiplier(verticalMultiplierProvider.getValueAt(relativePosition) * request.getScale())
                .build();

        ClipImage result = displacementService.applyDisplacementMap(displacementMapRequest);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(perturbation.getBuffer());

        return result;
    }

    @Override
    public void initializeValueProvider() {
        zPositionProvider = new DoubleProvider(-300, 300, new MultiKeyframeBasedDoubleInterpolator(0.0));
        colorScaleProvider = new DoubleProvider(1, 255, new MultiKeyframeBasedDoubleInterpolator(50.0));
        frequencyProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.13));
        gradientPerturbationAmplifier = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(10.0));
        isFractalProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        startPointProvider = PointProvider.ofNormalizedImagePosition(0, 0);
        seedProvider = new IntegerProvider(0, 1000, new MultiKeyframeBasedDoubleInterpolator((double) (new Random().nextInt(1000))));

        verticalMultiplierProvider = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(60.0));
        horizontalMultiplierProvider = new DoubleProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(60.0));

    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor frequencyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(frequencyProvider)
                .withName("frequency")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor perturbationDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(gradientPerturbationAmplifier)
                .withName("perturbation")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor isFractalDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(isFractalProvider)
                .withName("Is fractal")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor zPositionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zPositionProvider)
                .withName("Z position")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor colorScaleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorScaleProvider)
                .withName("Color scale")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor startPointDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startPointProvider)
                .withName("Start point")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor seedDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(seedProvider)
                .withName("Seed")
                .withGroup("perturbation")
                .build();
        ValueProviderDescriptor verticalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(verticalMultiplierProvider)
                .withName("vertical multiplier")
                .withGroup("displacement")
                .build();
        ValueProviderDescriptor horizontalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(horizontalMultiplierProvider)
                .withName("horizontal multiplier")
                .withGroup("displacement")
                .build();

        List<ValueProviderDescriptor> descriptors = new ArrayList<>();
        descriptors.add(frequencyDescriptor);
        descriptors.add(perturbationDescriptor);
        descriptors.add(isFractalDescriptor);
        descriptors.add(zPositionDescriptor);
        descriptors.add(colorScaleDescriptor);
        descriptors.add(startPointDescriptor);
        descriptors.add(seedDescriptor);

        descriptors.add(verticalDisplacementMultiplierProviderDescriptor);
        descriptors.add(horizontalDisplacementMultiplierProviderDescriptor);

        return descriptors;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PerturbationDistortEffect(this, cloneRequestMetadata);
    }

}
