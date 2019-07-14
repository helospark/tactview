package com.helospark.tactview.core.timeline.effect.rotate;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService.RotateServiceRequest;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RotateEffect extends StatelessVideoEffect {
    private DoubleProvider angleProvider;

    private RotateService rotateService;

    public RotateEffect(TimelineInterval interval, RotateService rotateService) {
        super(interval);
        this.rotateService = rotateService;
    }

    public RotateEffect(RotateEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public RotateEffect(JsonNode node, LoadMetadata loadMetadata, RotateService rotateService) {
        super(node, loadMetadata);
        this.rotateService = rotateService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double degrees = angleProvider.getValueAt(request.getClipPosition());

        RotateServiceRequest serviceRequest = RotateServiceRequest.builder()
                .withAngle(degrees)
                .withImage(request.getCurrentFrame())
                .withCenterX(0.5)
                .withCenterY(0.5)
                .build();

        return rotateService.rotate(serviceRequest);
    }

    @Override
    public void initializeValueProvider() {
        angleProvider = new DoubleProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

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
