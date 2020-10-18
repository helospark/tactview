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
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonPoint;
import com.helospark.tactview.core.util.DesSerFactory;

// TODO: Too many copypaste from PolygonProvider
public class BezierPolygonProvider extends KeyframeableEffect<List<BezierPolygonPoint>> {
    protected boolean useKeyframes;
    protected List<BezierPolygonPoint> defaultValues;
    protected TreeMap<TimelinePosition, List<BezierPolygonPoint>> values = new TreeMap<>();
    protected UnivariateInterpolator interpolatorImplementation = new LinearInterpolator();

    public BezierPolygonProvider(Map<TimelinePosition, List<BezierPolygonPoint>> points) {
        this.defaultValues = List.of();
        this.values.putAll(points);
    }

    public BezierPolygonProvider(List<BezierPolygonPoint> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public BezierPolygonProvider(boolean keyframesEnabled, List<BezierPolygonPoint> defaultValues, TreeMap<TimelinePosition, List<BezierPolygonPoint>> values,
            UnivariateInterpolator interpolatorImplementation) {
        this.useKeyframes = keyframesEnabled;
        this.defaultValues = defaultValues;
        this.values = values;
        this.interpolatorImplementation = interpolatorImplementation;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<List<BezierPolygonPoint>>>> generateSerializableContent() {
        return BezierPolygonProviderDesSerFactory.class;
    }

    @Override
    public BezierPolygon getValueAt(TimelinePosition position) {
        Entry<TimelinePosition, List<BezierPolygonPoint>> lastEntry = values.lastEntry();
        Entry<TimelinePosition, List<BezierPolygonPoint>> firstEntry = values.firstEntry();
        if (values.isEmpty() || !useKeyframes) {
            return new BezierPolygon(defaultValues);
        } else if (values.size() == 1) {
            return new BezierPolygon(values.firstEntry().getValue());
        } else if (position.isGreaterThan(lastEntry.getKey())) {
            return new BezierPolygon(lastEntry.getValue());
        } else if (position.isLessThan(firstEntry.getKey())) {
            return new BezierPolygon(firstEntry.getValue());
        } else {
            Entry<TimelinePosition, List<BezierPolygonPoint>> previousEntry = values.floorEntry(position);
            Entry<TimelinePosition, List<BezierPolygonPoint>> nextEntry = values.ceilingEntry(position);

            if (previousEntry.getKey().getSeconds().doubleValue() >= nextEntry.getKey().getSeconds().doubleValue()) {
                return new BezierPolygon(previousEntry.getValue());
            }

            return doInterpolate(previousEntry, nextEntry, position);
        }
    }

    private BezierPolygon doInterpolate(Entry<TimelinePosition, List<BezierPolygonPoint>> previousEntry, Entry<TimelinePosition, List<BezierPolygonPoint>> nextEntry,
            TimelinePosition currentPosition) {
        List<BezierPolygonPoint> lastPoints = previousEntry.getValue();
        List<BezierPolygonPoint> nextPoints = nextEntry.getValue();
        if (lastPoints.size() != nextPoints.size()) {
            return new BezierPolygon(lastPoints); // we cannot interpolate a polygon with changing sides
        } else {
            List<BezierPolygonPoint> newPoints = new ArrayList<>();

            for (int i = 0; i < lastPoints.size(); ++i) {
                BezierPolygonPoint point1 = lastPoints.get(i);
                BezierPolygonPoint point2 = nextPoints.get(i);

                double xCoordinate = interpolateAxis(previousEntry, nextEntry, currentPosition, new double[]{point1.getX(), point2.getX()});
                double yCoordinate = interpolateAxis(previousEntry, nextEntry, currentPosition, new double[]{point1.getY(), point2.getY()});

                newPoints.add(new BezierPolygonPoint(xCoordinate, yCoordinate, point1.getType()));
            }
            return new BezierPolygon(newPoints);
        }
    }

    private double interpolateAxis(Entry<TimelinePosition, List<BezierPolygonPoint>> lastEntry, Entry<TimelinePosition, List<BezierPolygonPoint>> nextEntry, TimelinePosition position,
            double[] yVals) {
        double[] timeVals = new double[]{lastEntry.getKey().getSeconds().doubleValue(), nextEntry.getKey().getSeconds().doubleValue()};
        return interpolatorImplementation.interpolate(timeVals, yVals).value(position.getSeconds().doubleValue());
    }

    @Override
    public boolean isPrimitive() {
        return true; // because this is dynamically generated
    }

    @Override
    public KeyframeableEffect<List<BezierPolygonPoint>> deepClone() {
        return new BezierPolygonProvider(useKeyframes, new ArrayList<>(defaultValues), new TreeMap<>(values), interpolatorImplementation);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, List<BezierPolygonPoint> value) {
        List<BezierPolygonPoint> newPoints = value;
        if (useKeyframes) {
            values.put(globalTimelinePosition, newPoints);
        } else {
            defaultValues = newPoints;
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
