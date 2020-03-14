package com.helospark.tactview.ui.javafx.menu;

import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCodeCombination;

public interface MenuContribution {

    public List<String> getPath();

    public void onAction(ActionEvent event);

    public default Optional<KeyCodeCombination> getAccelerator() {
        return Optional.empty();
    }

}
