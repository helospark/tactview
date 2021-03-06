package com.helospark.tactview.core.timeline.effect.interpolation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.StatefulCloneable;

public abstract class KeyframeableEffect<T> implements StatefulCloneable<KeyframeableEffect>, SavedContentAddable<KeyframeableEffect> {
    String id = UUID.randomUUID().toString();
    boolean scaleDependent;

    public abstract Object getValueAt(TimelinePosition position);

    public String getId() {
        return id;
    }

    public void keyframeAdded(TimelinePosition globalTimelinePosition, T value) {

    }

    public void interpolatorChanged(EffectInterpolator newInterpolator) {

    }

    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {

    }

    public abstract boolean isPrimitive();

    public List<KeyframeableEffect<?>> getChildren() {
        return Collections.emptyList();
    }

    public boolean isScaleDependent() {
        return scaleDependent;
    }

    public void setScaleDependent() {
        scaleDependent = true;
    }

    public SizeFunction getSizeFunction() {
        return SizeFunction.NO_TRANSFORMATION;
    }

    public Map<TimelinePosition, Object> getValues() {
        return Collections.emptyMap();
    }

    public boolean isKeyframe(TimelinePosition position) {
        return getValues().containsKey(position);
    }

    public EffectInterpolator getInterpolatorClone() {
        EffectInterpolator interpolator = getInterpolator();
        if (interpolator != null) {
            return interpolator.deepClone();
        } else {
            return null;
        }
    }

    public EffectInterpolator getInterpolator() {
        return null;
    }

    public void setInterpolator(Object previousInterpolator) {

    }

    @Override
    public abstract KeyframeableEffect deepClone();

    public boolean supportsKeyframes() {
        return false;
    }

    public void setUseKeyframes(boolean useKeyframes) {

    }

    public boolean keyframesEnabled() {
        return false;
    }

    @Override
    public String toString() {
        return "KeyframeableEffect [id=" + id + ", scaleDependent=" + scaleDependent + ", getClass()=" + getClass() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyframeableEffect other = (KeyframeableEffect) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
