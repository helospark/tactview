package com.helospark.tactview.core.timeline.effect.transition.flash;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class WhiteFlashTransition extends AbstractVideoTransitionEffect {
    private DoubleProvider keepAtWhitePercentage;
    private IndependentPixelOperation independentPixelOperation;

    public WhiteFlashTransition(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public WhiteFlashTransition(WhiteFlashTransition whiteFlashTransition) {
        super(whiteFlashTransition);
        ReflectionUtil.copyOrCloneFieldFromTo(whiteFlashTransition, this);
    }

    public WhiteFlashTransition(JsonNode node, IndependentPixelOperation independentPixelOperation2) {
        super(node);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();

        double keepAtWhite = keepAtWhitePercentage.getValueAt(transitionRequest.getEffectPosition());

        double fadeTime = (1.0 - keepAtWhite) / 2.0;

        ReadOnlyClipImage firstFrame = transitionRequest.getFirstFrame();
        ReadOnlyClipImage secondFrame = transitionRequest.getSecondFrame();

        ReadOnlyClipImage currentImageShown;

        double fadeProgress;
        if (progress < 0.5) {
            fadeProgress = progress / fadeTime;
            currentImageShown = firstFrame;
        } else {
            fadeProgress = 1.0 - ((progress - 0.5) / fadeTime);
            currentImageShown = secondFrame;
        }
        fadeProgress = clamp(fadeProgress, 0.0, 1.0);
        int valueToAdd = (int) (fadeProgress * 255);

        ClipImage result = ClipImage.sameSizeAs(firstFrame);

        independentPixelOperation.executePixelTransformation(firstFrame.getWidth(), firstFrame.getHeight(), (x, y) -> {
            for (int i = 0; i < 3; ++i) {
                int color = currentImageShown.getColorComponentWithOffset(x, y, i);
                result.setColorComponentByOffset(color + valueToAdd, x, y, i);
            }
            int alpha = currentImageShown.getAlpha(x, y);
            result.setAlpha(alpha, x, y);
        });

        return result;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new WhiteFlashTransition(this);
    }

    @Override
    public void initializeValueProvider() {
        super.initializeValueProvider();

        keepAtWhitePercentage = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = super.getValueProviders();

        ValueProviderDescriptor keepAtWhitePercentProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(keepAtWhitePercentage)
                .withName("White percent")
                .build();

        result.add(keepAtWhitePercentProviderDescriptor);

        return result;
    }

}
