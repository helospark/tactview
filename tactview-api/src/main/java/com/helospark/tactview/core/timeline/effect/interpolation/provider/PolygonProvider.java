package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.util.DesSerFactory;

public class PolygonProvider extends KeyframeableEffect<Polygon> {
    protected boolean useKeyframes;
    protected List<Point> defaultValues;
    protected TreeMap<TimelinePosition, List<Point>> values = new TreeMap<>();
    protected UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();

    public PolygonProvider(Map<TimelinePosition, List<Point>> points) {
        this.defaultValues = List.of();
        this.values.putAll(points);
    }

    public PolygonProvider(List<Point> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public PolygonProvider(boolean keyframesEnabled, List<Point> defaultValues, TreeMap<TimelinePosition, List<Point>> values, UnivariateInterpolator interpolatorImplementation) {
        this.useKeyframes = keyframesEnabled;
        this.defaultValues = defaultValues;
        this.values = values;
        this.interpolatorImplementation = interpolatorImplementation;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<Polygon>>> generateSerializableContent() {
        return PolygonProviderDesSerFactory.class;
    }

    @Override
    public Polygon getValueAt(TimelinePosition position) {
        Entry<TimelinePosition, List<Point>> lastEntry = values.lastEntry();
        Entry<TimelinePosition, List<Point>> firstEntry = values.firstEntry();
        if (values.isEmpty() || !useKeyframes) {
            return new Polygon(defaultValues);
        } else if (values.size() == 1) {
            return new Polygon(values.firstEntry().getValue());
        } else if (position.isGreaterThan(lastEntry.getKey())) {
            return new Polygon(lastEntry.getValue());
        } else if (position.isLessThan(firstEntry.getKey())) {
            return new Polygon(firstEntry.getValue());
        } else {
            Entry<TimelinePosition, List<Point>> previousEntry = values.floorEntry(position);
            Entry<TimelinePosition, List<Point>> nextEntry = values.ceilingEntry(position);

            if (previousEntry.getKey().getSeconds().doubleValue() >= nextEntry.getKey().getSeconds().doubleValue()) {
                return new Polygon(previousEntry.getValue());
            }

            return doInterpolate(previousEntry, nextEntry, position);
        }
    }

    private Polygon doInterpolate(Entry<TimelinePosition, List<Point>> previousEntry, Entry<TimelinePosition, List<Point>> nextEntry, TimelinePosition currentPosition) {
        List<Point> lastPoints = previousEntry.getValue();
        List<Point> nextPoints = nextEntry.getValue();
        if (lastPoints.size() != nextPoints.size()) {
            return new Polygon(lastPoints); // we cannot interpolate a polygon with changing sides
        } else {
            List<Point> newPoints = new ArrayList<>();

            for (int i = 0; i < lastPoints.size(); ++i) {
                Point point1 = lastPoints.get(i);
                Point point2 = nextPoints.get(i);

                double xCoordinate = interpolateAxis(previousEntry, nextEntry, currentPosition, new double[]{point1.x, point2.x});
                double yCoordinate = interpolateAxis(previousEntry, nextEntry, currentPosition, new double[]{point1.y, point2.y});

                newPoints.add(new Point(xCoordinate, yCoordinate));
            }
            return new Polygon(newPoints);
        }
    }

    private double interpolateAxis(Entry<TimelinePosition, List<Point>> lastEntry, Entry<TimelinePosition, List<Point>> nextEntry, TimelinePosition position, double[] yVals) {
        double[] timeVals = new double[]{lastEntry.getKey().getSeconds().doubleValue(), nextEntry.getKey().getSeconds().doubleValue()};
        return interpolatorImplementation.interpolate(timeVals, yVals).value(position.getSeconds().doubleValue());
    }

    @Override
    public boolean isPrimitive() {
        return true; // because this is dynamically generated
    }

    @Override
    public KeyframeableEffect<Polygon> deepClone() {
        return new PolygonProvider(useKeyframes, new ArrayList<>(defaultValues), new TreeMap<>(values), interpolatorImplementation);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, Polygon value) {
        System.out.println("Adding polygon keyframe " + globalTimelinePosition + " " + value);
        if (useKeyframes) {
            values.put(globalTimelinePosition, value.getPoints());
        } else {
            defaultValues = value.getPoints();
        }
    }

    @Override
    public SizeFunction getSizeFunction() {
        return SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;
    }

    @Override
    public boolean supportsKeyframes() {
        return true;
    }

    @Override
    public boolean keyframesEnabled() {
        return useKeyframes;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        this.useKeyframes = useKeyframes;
    }
}
