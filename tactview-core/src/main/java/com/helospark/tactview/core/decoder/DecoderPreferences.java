package com.helospark.tactview.core.decoder;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.preference.PreferenceValue;

@Component
public class DecoderPreferences {
    private boolean enableHardwareAcceleration = true;
    private boolean enableVideoPrefetch = true;

    @PreferenceValue(name = "Enable hardware acceleration", defaultValue = "true", group = "Performance")
    public void setEnableHardwareAcceleration(boolean enableHardwareAcceleration) {
        this.enableHardwareAcceleration = enableHardwareAcceleration;
    }

    @PreferenceValue(name = "Enable video prefetch", defaultValue = "true", group = "Performance")
    public void setEnableVideoPrefetch(boolean enableVideoPrefetch) {
        this.enableVideoPrefetch = enableVideoPrefetch;
    }

    public boolean isEnableHardwareAcceleration() {
        return enableHardwareAcceleration;
    }

    public boolean isEnableVideoPrefetch() {
        return enableVideoPrefetch;
    }

}
