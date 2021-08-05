package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.io.File;
import java.util.Map;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class FileProvider extends KeyframeableEffect<String> {
    String extension;
    StepStringInterpolator stringInterpolator;

    public FileProvider(String extension, StepStringInterpolator stringInterpolator) {
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
    public KeyframeableEffect<String> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new FileProvider(extension, stringInterpolator.deepClone(cloneRequestMetadata));
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<String>>> generateSerializableContent() {
        return FileProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return stringInterpolator instanceof KeyframeSupportingInterpolator;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) stringInterpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) stringInterpolator).isUsingKeyframes();
    }

    public String getValueOrDefault(TimelinePosition relativePosition, String defaultValue) {
        File result = getValueAt(relativePosition);
        if (result.exists()) {
            return result.getAbsolutePath();
        } else {
            return defaultValue;
        }
    }

}
