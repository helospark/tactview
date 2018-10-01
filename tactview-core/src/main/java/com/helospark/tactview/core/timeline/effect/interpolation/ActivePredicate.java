package com.helospark.tactview.core.timeline.effect.interpolation;

import java.util.Map;

public interface ActivePredicate {

    public boolean shouldActivate(Map<String, KeyframeableEffect> values);

}
