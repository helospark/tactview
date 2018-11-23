package com.helospark.tactview.core.timeline.effect.transition.blurtransition;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVGaussianBlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVRegion;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class BlurTransition extends AbstractVideoTransitionEffect {
    private OpenCVBasedGaussianBlur gaussianBlur;
    private IntegerProvider maxBlurProvider;

    public BlurTransition(TimelineInterval interval, OpenCVBasedGaussianBlur gaussianBlur) {
        super(interval);
        this.gaussianBlur = gaussianBlur;
    }

    public BlurTransition(BlurTransition cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    protected ClipFrameResult applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();

        ClipFrameResult result = ClipFrameResult.sameSizeAs(transitionRequest.getFirstFrame());

        ClipFrameResult currentFrame;
        double maxValue = maxBlurProvider.getValueAt(transitionRequest.getEffectPosition()) / transitionRequest.getScale();
        int blurValue;
        if (progress < 0.5) {
            currentFrame = transitionRequest.getFirstFrame();
            blurValue = (int) (maxValue * progress) * 2 + 1;
        } else {
            currentFrame = transitionRequest.getSecondFrame();
            blurValue = (int) (maxValue * (1.0 - progress)) * 2 + 1;
        }
        applyBlur(currentFrame, result, blurValue);

        return result;
    }

    private void applyBlur(ClipFrameResult currentFrame, ClipFrameResult result, int blurValue) {
        OpenCVGaussianBlurRequest nativeRequest = new OpenCVGaussianBlurRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.kernelWidth = blurValue;
        nativeRequest.kernelHeight = blurValue;
        nativeRequest.blurRegion = new OpenCVRegion();
        nativeRequest.blurRegion.x = 0;
        nativeRequest.blurRegion.y = 0;
        nativeRequest.blurRegion.width = currentFrame.getWidth();
        nativeRequest.blurRegion.height = currentFrame.getHeight();

        gaussianBlur.applyGaussianBlur(nativeRequest);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = super.getValueProviders();

        maxBlurProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(50.0));

        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maxBlurProvider)
                .withName("Max blur")
                .build();

        result.add(sizeDescriptor);

        return result;
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new BlurTransition(this);
    }

}
