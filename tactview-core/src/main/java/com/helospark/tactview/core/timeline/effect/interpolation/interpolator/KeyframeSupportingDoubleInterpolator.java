package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

public abstract class KeyframeSupportingDoubleInterpolator implements DoubleInterpolator, KeyframeSupportingInterpolator {
    private static final BigDecimal integralCacheResolution = new BigDecimal("0.1");
    TreeMap<TimelinePosition, BigDecimal> integralCache;

    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        if (integralCache != null) {
            integralCache.clear();
            integralCache.put(TimelinePosition.ofZero(), BigDecimal.ZERO);
        }
        valueAddedInternal(globalTimelinePosition, value);
    }

    public void valueRemoved(TimelinePosition globalTimelinePosition) {
        if (integralCache != null) {
            integralCache.clear();
            integralCache.put(TimelinePosition.ofZero(), BigDecimal.ZERO);
        }
        valueRemovedInternal(globalTimelinePosition);
    }

    public abstract void valueAddedInternal(TimelinePosition globalTimelinePosition, String value);

    public abstract void valueRemovedInternal(TimelinePosition globalTimelinePosition);

    public abstract Map<TimelinePosition, Object> getValues();

    public abstract void setDefaultValue(double defaultValue);

    @Override
    public BigDecimal integrate(TimelinePosition from, TimelinePosition to) {
        if (!isUsingKeyframes()) {
            BigDecimal constantValue = BigDecimal.valueOf(valueAt(from));
            BigDecimal toSeconds = to.getSeconds();
            BigDecimal fromSeconds = from.getSeconds();
            return toSeconds.subtract(fromSeconds).multiply(constantValue);
        }
        if (integralCache == null) {
            synchronized (this) {
                if (integralCache == null) {
                    integralCache = new TreeMap<>();
                    integralCache.put(TimelinePosition.ofZero(), BigDecimal.ZERO);
                }
            }
        }

        // TODO: check below

        Entry<TimelinePosition, BigDecimal> startFloorEntry = integralCache.floorEntry(from);
        Entry<TimelinePosition, BigDecimal> endFloorEntry = integralCache.floorEntry(to);

        BigDecimal resultArea;
        synchronized (this) { // TODO: Use concurrent TreeMap collection somehow
            TimelinePosition currentTime;

            if (startFloorEntry != null && endFloorEntry != null) {
                resultArea = endFloorEntry.getValue().subtract(startFloorEntry.getValue());
                currentTime = endFloorEntry.getKey();
            } else {
                currentTime = TimelinePosition.ofZero();
                resultArea = BigDecimal.ZERO;
            }

            TimelinePosition newTo = to.subtract(new TimelineLength(integralCacheResolution));
            while (currentTime.isLessThan(newTo)) {
                BigDecimal partialArea = DoubleInterpolator.super.integrate(currentTime, currentTime.add(integralCacheResolution));
                resultArea = resultArea.add(partialArea);
                currentTime = currentTime.add(integralCacheResolution);
                integralCache.put(currentTime, resultArea);
            }
            resultArea = resultArea.add(DoubleInterpolator.super.integrate(currentTime, to));
        }
        return resultArea;
    }

}
