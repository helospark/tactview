package com.helospark.tactview.core.timeline.valueprovidereffect;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;

public abstract class StatelessValueProviderEffect extends StatelessEffect {

    public StatelessValueProviderEffect(TimelineInterval interval) {
        super(interval);
    }

    public abstract List<String> expectedValues();

    public abstract List<EffectValue> getValuesAt(StatelessValueProviderRequest request);

}
