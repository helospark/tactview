package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

public abstract class CompositeKeyframeableEffect<T> extends KeyframeableEffect<T> {
    private List<KeyframeableEffect> compositeElements;

    public CompositeKeyframeableEffect(List<KeyframeableEffect> compositeElements) {
        this.compositeElements = compositeElements;
    }

    @Override
    public boolean supportsKeyframes() {
        return compositeElements.stream()
                .anyMatch(a -> a.supportsKeyframes());
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        List<KeyframeableEffect<?>> children = getChildren();
        for (int i = 0; i < children.size(); ++i) {
            children.get(i).removeKeyframeAt(globalTimelinePosition);
        }
    }

    @Override
    public boolean keyframesEnabled() {
        boolean result = false;

        for (var element : compositeElements) {
            if (element.supportsKeyframes()) {
                result |= element.keyframesEnabled();
            }
        }

        return result;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        for (var element : compositeElements) {
            if (element.supportsKeyframes()) {
                element.setUseKeyframes(useKeyframes);
            }
        }
    }

}
