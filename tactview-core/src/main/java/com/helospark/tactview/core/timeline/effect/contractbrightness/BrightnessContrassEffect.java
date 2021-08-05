package com.helospark.tactview.core.timeline.effect.contractbrightness;

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

public class BrightnessContrassEffect extends StatelessVideoEffect {
    private DoubleProvider contrastProvider;
    private DoubleProvider brightnessProvider;

    private BrignessContrastService brignessContrastService;

    public BrightnessContrassEffect(TimelineInterval interval, BrignessContrastService brignessContrastService) {
        super(interval);
        this.brignessContrastService = brignessContrastService;
    }

    public BrightnessContrassEffect(BrightnessContrassEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public BrightnessContrassEffect(JsonNode node, LoadMetadata loadMetadata, BrignessContrastService brignessContrastService2) {
        super(node, loadMetadata);
        this.brignessContrastService = brignessContrastService2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest effectRequest) {
        double contrast = contrastProvider.getValueAt(effectRequest.getEffectPosition());
        double brightness = brightnessProvider.getValueAt(effectRequest.getEffectPosition());

        ReadOnlyClipImage currentFrame = effectRequest.getCurrentFrame();

        BrignessContrastServiceRequest brightnessContrastRequest = BrignessContrastServiceRequest.builder()
                .withBrightness(brightness)
                .withContrast(contrast)
                .build();

        return brignessContrastService.createImageWithBrighnessContrastChange(currentFrame, brightnessContrastRequest);
    }

    @Override
    protected void initializeValueProviderInternal() {
        contrastProvider = new DoubleProvider(0, 10, new MultiKeyframeBasedDoubleInterpolator(1.0));
        brightnessProvider = new DoubleProvider(0, 10, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor contrastDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(contrastProvider)
                .withName("contrast")
                .build();
        ValueProviderDescriptor brightnessDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brightnessProvider)
                .withName("brightness")
                .build();

        return List.of(contrastDescriptor, brightnessDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new BrightnessContrassEffect(this, cloneRequestMetadata);
    }

}
