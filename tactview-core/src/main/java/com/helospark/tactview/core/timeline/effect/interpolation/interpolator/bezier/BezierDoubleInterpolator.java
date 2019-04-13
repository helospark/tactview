package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.bezier.CubicBezierPoint;

public class BezierDoubleInterpolator extends KeyframeSupportingDoubleInterpolator {
    protected TreeMap<TimelinePosition, CubicBezierPoint> values;
    protected double defaultValue;
    protected boolean useKeyframes;

    public BezierDoubleInterpolator(Double singleDefaultValue) {
        this.values = new TreeMap<>();
        this.defaultValue = singleDefaultValue;
    }

    public BezierDoubleInterpolator(TreeMap<TimelinePosition, CubicBezierPoint> values) {
        this.values = new TreeMap<>(values);
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        if (values.isEmpty() || !useKeyframes) {
            return defaultValue;
        } else {
            SortedMap<TimelinePosition, CubicBezierPoint> headMap = values.headMap(position);
            SortedMap<TimelinePosition, CubicBezierPoint> tailMap = values.tailMap(position);

            TimelinePosition lowKey = headMap.isEmpty() ? null : headMap.lastKey();
            TimelinePosition highKey = tailMap.isEmpty() ? null : tailMap.firstKey();

            CubicBezierPoint headElement = lowKey == null ? null : values.get(lowKey);
            CubicBezierPoint tailElement = highKey == null ? null : values.get(highKey);

            if (headElement == null) {
                return tailElement.value;
            } else if (tailElement == null) {
                return headElement.value;
            } else {
                return doInterpolate(position, lowKey, highKey, headElement, tailElement);
            }
        }
    }

    private Double doInterpolate(TimelinePosition position, TimelinePosition lowKey, TimelinePosition highKey, CubicBezierPoint lowElement, CubicBezierPoint highElement) {
        double lowTime = lowKey.getSeconds().doubleValue();
        double highTime = highKey.getSeconds().doubleValue();
        double currentTime = position.getSeconds().doubleValue();

        double normalizedValue = (currentTime - lowTime) / (highTime - lowTime);

        return cubicBezier(lowKey, highKey, lowElement, highElement, normalizedValue);
    }

    // https://stackoverflow.com/questions/37642168/how-to-convert-quadratic-bezier-curve-code-into-cubic-bezier-curve/37642695#37642695
    private double cubicBezier(TimelinePosition lowKey, TimelinePosition highKey, CubicBezierPoint lowElement, CubicBezierPoint highElement, double percent) {
        Point startPoint = new Point(lowKey.getSeconds().doubleValue(), lowElement.value);
        Point endPoint = new Point(highKey.getSeconds().doubleValue(), highElement.value);
        Point controlPoint = startPoint.add(lowElement.controlPointOut);
        Point secondControlPoint = endPoint.add(highElement.controlPointIn);

        double x1 = startPoint.x;
        double y1 = startPoint.y;
        double x2 = controlPoint.x;
        double y2 = controlPoint.y;
        double x3 = secondControlPoint.x;
        double y3 = secondControlPoint.y;
        double x4 = endPoint.x;
        double y4 = endPoint.y;

        double i = percent;

        // The Green Lines
        double xa = getPt(x1, x2, i);
        double ya = getPt(y1, y2, i);
        double xb = getPt(x2, x3, i);
        double yb = getPt(y2, y3, i);
        double xc = getPt(x3, x4, i);
        double yc = getPt(y3, y4, i);

        // The Blue Line
        double xm = getPt(xa, xb, i);
        double ym = getPt(ya, yb, i);
        double xn = getPt(xb, xc, i);
        double yn = getPt(yb, yc, i);

        // The Black Dot
        double x = getPt(xm, xn, i);
        double y = getPt(ym, yn, i);

        System.out.println("CurveY: " + y);

        return y;
    }

    private double getPt(double n1, double n2, double perc) {
        double diff = n2 - n1;

        return n1 + (diff * perc);
    }

    @Override
    public BezierDoubleInterpolator deepClone() {
        BezierDoubleInterpolator result = new BezierDoubleInterpolator(values);
        result.defaultValue = defaultValue;
        result.useKeyframes = useKeyframes;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return BezierDoubleInterpolatorInterpolatorFactory.class;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        this.useKeyframes = useKeyframes;
    }

    @Override
    public boolean isUsingKeyframes() {
        return useKeyframes;
    }

    @Override
    public void setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void valueAddedInternal(TimelinePosition globalTimelinePosition, String value) {
        Double valueToSet = Double.valueOf(value);
        if (!useKeyframes) {
            defaultValue = valueToSet;
        } else {
            values.put(globalTimelinePosition, new CubicBezierPoint(valueToSet, new Point(-1.0, 0.0), new Point(1.0, 0.0)));
        }
    }

    @Override
    public void valueRemovedInternal(TimelinePosition globalTimelinePosition) {
        values.remove(globalTimelinePosition);
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        TreeMap<TimelinePosition, Object> result = new TreeMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue().value);
        }
        return result;
    }

}
