package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class LineProvider extends KeyframeableEffect {
    private PointProvider startPointProvider;
    private PointProvider endPointProvider;

    @Override
    public Line getValueAt(TimelinePosition position) {
        Point x = startPointProvider.getValueAt(position);
        Point y = endPointProvider.getValueAt(position);
        return new Line(x, y);
    }

}
