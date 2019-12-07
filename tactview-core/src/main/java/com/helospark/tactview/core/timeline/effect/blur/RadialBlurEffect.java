package com.helospark.tactview.core.timeline.effect.blur;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.service.RadialBlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.service.RadialBlurService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RadialBlurEffect extends StatelessVideoEffect {
    private RadialBlurService radialBlurService;

    private DoubleProvider angleProvider;
    private PointProvider centerProvider;

    public RadialBlurEffect(TimelineInterval interval, RadialBlurService radialBlurService) {
        super(interval);
        this.radialBlurService = radialBlurService;
    }

    public RadialBlurEffect(RadialBlurEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    public RadialBlurEffect(JsonNode node, LoadMetadata loadMetadata, RadialBlurService radialBlurService) {
        super(node, loadMetadata);
        this.radialBlurService = radialBlurService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        double angle = angleProvider.getValueAt(request.getEffectPosition());
        Point center = centerProvider.getValueAt(request.getEffectPosition()).multiply(currentFrame.getWidth(), currentFrame.getHeight());

        RadialBlurRequest radialBlurRequest = RadialBlurRequest.builder()
                .withAngle(Math.toRadians(angle))
                .withCenterX((int) center.x)
                .withCenterY((int) center.y)
                .withInputImage(currentFrame)
                .build();

        return radialBlurService.radialBlur(radialBlurRequest);
    }

    @Override
    protected void initializeValueProviderInternal() {
        angleProvider = new DoubleProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(10.0));
        centerProvider = PointProvider.ofNormalizedImagePosition(0.5, 0.5);
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(angleProvider)
                .withName("angle")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(centerProvider)
                .withName("center")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new RadialBlurEffect(this, cloneRequestMetadata);
    }

}
