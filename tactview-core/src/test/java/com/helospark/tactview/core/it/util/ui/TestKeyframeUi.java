package com.helospark.tactview.core.it.util.ui;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CompositeKeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;

public class TestKeyframeUi {
    private EffectParametersRepository parametersRepository;
    private ValueProviderDescriptor descriptor;
    private TimelinePosition position = TimelinePosition.ofZero();

    public TestKeyframeUi(EffectParametersRepository parametersRepository, ValueProviderDescriptor descriptor) {
        this.parametersRepository = parametersRepository;
        this.descriptor = descriptor;
    }

    public TestKeyframeUi moveToPosition(TimelinePosition position) {
        this.position = position;
        return this;
    }

    public void addKeyframe(double value) {
        DoubleProvider doubleProvider = (DoubleProvider) descriptor.getKeyframeableEffect();
        setKeyframeForPrimitive(value, doubleProvider);
    }

    public void addKeyframe(InterpolationLine interpolationLine) {
        LineProvider lineProvider = (LineProvider) descriptor.getKeyframeableEffect();

        addKeyframeForChild(interpolationLine, lineProvider);
    }

    public TestKeyframeUi addKeyframe(Point point) {
        PointProvider pointProvider = (PointProvider) descriptor.getKeyframeableEffect();

        addKeyframeForChild(point, pointProvider);
        return this;
    }

    public TestKeyframeUi addKeyframe(Color color) {
        ColorProvider colorProvider = (ColorProvider) descriptor.getKeyframeableEffect();

        addKeyframeForChild(color, colorProvider);

        return this;
    }

    private void addKeyframeForChild(Object data, CompositeKeyframeableEffect composite) {
        KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(composite.getId())
                .withGlobalTimelinePosition(position)
                .withValue(data)
                .build();
        parametersRepository.keyframeAdded(keyframeAddedRequest);
    }

    private void setKeyframeForPrimitive(Object value, KeyframeableEffect effect) {
        KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(effect.getId())
                .withGlobalTimelinePosition(position)
                .withValue(value)
                .build();
        parametersRepository.keyframeAdded(keyframeAddedRequest);
    }

    public TestKeyframeUi enableKeyframes() {
        enableKeyframes(true);
        return this;
    }

    public TestKeyframeUi disableKeyframes() {
        enableKeyframes(false);
        return this;
    }

    private TestKeyframeUi enableKeyframes(boolean status) {
        KeyframeableEffect provider = descriptor.getKeyframeableEffect();

        parametersRepository.setUsingKeyframes(provider.getId(), status);

        return this;
    }

}
