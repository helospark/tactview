package com.helospark.tactview.ui.javafx.save;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;

@Component
public class CurrentProjectSavedFileRepository {
    private Optional<String> currentSavedFile = Optional.empty();

    public Optional<String> getCurrentSavedFile() {
        return currentSavedFile;
    }

    public void setCurrentSavedFile(String file) {
        currentSavedFile = Optional.ofNullable(file);
    }

}
