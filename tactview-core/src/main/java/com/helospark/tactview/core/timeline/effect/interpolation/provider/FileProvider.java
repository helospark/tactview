package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.io.File;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;

public class FileProvider extends KeyframeableEffect {
    private String extension;
    private StringInterpolator stringInterpolator;

    public FileProvider(String extension, StringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
        this.extension = extension;
    }

    @Override
    public File getValueAt(TimelinePosition position) {
        return new File(stringInterpolator.valueAt(position));
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

    @Override
    public KeyframeableEffect deepClone() {
        return new FileProvider(extension, stringInterpolator.deepClone());
    }

    public String getExtension() {
        return extension;
    }

}
