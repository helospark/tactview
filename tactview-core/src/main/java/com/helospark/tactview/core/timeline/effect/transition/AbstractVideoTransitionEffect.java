package com.helospark.tactview.core.timeline.effect.transition;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.PercentAwareMultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class AbstractVideoTransitionEffect extends StatelessEffect {
    private DoubleProvider progressProvider;
    private ValueListProvider<ValueListElement> transitionDirection;

    public AbstractVideoTransitionEffect(TimelineInterval interval) {
        super(interval);
    }

    public AbstractVideoTransitionEffect(AbstractVideoTransitionEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, AbstractVideoTransitionEffect.class);
    }

    public AbstractVideoTransitionEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public ClipImage applyTransition(ExternalStatelessVideoTransitionEffectRequest request) {
        TimelinePosition effectPosition = request.getGlobalPosition().from(parentIntervalAware.getInterval().getStartPosition()).from(interval.getStartPosition());
        double progress = progressProvider.getValueAt(effectPosition).doubleValue();

        String direction = transitionDirection.getValueAt(effectPosition).getId();

        ReadOnlyClipImage firstFrame;
        ReadOnlyClipImage secondFrame;

        if (direction.equals("down")) {
            firstFrame = request.getFirstFrame();
            secondFrame = request.getSecondFrame();
        } else {
            firstFrame = request.getSecondFrame();
            secondFrame = request.getFirstFrame();
        }

        InternalStatelessVideoTransitionEffectRequest transitionRequest = InternalStatelessVideoTransitionEffectRequest.builder()
                .withClipPosition(request.getGlobalPosition().from(parentIntervalAware.getInterval().getStartPosition()))
                .withEffectPosition(effectPosition)
                .withProgress(progress)
                .withFirstFrame(firstFrame)
                .withSecondFrame(secondFrame)
                .withGlobalPosition(request.getGlobalPosition())
                .withScale(request.getScale())
                .build();

        if (firstFrame.getWidth() != secondFrame.getWidth() ||
                firstFrame.getHeight() != secondFrame.getHeight()) {
            throw new IllegalArgumentException("Transition must be called with the images with the same dimension");
        }

        ClipImage result = applyTransitionInternal(transitionRequest);

        if (result.getWidth() != firstFrame.getWidth() || result.getHeight() != firstFrame.getHeight()) {
            throw new IllegalStateException("Transition is not allowed to resize the image");
        }

        return result;
    }

    protected abstract ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest);

    @Override
    public void initializeValueProvider() {
        TreeMap<TimelinePosition, Double> values = new TreeMap<>();
        values.put(new TimelinePosition(0), 0.0);
        values.put(new TimelinePosition(1), 1.0);
        progressProvider = new DoubleProvider(0.0, 1.0, new PercentAwareMultiKeyframeBasedDoubleInterpolator(values, TimelineLength.ofSeconds(1.0)));
        transitionDirection = new ValueListProvider<>(createDirectionElements(), new StepStringInterpolator("down"));
    }

    private List<ValueListElement> createDirectionElements() {
        return List.of(new ValueListElement("down", "Down"), new ValueListElement("up", "Up"));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor progressDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(progressProvider)
                .withName("progress")
                .build();
        ValueProviderDescriptor transitionDirectionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(transitionDirection)
                .withName("transition direction")
                .build();

        return new ArrayList<>(List.of(progressDescriptor, transitionDirectionDescriptor));
    }

    @Override
    public void notifyAfterResize() {
        getPercentAwareInterpolator().resizeTo(interval.getLength());
    }

    @Override
    public void notifyAfterInitialized() {
        getPercentAwareInterpolator().resizeTo(interval.getLength());
    }

    private PercentAwareMultiKeyframeBasedDoubleInterpolator getPercentAwareInterpolator() {
        return (PercentAwareMultiKeyframeBasedDoubleInterpolator) progressProvider.getInterpolator();
    }

}
