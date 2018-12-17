package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.util.DesSerFactory;

public class PolygonProvider extends CompositeKeyframeableEffect {
    private List<Point> points;

    public PolygonProvider(List<Point> points) {
        super(List.of());
        this.points = points;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Polygon getValueAt(TimelinePosition position) {
        //        List<Point> pointsInPolygon = new ArrayList<>();
        //        for (var element : points) {
        //            pointsInPolygon.add(element.getValueAt(position));
        //        }
        return new Polygon(points);
    }

    @Override
    public boolean isPrimitive() {
        return true; // because this is dynamically generated
    }

    @Override
    public KeyframeableEffect deepClone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        points = Polygon.deserializePointsFromString(value); // TODO: we need providers
    }

    @Override
    public SizeFunction getSizeFunction() {
        return SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;
    }
}
