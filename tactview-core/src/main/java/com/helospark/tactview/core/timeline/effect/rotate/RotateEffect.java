package com.helospark.tactview.core.timeline.effect.rotate;

import java.util.Arrays;
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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RotateEffect extends StatelessVideoEffect {
    private DoubleProvider angleProvider;

    private RotateService rotateService;

    public RotateEffect(TimelineInterval interval, RotateService rotateService) {
        super(interval);
        this.rotateService = rotateService;
    }

    public RotateEffect(RotateEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public RotateEffect(JsonNode node, LoadMetadata loadMetadata, RotateService rotateService) {
        super(node, loadMetadata);
        this.rotateService = rotateService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double degrees = angleProvider.getValueAt(request.getClipPosition(), request.getEvaluationContext());

        RotateServiceRequest serviceRequest = RotateServiceRequest.builder()
                .withAngle(degrees)
                .withImage(request.getCurrentFrame())
                .withCenterX(0.5)
                .withCenterY(0.5)
                .build();

        return rotateService.rotate(serviceRequest);
    }

    @Override
    protected void initializeValueProviderInternal() {
        angleProvider = new DoubleProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor angleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(angleProvider)
                .withName("angle")
                .build();

        return Arrays.asList(angleDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new RotateEffect(this, cloneRequestMetadata);
    }

}
