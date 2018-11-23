package com.helospark.tactview.core.timeline.effect.scale;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;

public class ScaleEffect extends StatelessVideoEffect {
    private DoubleProvider widthScale;
    private DoubleProvider heightScale;

    private ScaleService scaleService;

    public ScaleEffect(TimelineInterval interval, ScaleService scaleService) {
        super(interval);
        this.scaleService = scaleService;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        int newWidth = (int) (currentFrame.getWidth() * widthScale.getValueAt(request.getEffectPosition()));
        int newHeight = (int) (currentFrame.getHeight() * heightScale.getValueAt(request.getEffectPosition()));

        ScaleRequest scaleRequest = ScaleRequest.builder()
                .withImage(currentFrame)
                .withNewWidth(newWidth)
                .withNewHeight(newHeight)
                .build();

        return scaleService.createScaledImage(scaleRequest);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        widthScale = new DoubleProvider(0, 20, new MultiKeyframeBasedDoubleInterpolator(1.0));
        heightScale = new DoubleProvider(0, 20, new MultiKeyframeBasedDoubleInterpolator(1.0));

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(widthScale)
                .withName("width scale")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(heightScale)
                .withName("height scale")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor);
    }

}
