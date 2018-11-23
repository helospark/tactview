package com.helospark.tactview.core.timeline.effect.transition.alphatransition;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class AlphaTransitionEffect extends AbstractVideoTransitionEffect {
    private IndependentPixelOperation independentPixelOperation;

    public AlphaTransitionEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public AlphaTransitionEffect(AlphaTransitionEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    public ClipFrameResult applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        double progress = request.getProgress();
        ClipFrameResult firstFrame = request.getFirstFrame();
        ClipFrameResult secondFrame = request.getSecondFrame();

        ClipFrameResult result = ClipFrameResult.sameSizeAs(request.getFirstFrame());

        independentPixelOperation.executePixelTransformation(firstFrame.getWidth(), firstFrame.getHeight(), (x, y) -> {
            for (int imageChannel = 0; imageChannel < 4; ++imageChannel) {
                int firstColor = firstFrame.getColorComponentWithOffset(x, y, imageChannel);
                int secondColor = secondFrame.getColorComponentWithOffset(x, y, imageChannel);
                int newColor = (int) (secondColor * progress + firstColor * (1.0 - progress));
                result.setColorComponentByOffset(newColor, x, y, imageChannel);
            }
        });
        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return super.getValueProviders();
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new AlphaTransitionEffect(this);
    }

}
