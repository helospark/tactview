package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.DesSerFactory;

public class TypingStringInterpolator implements StringInterpolator {
    StepStringInterpolator stepStringInterpolator;

    public TypingStringInterpolator() {
        this.stepStringInterpolator = new StepStringInterpolator();
    }

    public TypingStringInterpolator(String defaultValue) {
        this.stepStringInterpolator = new StepStringInterpolator(defaultValue);
    }

    public TypingStringInterpolator(TreeMap<TimelinePosition, String> values) {
        this.stepStringInterpolator = new StepStringInterpolator(values);
    }

    public TypingStringInterpolator(String defaultValue, TreeMap<TimelinePosition, String> values, boolean useKeyframes) {
        this.stepStringInterpolator = new StepStringInterpolator(defaultValue, values, useKeyframes);
    }

    public TypingStringInterpolator(StepStringInterpolator stepStringInterpolator) {
        this.stepStringInterpolator = stepStringInterpolator;
    }

    @Override
    public String valueAt(TimelinePosition position) {
        if (!stepStringInterpolator.useKeyframes) {
            return stepStringInterpolator.defaultValue;
        } else {
            Entry<TimelinePosition, String> currentValue = getPreviousEntry(position);
            Entry<TimelinePosition, String> nextEntry = getNextEntry(position);

            if (currentValue == null) {
                currentValue = new AbstractMap.SimpleEntry<>(TimelinePosition.ofZero(), stepStringInterpolator.defaultValue);
            }
            if (nextEntry == null) {
                return currentValue.getValue();
            }
            if (nextEntry.getKey().getSeconds().equals(currentValue.getKey().getSeconds())) {
                return nextEntry.getValue();
            }
            if (!currentValue.getValue().equals("")) {
                return ""; // TODO later
            }

            TimelinePosition timeDifference = nextEntry.getKey().subtract(currentValue.getKey());
            double progress = position.subtract(currentValue.getKey()).divide(timeDifference.getSeconds()).getSeconds().doubleValue();

            int currentLength = 0; // TODO: later
            int nextLength = nextEntry.getValue().length();

            int numberOfCharsToDisplay = (int) ((nextLength - currentLength) * progress);

            if (numberOfCharsToDisplay >= nextLength) {
                numberOfCharsToDisplay = nextLength - 1;
            }

            return nextEntry.getValue().substring(0, numberOfCharsToDisplay);
        }
    }

    private Entry<TimelinePosition, String> getNextEntry(TimelinePosition position) {
        TreeMap<TimelinePosition, String> values = stepStringInterpolator.values;

        return values.ceilingEntry(position);
    }

    private Entry<TimelinePosition, String> getPreviousEntry(TimelinePosition position) {
        TreeMap<TimelinePosition, String> values = stepStringInterpolator.values;

        return values.floorEntry(position);
    }

    @Override
    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        stepStringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stepStringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean hasKeyframes() {
        return stepStringInterpolator.hasKeyframes();
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stepStringInterpolator.getValues();
    }

    @Override
    public TypingStringInterpolator deepClone(CloneRequestMetadata cloneRequestMetadata) {
        return new TypingStringInterpolator(stepStringInterpolator.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return TypingStringInterpolatorFactory.class;
    }

    @Override
    public boolean useKeyframes() {
        return stepStringInterpolator.useKeyframes;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        stepStringInterpolator.setUseKeyframes(useKeyframes);
    }

    @Override
    public boolean isUsingKeyframes() {
        return stepStringInterpolator.isUsingKeyframes();
    }

    @Override
    public String getDefaultValue() {
        return stepStringInterpolator.getDefaultValue();
    }

    @Override
    public void resetToDefaultValue() {
        this.stepStringInterpolator.resetToDefaultValue();
    }
}
