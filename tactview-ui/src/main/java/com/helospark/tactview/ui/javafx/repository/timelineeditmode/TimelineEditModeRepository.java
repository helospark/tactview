package com.helospark.tactview.ui.javafx.repository.timelineeditmode;

import com.helospark.lightdi.annotation.Component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@Component
public class TimelineEditModeRepository {
    private ObjectProperty<TimelineEditMode> mode = new SimpleObjectProperty<>(TimelineEditMode.NORMAL);
    private BooleanProperty magnetEditMode = new SimpleBooleanProperty(true);

    public ObjectProperty<TimelineEditMode> getModeProperty() {
        return mode;
    }

    public TimelineEditMode getMode() {
        return mode.get();
    }

    public void setMode(TimelineEditMode mode) {
        this.mode.set(mode);
    }

    public boolean isRippleDeleteEnabled() {
        return mode.get().equals(TimelineEditMode.ALL_CHANNEL_RIPPLE) || mode.get().equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE);
    }

    public boolean isMagnetEditModeEnabled(boolean invertSpecialPoints) {
        boolean result = magnetEditMode.get();
        if (invertSpecialPoints) {
            result = !result;
        }
        return result;
    }

    public void enableMagnetEditMode(boolean enable) {
        this.magnetEditMode.set(enable);
    }

    public BooleanProperty getMagnetEditMode() {
        return magnetEditMode;
    }
}
