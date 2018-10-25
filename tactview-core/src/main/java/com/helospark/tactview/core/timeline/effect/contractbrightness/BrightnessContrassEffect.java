package com.helospark.tactview.core.timeline.effect.contractbrightness;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class BrightnessContrassEffect extends StatelessVideoEffect {
    private DoubleProvider contrastProvider;
    private DoubleProvider brightnessProvider;

    private IndependentPixelOperation independentPixelOperations;

    public BrightnessContrassEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperations) {
        super(interval);
        this.independentPixelOperations = independentPixelOperations;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest effectRequest) {
        double contrast = contrastProvider.getValueAt(effectRequest.getEffectPosition());
        double brightness = brightnessProvider.getValueAt(effectRequest.getEffectPosition());

        ClipFrameResult currentFrame = effectRequest.getCurrentFrame();

        return independentPixelOperations.createNewImageWithAppliedTransformation(currentFrame, pixelRequest -> {
            pixelRequest.output[0] = (int) (contrast * pixelRequest.input[0] + brightness);
            pixelRequest.output[1] = (int) (contrast * pixelRequest.input[1] + brightness);
            pixelRequest.output[2] = (int) (contrast * pixelRequest.input[2] + brightness);
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        contrastProvider = new DoubleProvider(0, 10, new DoubleInterpolator(1.0));
        brightnessProvider = new DoubleProvider(0, 200, new DoubleInterpolator(0.0));

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

}
