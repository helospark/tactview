package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;
import java.util.Optional;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;

public class DependentClipProvider extends KeyframeableEffect {
    private StringInterpolator stringInterpolator;

    public DependentClipProvider(StringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
    }

    public Optional<ClipFrameResult> getValueAt(TimelinePosition position, Map<String, ClipFrameResult> clips) {
        return Optional.ofNullable(clips.get(stringInterpolator.valueAt(position)));
    }

    @Override
    public String getValueAt(TimelinePosition position) {
        return stringInterpolator.valueAt(position);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

    @Override
    public KeyframeableEffect deepClone() {
        return new DependentClipProvider(stringInterpolator.deepClone());
    }

}
