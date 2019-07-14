package com.helospark.tactview.core.timeline.effect.layermask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskAlphaCalculator;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskTypeListElement;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskGrayscaleToAlpha;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class LayerMaskEffect extends StatelessVideoEffect {
    private LayerMaskApplier layerMaskApplier;
    private List<LayerMaskAlphaCalculator> calculators;

    private DependentClipProvider layerMaskProvider;
    private BooleanProvider invertProvider;

    private ValueListProvider<LayerMaskTypeListElement> layerMaskTypeProvider;

    public LayerMaskEffect(TimelineInterval interval, LayerMaskApplier layerMaskApplier, List<LayerMaskAlphaCalculator> calculators) {
        super(interval);
        this.layerMaskApplier = layerMaskApplier;
        this.calculators = calculators;
    }

    public LayerMaskEffect(LayerMaskEffect layerMaskEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(layerMaskEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(layerMaskEffect, this);
    }

    public LayerMaskEffect(JsonNode node, LoadMetadata loadMetadata, LayerMaskApplier layerMaskApplier2, List<LayerMaskAlphaCalculator> calculators2) {
        super(node, loadMetadata);
        this.layerMaskApplier = layerMaskApplier2;
        this.calculators = calculators2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Optional<ReadOnlyClipImage> layerMask = layerMaskProvider.getValueAt(request.getEffectPosition(), request.getRequestedClips());
        LayerMaskAlphaCalculator layerMaskType = layerMaskTypeProvider.getValueAt(request.getEffectPosition()).getLayerMaskAlphaCalculator();
        if (layerMask.isPresent()) {
            LayerMaskApplyRequest layerMaskRequest = LayerMaskApplyRequest.builder()
                    .withCurrentFrame(request.getCurrentFrame())
                    .withMask(layerMask.get())
                    .withScaleLayerMask(true)
                    .withCalculator(layerMaskType)
                    .withInvert(invertProvider.getValueAt(request.getEffectPosition()))
                    .build();

            return layerMaskApplier.createNewImageWithLayerMask(layerMaskRequest);
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }
    }

    @Override
    public void initializeValueProvider() {
        layerMaskProvider = new DependentClipProvider(new StepStringInterpolator());
        layerMaskTypeProvider = new ValueListProvider<>(getList(calculators), new StepStringInterpolator(getDefault(calculators)));
        invertProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

        ValueProviderDescriptor layerMaskProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(layerMaskProvider)
                .withName("Layer mask")
                .build();

        ValueProviderDescriptor layerMaskTypeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(layerMaskTypeProvider)
                .withName("Type")
                .build();
        ValueProviderDescriptor invertProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(invertProvider)
                .withName("Invert")
                .build();

        result.add(layerMaskProviderDescriptor);
        result.add(layerMaskTypeDescriptor);
        result.add(invertProviderDescriptor);

        return result;
    }

    private String getDefault(List<LayerMaskAlphaCalculator> calculators) {
        return calculators.stream()
                .filter(a -> a instanceof LayerMaskGrayscaleToAlpha)
                .findFirst()
                .orElse(calculators.get(0))
                .getId();
    }

    private List<LayerMaskTypeListElement> getList(List<LayerMaskAlphaCalculator> calculators) {
        return calculators.stream()
                .map(a -> new LayerMaskTypeListElement(a, a.getId(), a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LayerMaskEffect(this, cloneRequestMetadata);
    }

    @Override
    public List<String> getClipDependency(TimelinePosition position) {
        List<String> result = new ArrayList<>();
        String clip = layerMaskProvider.getValueAt(position);
        if (clip != "") {
            result.add(clip);
        }
        return result;
    }

}
