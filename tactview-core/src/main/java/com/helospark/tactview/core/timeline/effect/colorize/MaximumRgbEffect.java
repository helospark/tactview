package com.helospark.tactview.core.timeline.effect.colorize;

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
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class MaximumRgbEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    public MaximumRgbEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MaximumRgbEffect(MaximumRgbEffect maximumRgbEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(maximumRgbEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(maximumRgbEffect, this);
    }

    public MaximumRgbEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            int maxIndex = 0;
            int maxValue = pixelRequest.input[maxIndex];
            for (int i = 1; i < 3; ++i) {
                if (pixelRequest.input[i] > maxValue) {
                    maxIndex = i;
                    maxValue = pixelRequest.input[i];
                }
            }

            for (int i = 0; i < 3; ++i) {
                if (i == maxIndex) {
                    pixelRequest.output[i] = pixelRequest.input[i];
                } else {
                    pixelRequest.output[i] = 0;
                }
            }
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    @Override
    public void initializeValueProvider() {

    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new MaximumRgbEffect(this, cloneRequestMetadata);
    }

}
