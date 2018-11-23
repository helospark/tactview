package com.helospark.tactview.core.timeline.effect.transition.chromadissolve;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LightDissolveTransitionEffect extends AbstractVideoTransitionEffect {
    private static final String DISSOLVE_LIGHT_FIRST = "light";
    private static final String DISSOLVE_DARK_FIRST = "dark";

    private IndependentPixelOperation independentPixelOperation;

    private ValueListProvider<ValueListElement> directionProvider;

    public LightDissolveTransitionEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public LightDissolveTransitionEffect(LightDissolveTransitionEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    protected ClipFrameResult applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();
        ValueListElement direction = directionProvider.getValueAt(transitionRequest.getEffectPosition());

        ClipFrameResult result = ClipFrameResult.sameSizeAs(transitionRequest.getFirstFrame());

        if (direction.getId().equals(DISSOLVE_DARK_FIRST)) {
            independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
                double currentLightLevel = getLight(x, y, transitionRequest.getFirstFrame()) / 255.0;
                if (currentLightLevel >= progress) {
                    copyPixelFrom(transitionRequest.getFirstFrame(), result, x, y);
                } else {
                    copyPixelFrom(transitionRequest.getSecondFrame(), result, x, y);
                }
            });
        } else {
            independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
                double currentLightLevel = getLight(x, y, transitionRequest.getFirstFrame()) / 255.0;
                if (currentLightLevel >= (1.0 - progress)) {
                    copyPixelFrom(transitionRequest.getSecondFrame(), result, x, y);
                } else {
                    copyPixelFrom(transitionRequest.getFirstFrame(), result, x, y);
                }
            });
        }

        return result;
    }

    private void copyPixelFrom(ClipFrameResult copyFrom, ClipFrameResult result, Integer x, Integer y) {
        for (int i = 0; i < 4; ++i) {
            int color = copyFrom.getColorComponentWithOffset(x, y, i);
            result.setColorComponentByOffset(color, x, y, i);
        }
    }

    private int getLight(int x, int y, ClipFrameResult firstFrame) {
        int result = 0;
        for (int i = 0; i < 3; ++i) {
            result += firstFrame.getColorComponentWithOffset(x, y, i);
        }
        return result / 3;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = super.getValueProviders();

        directionProvider = new ValueListProvider<>(createList(), new StringInterpolator(DISSOLVE_DARK_FIRST));

        ValueProviderDescriptor directionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(directionProvider)
                .withName("dissolve first")
                .build();

        result.add(directionProviderDescriptor);

        return result;
    }

    private List<ValueListElement> createList() {
        return List.of(new ValueListElement(DISSOLVE_DARK_FIRST, DISSOLVE_DARK_FIRST),
                new ValueListElement(DISSOLVE_LIGHT_FIRST, DISSOLVE_LIGHT_FIRST));
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new LightDissolveTransitionEffect(this);
    }

}
