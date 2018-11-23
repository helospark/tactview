package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class LineProvider extends KeyframeableEffect {
    private PointProvider startPointProvider;
    private PointProvider endPointProvider;

    public LineProvider(PointProvider startPointProvider, PointProvider endPointProvider) {
        this.startPointProvider = startPointProvider;
        this.endPointProvider = endPointProvider;
    }

    @Override
    public Line getValueAt(TimelinePosition position) {
        Point x = startPointProvider.getValueAt(position);
        Point y = endPointProvider.getValueAt(position);
        return new Line(x, y);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect> getChildren() {
        return Arrays.asList(startPointProvider, endPointProvider);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return startPointProvider.getSizeFunction();
    }

    @Override
    public KeyframeableEffect deepClone() {
        return new LineProvider(startPointProvider.deepClone(), endPointProvider.deepClone());
    }

}
