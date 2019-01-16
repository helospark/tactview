package com.helospark.tactview.core.it.util.ui;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
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
        setKeyframeForPrimitive(String.valueOf(value), doubleProvider);
    }

    public void addKeyframe(InterpolationLine interpolationLine) {
        LineProvider lineProvider = (LineProvider) descriptor.getKeyframeableEffect();

        PointProvider point1 = (PointProvider) lineProvider.getChildren().get(0);
        PointProvider point2 = (PointProvider) lineProvider.getChildren().get(1);

        addKeyframeForChild(String.valueOf(interpolationLine.start.x), 0, point1);
        addKeyframeForChild(String.valueOf(interpolationLine.start.y), 1, point1);

        addKeyframeForChild(String.valueOf(interpolationLine.end.x), 0, point2);
        addKeyframeForChild(String.valueOf(interpolationLine.end.y), 1, point2);
    }

    public TestKeyframeUi addKeyframe(Color color) {
        ColorProvider colorProvider = (ColorProvider) descriptor.getKeyframeableEffect();

        addKeyframeForChild(String.valueOf(color.red), 0, colorProvider);
        addKeyframeForChild(String.valueOf(color.green), 1, colorProvider);
        addKeyframeForChild(String.valueOf(color.blue), 2, colorProvider);

        return this;
    }

    private void addKeyframeForChild(String data, int index, CompositeKeyframeableEffect colorProvider) {
        KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(colorProvider.getChildren().get(index).getId())
                .withGlobalTimelinePosition(position)
                .withValue(data)
                .build();
        parametersRepository.keyframeAdded(keyframeAddedRequest);
    }

    private void setKeyframeForPrimitive(String value, KeyframeableEffect effect) {
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
