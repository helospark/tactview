package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class CurveProvider extends CompositeKeyframeableEffect implements KeyframeSupportingInterpolator {
    List<PointProvider> curvePoints;

    boolean isUsingKeyframes = false;

    double minX;
    double maxX;
    double minY;
    double maxY;

    public CurveProvider(double minX, double maxX, double minY, double maxY, List<PointProvider> curvePoints) {
        super((List<KeyframeableEffect>) (Object) curvePoints);
        this.curvePoints = curvePoints;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public KnotAwareUnivariateFunction getValueAt(TimelinePosition position) {
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < curvePoints.size(); ++i) {
            result.add(curvePoints.get(i).getValueAt(position));
        }

        result.sort((point1, point2) -> Double.compare(point1.x, point2.x));

        SplineInterpolator spi = new SplineInterpolator();

        double[] xs = new double[result.size()];
        double[] ys = new double[result.size()];

        for (int i = 0; i < result.size(); ++i) {
            xs[i] = result.get(i).x;
            ys[i] = result.get(i).y;
        }

        // TODO: Make xs unique

        PolynomialSplineFunction univariateFunction = spi.interpolate(xs, ys);
        return new KnotAwareUnivariateFunction(univariateFunction, univariateFunction.getKnots(), minY, maxY);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        try {
            KeyFrameInfo keyframeInfo = new ObjectMapper().readValue(value, KeyFrameInfo.class);

            if (keyframeInfo.newPoint) {
                PointProvider newPoint = PointProvider.of(keyframeInfo.x, keyframeInfo.y);
                if (keyframeInfo.index >= curvePoints.size()) {
                    curvePoints.add(newPoint);
                } else {
                    curvePoints.add(keyframeInfo.index, newPoint);
                }
            } else {
                PointProvider curvePoint = curvePoints.get(keyframeInfo.index);
                curvePoint.xProvider.keyframeAdded(globalTimelinePosition, keyframeInfo.x + "");
                curvePoint.yProvider.keyframeAdded(globalTimelinePosition, keyframeInfo.y + "");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        for (int i = 0; i < curvePoints.size(); ++i) {
            curvePoints.get(i).removeKeyframeAt(globalTimelinePosition);
        }
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return Collections.emptyMap();
    }

    @Override
    public CurveProvider deepClone() {
        List<PointProvider> clonePointProviders = new ArrayList<>();
        for (var element : curvePoints) {
            clonePointProviders.add(element.deepClone());
        }
        CurveProvider result = new CurveProvider(minX, maxX, minY, maxY, clonePointProviders);
        result.isUsingKeyframes = this.isUsingKeyframes;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return CurveProviderFactory.class;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public static class KeyFrameInfo {
        public boolean newPoint;
        public int index;
        public double x, y;

        public KeyFrameInfo() {

        }

        public KeyFrameInfo(boolean newPoint, int index, double x, double y) {
            this.newPoint = newPoint;
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public boolean isNewPoint() {
            return newPoint;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int getIndex() {
            return index;
        }

        public void setNewPoint(boolean newPoint) {
            this.newPoint = newPoint;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

    }

    public static class KnotAwareUnivariateFunction implements UnivariateFunction {
        UnivariateFunction function;
        double[] knots;
        double minY, maxY;

        public KnotAwareUnivariateFunction(UnivariateFunction function, double[] knots, double minY, double maxY) {
            this.function = function;
            this.knots = knots;
            this.minY = minY;
            this.maxY = maxY;
        }

        @Override
        public double value(double x) {
            double unclampedValue = getUnclampedValue(x);
            if (unclampedValue < minY) {
                return minY;
            } else if (unclampedValue > maxY) {
                return maxY;
            } else {
                return unclampedValue;
            }
        }

        private double getUnclampedValue(double x) {
            if (knots.length == 0) {
                return 0.0;
            } else if (x < knots[0]) {
                return function.value(knots[0]);
            } else if (x > knots[knots.length - 1]) {
                return function.value(knots[knots.length - 1]);
            } else {
                return function.value(x);
            }
        }

        public double[] getKnots() {
            return knots;
        }

    }

    @Override
    public boolean isUsingKeyframes() {
        return isUsingKeyframes;
    }

    public void setUsingKeyframes(boolean isUsingKeyframes) {
        this.isUsingKeyframes = isUsingKeyframes;
    }

    @Override
    public boolean supportsKeyframes() {
        return true;
    }

}
