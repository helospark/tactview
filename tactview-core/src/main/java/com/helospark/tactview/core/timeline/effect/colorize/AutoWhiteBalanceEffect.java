package com.helospark.tactview.core.timeline.effect.colorize;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class AutoWhiteBalanceEffect extends StatelessVideoEffect {
    private ColorProvider colorProvider;
    private DoubleProvider tintMultiplierProvider;
    private DoubleProvider temperatureMultiplierProvider;

    private ColorTemperatureService colorTemperatureService;

    public AutoWhiteBalanceEffect(TimelineInterval interval, ColorTemperatureService colorTemperatureService) {
        super(interval);
        this.colorTemperatureService = colorTemperatureService;
    }

    public AutoWhiteBalanceEffect(AutoWhiteBalanceEffect maximumRgbEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(maximumRgbEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(maximumRgbEffect, this, cloneRequestMetadata);
    }

    public AutoWhiteBalanceEffect(JsonNode node, LoadMetadata loadMetadata, ColorTemperatureService colorTemperatureService) {
        super(node, loadMetadata);
        this.colorTemperatureService = colorTemperatureService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Color color = colorProvider.getValueAt(request.getEffectPosition());

        double tintMultiplier = tintMultiplierProvider.getValueAt(request.getEffectPosition());
        double temperatureMultiplier = temperatureMultiplierProvider.getValueAt(request.getEffectPosition());

        double temperatureChange = ((color.blue - color.red) / 2.0) * temperatureMultiplier;
        double tintChange = ((color.red + temperatureChange) - color.green) * tintMultiplier;

        ColorTemperatureChangeRequest temperatureChangeRequest = ColorTemperatureChangeRequest.builder()
                .withTemperatureChange(temperatureChange)
                .withTintChange(tintChange)
                .build();

        return colorTemperatureService.createNewImageWithAppliedTemperatureChange(request.getCurrentFrame(), temperatureChangeRequest);
    }

    @Override
    protected void initializeValueProviderInternal() {
        colorProvider = ColorProvider.fromDefaultValue(1.0, 1.0, 1.0);
        tintMultiplierProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.5));
        temperatureMultiplierProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Select a color that should be white")
                .build();
        ValueProviderDescriptor tintMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(tintMultiplierProvider)
                .withName("Tint multiplier")
                .build();
        ValueProviderDescriptor temperatureMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(temperatureMultiplierProvider)
                .withName("Temperature multiplier")
                .build();

        return List.of(colorProviderDescriptor, tintMultiplierDescriptor, temperatureMultiplierDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new AutoWhiteBalanceEffect(this, cloneRequestMetadata);
    }

}
