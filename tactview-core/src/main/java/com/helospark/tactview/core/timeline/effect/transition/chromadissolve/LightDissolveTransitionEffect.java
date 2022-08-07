package com.helospark.tactview.core.timeline.effect.transition.chromadissolve;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
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

    public LightDissolveTransitionEffect(LightDissolveTransitionEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public LightDissolveTransitionEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();
        ValueListElement direction = directionProvider.getValueAt(transitionRequest.getEffectPosition(), transitionRequest.getEvaluationContext());

        ClipImage result = ClipImage.sameSizeAs(transitionRequest.getFirstFrame());

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

    private void copyPixelFrom(ReadOnlyClipImage copyFrom, ClipImage result, Integer x, Integer y) {
        for (int i = 0; i < 4; ++i) {
            int color = copyFrom.getColorComponentWithOffset(x, y, i);
            result.setColorComponentByOffset(color, x, y, i);
        }
    }

    private int getLight(int x, int y, ReadOnlyClipImage firstFrame) {
        int result = 0;
        for (int i = 0; i < 3; ++i) {
            result += firstFrame.getColorComponentWithOffset(x, y, i);
        }
        return result / 3;
    }

    @Override
    protected void initializeValueProviderInternal() {
        super.initializeValueProviderInternal();

        directionProvider = new ValueListProvider<>(createList(), new StepStringInterpolator(DISSOLVE_DARK_FIRST));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = super.getValueProvidersInternal();

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
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LightDissolveTransitionEffect(this, cloneRequestMetadata);
    }

}
