package com.helospark.tactview.ui.javafx.menu;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCodeCombination;

public class DefaultMenuContribution implements MenuContribution {
    private List<String> path;
    private Consumer<ActionEvent> actionConsumer;
    private Optional<KeyCodeCombination> keyCombination = Optional.empty();

    public DefaultMenuContribution(List<String> path, Consumer<ActionEvent> actionConsumer) {
        this.path = path;
        this.actionConsumer = actionConsumer;
    }

    public DefaultMenuContribution(List<String> path, Consumer<ActionEvent> actionConsumer, KeyCodeCombination keyCombination) {
        this.path = path;
        this.actionConsumer = actionConsumer;
        this.keyCombination = Optional.of(keyCombination);
    }

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public void onAction(ActionEvent event) {
        actionConsumer.accept(event);
    }

    @Override
    public Optional<KeyCodeCombination> getAccelerator() {
        return keyCombination;
    }

}
