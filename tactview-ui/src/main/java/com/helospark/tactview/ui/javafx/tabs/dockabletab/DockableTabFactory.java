package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

public interface DockableTabFactory {

    public DetachableTab createTab();

    public boolean doesSupport(String id);

    public String getId();

    public default OpenDetachableTabTarget getPreferredMode() {
        return OpenDetachableTabTarget.MAIN_WINDOW;
    }

    public default Optional<String> getPreferredNextTo() {
        return Optional.empty();
    }

    public default Optional<Point> getPreferredDefaultWindowSize() {
        return Optional.empty();
    }

}
