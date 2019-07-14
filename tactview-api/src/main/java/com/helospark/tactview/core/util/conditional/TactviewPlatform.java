package com.helospark.tactview.core.util.conditional;

import org.apache.commons.lang3.SystemUtils;

public enum TactviewPlatform {
    LINUX(SystemUtils.IS_OS_LINUX),
    WINDOWS(SystemUtils.IS_OS_WINDOWS),
    MAC(SystemUtils.IS_OS_MAC);

    boolean active;

    private TactviewPlatform(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

}
