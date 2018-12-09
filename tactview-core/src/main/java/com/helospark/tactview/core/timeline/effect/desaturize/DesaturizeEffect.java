package com.helospark.tactview.core.timeline.effect.desaturize;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DesaturizeEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperations;

    public DesaturizeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperations) {
        super(interval);
        this.independentPixelOperations = independentPixelOperations;
    }

    public DesaturizeEffect(DesaturizeEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DesaturizeEffect(JsonNode node, IndependentPixelOperation independentPixelOperations2) {
        super(node);
        this.independentPixelOperations = independentPixelOperations2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        return independentPixelOperations.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            int desaturized = (pixelRequest.input[0] + pixelRequest.input[1] + pixelRequest.input[2]) / 3;
            pixelRequest.output[0] = desaturized;
            pixelRequest.output[1] = desaturized;
            pixelRequest.output[2] = desaturized;
            pixelRequest.output[3] = pixelRequest.input[3];
        });

    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return Collections.emptyList();
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new DesaturizeEffect(this);
    }

}
