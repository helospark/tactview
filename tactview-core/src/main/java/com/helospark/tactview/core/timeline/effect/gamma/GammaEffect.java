package com.helospark.tactview.core.timeline.effect.gamma;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GammaEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider gammaProvider;

    public GammaEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public GammaEffect(GammaEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public GammaEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperations) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperations;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double gamma = gammaProvider.getValueAt(request.getEffectPosition());

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            // if needed lookup table could be used for performance reasons
            pixelRequest.output[0] = (int) (Math.pow(pixelRequest.input[0] / 255.0, gamma) * 255.0);
            pixelRequest.output[1] = (int) (Math.pow(pixelRequest.input[1] / 255.0, gamma) * 255.0);
            pixelRequest.output[2] = (int) (Math.pow(pixelRequest.input[2] / 255.0, gamma) * 255.0);
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    @Override
    public void initializeValueProvider() {
        gammaProvider = new DoubleProvider(0, 3, new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor gammaDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(gammaProvider)
                .withName("gamma")
                .build();

        return List.of(gammaDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new GammaEffect(this);
    }

}
