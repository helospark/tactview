package com.helospark.tactview.core.timeline.effect.interpolation;

import java.util.UUID;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public abstract class KeyframeableEffect {
    private String id = UUID.randomUUID().toString();
    protected boolean scaleDependent;
    public abstract Object getValueAt(TimelinePosition position);

    public String getId() {
        return id;
    }

    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {

    }

    public void interpolatorChanged(EffectInterpolator newInterpolator) {

    }

    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {

    }

    public boolean isScaleDependent() {
        return scaleDependent;
    }

}
