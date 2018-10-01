package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class PointProvider extends KeyframeableEffect {
    private DoubleProvider xProvider;
    private DoubleProvider yProvider;

    @Override
    public Point getValueAt(TimelinePosition position) {
        double x = xProvider.getValueAt(position);
        double y = yProvider.getValueAt(position);
        return new Point(x, y);
    }

}
