package com.helospark.tactview.core.timeline.effect.desaturize;

import java.util.Collections;
import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class DesaturizeEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperations;

    public DesaturizeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperations) {
        super(interval);
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
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

}
