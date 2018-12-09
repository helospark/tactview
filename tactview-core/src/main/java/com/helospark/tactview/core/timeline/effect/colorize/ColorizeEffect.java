package com.helospark.tactview.core.timeline.effect.colorize;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ColorizeEffect extends StatelessVideoEffect {
    private ColorizeService colorizeService;

    private DoubleProvider hueChangeProvider;
    private DoubleProvider saturationChangeProvider;
    private DoubleProvider valueChangeProvider;

    public ColorizeEffect(TimelineInterval interval, ColorizeService colorizeService) {
        super(interval);
        this.colorizeService = colorizeService;
    }

    public ColorizeEffect(ColorizeEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public ColorizeEffect(JsonNode node, ColorizeService colorizeService2) {
        super(node);
        this.colorizeService = colorizeService2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double hueChange = hueChangeProvider.getValueAt(request.getEffectPosition());
        double saturationChange = saturationChangeProvider.getValueAt(request.getEffectPosition());
        double valueChange = valueChangeProvider.getValueAt(request.getEffectPosition());

        ColorizeRequest colorizeRequest = ColorizeRequest.builder()
                .withHueChange(hueChange)
                .withSaturationChange(saturationChange)
                .withValueChange(valueChange)
                .build();

        return colorizeService.colorize(request.getCurrentFrame(), colorizeRequest);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        hueChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        saturationChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        valueChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        ValueProviderDescriptor hueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(hueChangeProvider)
                .withName("Hue")
                .build();

        ValueProviderDescriptor saturationDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(saturationChangeProvider)
                .withName("Saturation")
                .build();

        ValueProviderDescriptor valueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(valueChangeProvider)
                .withName("Value")
                .build();

        return List.of(hueDescriptor, saturationDescriptor, valueDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new ColorizeEffect(this);
    }

}
