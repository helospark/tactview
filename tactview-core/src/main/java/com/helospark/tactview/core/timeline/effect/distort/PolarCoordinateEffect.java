package com.helospark.tactview.core.timeline.effect.distort;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.distort.service.PolarService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class PolarCoordinateEffect extends StatelessVideoEffect {
    private PolarService polarService;

    private DoubleProvider circleDepthProvider;
    private DoubleProvider offsetAngleProvider;
    private BooleanProvider mapBackwardProvider;
    private BooleanProvider toPolarProvider;
    private BooleanProvider mapFromTopProvider;

    public PolarCoordinateEffect(TimelineInterval interval, PolarService polarService) {
        super(interval);
        this.polarService = polarService;
    }

    public PolarCoordinateEffect(PolarCoordinateEffect polarCoordinateEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(polarCoordinateEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(polarCoordinateEffect, this);
    }

    public PolarCoordinateEffect(JsonNode node, LoadMetadata loadMetadata, PolarService polarService) {
        super(node, loadMetadata);
        this.polarService = polarService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        double circleDepth = circleDepthProvider.getValueAt(request.getEffectPosition());
        double offsetAngleDepth = offsetAngleProvider.getValueAt(request.getEffectPosition());
        boolean mapBackward = mapBackwardProvider.getValueAt(request.getEffectPosition());
        boolean toPolar = toPolarProvider.getValueAt(request.getEffectPosition());
        boolean mapFromTop = mapFromTopProvider.getValueAt(request.getEffectPosition());

        PolarOperationRequest polarRequest = PolarOperationRequest.builder()
                .withCircleDepth(circleDepth)
                .withCurrentFrame(currentFrame)
                .withMapBackward(mapBackward)
                .withMapFromTop(mapFromTop)
                .withOffsetAngleDepth(offsetAngleDepth)
                .withToPolar(toPolar)
                .build();

        return polarService.polarOperation(polarRequest);
    }

    @Override
    public void initializeValueProvider() {
        circleDepthProvider = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(100.0));
        offsetAngleProvider = new DoubleProvider(0.0, 2.0 * Math.PI, new MultiKeyframeBasedDoubleInterpolator(0.0));
        mapBackwardProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        toPolarProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        mapFromTopProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor circleDepthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(circleDepthProvider)
                .withName("Circle deapth")
                .build();
        ValueProviderDescriptor offsetAngleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offsetAngleProvider)
                .withName("Offset angle")
                .build();
        ValueProviderDescriptor mapBackwardProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mapBackwardProvider)
                .withName("Map backward")
                .build();
        ValueProviderDescriptor mapFromTopProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mapFromTopProvider)
                .withName("Map from top")
                .build();
        ValueProviderDescriptor toPolarProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toPolarProvider)
                .withName("To polar")
                .build();

        return List.of(circleDepthProviderDescriptor, offsetAngleProviderDescriptor, mapBackwardProviderDescriptor, mapFromTopProviderDescriptor, toPolarProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PolarCoordinateEffect(this, cloneRequestMetadata);
    }

}
