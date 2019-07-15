package com.helospark.tactview.core.timeline.valueprovidereffect;

import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.valueprovidereffect.impl.PivotPointValueProviderEffect;

public abstract class StatelessValueProviderEffect extends StatelessEffect {

    public StatelessValueProviderEffect(TimelineInterval interval) {
        super(interval);
    }

    public StatelessValueProviderEffect(PivotPointValueProviderEffect pivotPointValueProviderEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(pivotPointValueProviderEffect, cloneRequestMetadata);
    }

    public abstract List<String> expectedValues();

    public abstract List<EffectValue> getValuesAt(StatelessValueProviderRequest request);

}
