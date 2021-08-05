package com.helospark.tactview.core.timeline.effect.displacementmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DisplacementMapEffect extends StatelessVideoEffect {
    private DependentClipProvider displacementMapProvider;
    private DoubleProvider verticalDisplacementMultiplierProvider;
    private DoubleProvider horizontalDisplacementMultiplierProvider;

    private DisplacementMapService displacementService;

    public DisplacementMapEffect(TimelineInterval interval, DisplacementMapService displacementService) {
        super(interval);
        this.displacementService = displacementService;
    }

    public DisplacementMapEffect(DisplacementMapEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public DisplacementMapEffect(JsonNode node, LoadMetadata loadMetadata, DisplacementMapService displacementService) {
        super(node, loadMetadata);
        this.displacementService = displacementService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Optional<ReadOnlyClipImage> optionalDisplacementMap = displacementMapProvider.getValueAt(request.getEffectPosition(), request.getRequestedClips());

        double verticalMultiplier = verticalDisplacementMultiplierProvider.getValueAt(request.getEffectPosition()) * request.getScale();
        double horizontalMultiplier = horizontalDisplacementMultiplierProvider.getValueAt(request.getEffectPosition()) * request.getScale();
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        if (optionalDisplacementMap.isPresent()) {
            ApplyDisplacementMapRequest displacementMapRequest = ApplyDisplacementMapRequest.builder()
                    .withCurrentFrame(currentFrame)
                    .withDisplacementMap(optionalDisplacementMap.get())
                    .withHorizontalMultiplier(horizontalMultiplier)
                    .withVerticalMultiplier(verticalMultiplier)
                    .build();

            return displacementService.applyDisplacementMap(displacementMapRequest);
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }
    }

    @Override
    public List<String> getClipDependency(TimelinePosition position) {
        List<String> result = super.getClipDependency(position);
        String displacementMap = displacementMapProvider.getValueAt(position);

        if (!displacementMap.isEmpty()) {
            result.add(displacementMap);
        }

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        displacementMapProvider = new DependentClipProvider(new StepStringInterpolator());
        verticalDisplacementMultiplierProvider = new DoubleProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        horizontalDisplacementMultiplierProvider = new DoubleProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

        ValueProviderDescriptor displacementMapProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(displacementMapProvider)
                .withName("displacement map")
                .build();
        ValueProviderDescriptor verticalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(verticalDisplacementMultiplierProvider)
                .withName("vertical multiplier")
                .build();
        ValueProviderDescriptor horizontalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(horizontalDisplacementMultiplierProvider)
                .withName("horizontal multiplier")
                .build();

        result.add(displacementMapProviderDescriptor);
        result.add(verticalDisplacementMultiplierProviderDescriptor);
        result.add(horizontalDisplacementMultiplierProviderDescriptor);

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new DisplacementMapEffect(this, cloneRequestMetadata);
    }

}
