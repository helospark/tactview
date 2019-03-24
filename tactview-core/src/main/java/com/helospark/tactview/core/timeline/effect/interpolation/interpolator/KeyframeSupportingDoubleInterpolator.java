package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

public abstract class KeyframeSupportingDoubleInterpolator implements DoubleInterpolator, KeyframeSupportingInterpolator {
    private static final BigDecimal integralCacheResolution = BigDecimal.ONE;
    TreeMap<TimelinePosition, BigDecimal> integralCache;

    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        integralCache.clear();
        valueAddedInternal(globalTimelinePosition, value);
    }

    public void valueRemoved(TimelinePosition globalTimelinePosition) {
        integralCache.clear();
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
                }
            }
        }

        // TODO: check below

        Entry<TimelinePosition, BigDecimal> floorEntry = integralCache.floorEntry(from);

        BigDecimal resultArea;
        synchronized (this) { // TODO: Use concurrent TreeMap collection somehow
            TimelinePosition currentTime;
            if (floorEntry == null) {
                currentTime = TimelinePosition.ofZero();
                resultArea = BigDecimal.ZERO;
            } else {
                currentTime = floorEntry.getKey();
                resultArea = floorEntry.getValue();
            }

            TimelinePosition newTo = to.subtract(new TimelineLength(integralCacheResolution));
            while (currentTime.isLessThan(newTo)) {
                BigDecimal partialArea = DoubleInterpolator.super.integrate(currentTime, currentTime.add(integralCacheResolution));
                resultArea = resultArea.add(partialArea);
                currentTime.add(integralCacheResolution);
                integralCache.put(currentTime, resultArea);
            }
            resultArea = resultArea.add(DoubleInterpolator.super.integrate(currentTime, to));
        }
        return resultArea;
    }

}
