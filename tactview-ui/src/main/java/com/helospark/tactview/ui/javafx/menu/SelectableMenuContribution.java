package com.helospark.tactview.ui.javafx.menu;

import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCodeCombination;

public interface SelectableMenuContribution extends MenuContribution {
    public void onAction(ActionEvent event);

    public default Optional<KeyCodeCombination> getAccelerator() {
        return Optional.empty();
    }

}
