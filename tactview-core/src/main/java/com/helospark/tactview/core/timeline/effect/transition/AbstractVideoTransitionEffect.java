package com.helospark.tactview.core.timeline.effect.transition;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.PercentAwareMultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class AbstractVideoTransitionEffect extends StatelessEffect {
    private DoubleProvider progressProvider;
    private PercentAwareMultiKeyframeBasedDoubleInterpolator interpolator;

    public AbstractVideoTransitionEffect(TimelineInterval interval) {
        super(interval);
    }

    public AbstractVideoTransitionEffect(AbstractVideoTransitionEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public AbstractVideoTransitionEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public ClipImage applyTransition(ExternalStatelessVideoTransitionEffectRequest request) {
        TimelinePosition effectPosition = request.getGlobalPosition().from(parentIntervalAware.getInterval().getStartPosition()).from(interval.getStartPosition());
        double progress = progressProvider.getValueAt(effectPosition).doubleValue();
        InternalStatelessVideoTransitionEffectRequest transitionRequest = InternalStatelessVideoTransitionEffectRequest.builder()
                .withClipPosition(request.getGlobalPosition().from(parentIntervalAware.getInterval().getStartPosition()))
                .withEffectPosition(effectPosition)
                .withProgress(progress)
                .withFirstFrame(request.getFirstFrame())
                .withSecondFrame(request.getSecondFrame())
                .withGlobalPosition(request.getGlobalPosition())
                .withScale(request.getScale())
                .build();

        if (request.getFirstFrame().getWidth() != request.getSecondFrame().getWidth() ||
                request.getFirstFrame().getHeight() != request.getSecondFrame().getHeight()) {
            throw new IllegalArgumentException("Transition must be called with the images with the same dimension");
        }

        ClipImage result = applyTransitionInternal(transitionRequest);

        if (result.getWidth() != request.getFirstFrame().getWidth() || result.getHeight() != request.getFirstFrame().getHeight()) {
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
        interpolator = new PercentAwareMultiKeyframeBasedDoubleInterpolator(values, TimelineLength.ofSeconds(1.0));
        progressProvider = new DoubleProvider(0.0, 1.0, interpolator);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor progressDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(progressProvider)
                .withName("progress")
                .build();

        return new ArrayList<>(List.of(progressDescriptor));
    }

    @Override
    public void notifyAfterResize() {
        interpolator.resizeTo(interval.getLength()); // TODO: shouldn't it already know?
    }

    @Override
    public void notifyAfterInitialized() {
        interpolator.resizeTo(interval.getLength());
    }

}
