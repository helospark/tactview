package com.helospark.tactview.core.timeline.effect.invert;

import java.util.Collections;
import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class InvertEffect extends StatelessVideoEffect {
    private static final int MAX_PIXEL_VALUE = 255;
    private IndependentPixelOperation independentPixelOperation;

    public InvertEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public InvertEffect(InvertEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    public ClipImage createFrame(StatelessEffectRequest request) {
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            pixelRequest.output[0] = MAX_PIXEL_VALUE - pixelRequest.input[0];
            pixelRequest.output[1] = MAX_PIXEL_VALUE - pixelRequest.input[1];
            pixelRequest.output[2] = MAX_PIXEL_VALUE - pixelRequest.input[2];
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return Collections.emptyList();
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new InvertEffect(this);
    }

}
