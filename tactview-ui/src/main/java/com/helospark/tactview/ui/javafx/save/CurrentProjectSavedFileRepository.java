package com.helospark.tactview.ui.javafx.save;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;

@Component
public class CurrentProjectSavedFileRepository implements ResettableBean {
    private Optional<String> currentSavedFile = Optional.empty();

    public Optional<String> getCurrentSavedFile() {
        return currentSavedFile;
    }

    public void setCurrentSavedFile(String file) {
        currentSavedFile = Optional.ofNullable(file);
    }

    @Override
    public void resetDefaults() {
        currentSavedFile = Optional.empty();
    }

}
