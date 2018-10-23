package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class PointProvider extends KeyframeableEffect {
    private DoubleProvider xProvider;
    private DoubleProvider yProvider;

    public PointProvider(DoubleProvider xProvider, DoubleProvider yProvider) {
        this.xProvider = xProvider;
        this.yProvider = yProvider;
    }

    @Override
    public Point getValueAt(TimelinePosition position) {
        double x = xProvider.getValueAt(position);
        double y = yProvider.getValueAt(position);
        return new Point(x, y);
    }

    public DoubleProvider getxProvider() {
        return xProvider;
    }

    public DoubleProvider getyProvider() {
        return yProvider;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect> getChildren() {
        return Arrays.asList(xProvider, yProvider);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return xProvider.getSizeFunction();
    }

    @Override
    public boolean hasKeyframes() {
        return false;
    }
}
