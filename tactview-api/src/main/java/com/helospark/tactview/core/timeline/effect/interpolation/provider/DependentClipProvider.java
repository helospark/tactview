package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;
import java.util.Optional;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.DesSerFactory;

public class DependentClipProvider extends KeyframeableEffect<String> {
    StepStringInterpolator stringInterpolator;

    public DependentClipProvider(StepStringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
    }

    public Optional<ReadOnlyClipImage> getValueAt(TimelinePosition position, Map<String, ReadOnlyClipImage> clips) {
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
    public KeyframeableEffect<String> deepClone() {
        return new DependentClipProvider(stringInterpolator.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<String>>> generateSerializableContent() {
        return DependentClipProviderFactory.class;
    }

}
