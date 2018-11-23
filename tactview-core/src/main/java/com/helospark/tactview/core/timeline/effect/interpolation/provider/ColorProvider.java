package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class ColorProvider extends KeyframeableEffect {
    private DoubleProvider redProvider;
    private DoubleProvider greenProvider;
    private DoubleProvider blueProvider;

    public ColorProvider(DoubleProvider redProvider, DoubleProvider greenProvider, DoubleProvider blueProvider) {
        this.redProvider = redProvider;
        this.greenProvider = greenProvider;
        this.blueProvider = blueProvider;
    }

    @Override
    public Color getValueAt(TimelinePosition position) {
        return new Color(redProvider.getValueAt(position), greenProvider.getValueAt(position), blueProvider.getValueAt(position));
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect> getChildren() {
        return Arrays.asList(redProvider, greenProvider, blueProvider);
    }

    @Override
    public KeyframeableEffect deepClone() {
        return new ColorProvider(redProvider.deepClone(), greenProvider.deepClone(), blueProvider.deepClone());
    }

}
