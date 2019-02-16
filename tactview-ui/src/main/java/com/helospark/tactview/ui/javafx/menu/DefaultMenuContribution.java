package com.helospark.tactview.ui.javafx.menu;

import java.util.List;
import java.util.function.Consumer;

import javafx.event.ActionEvent;

public class DefaultMenuContribution implements MenuContribution {
    private List<String> path;
    private Consumer<ActionEvent> actionConsumer;

    public DefaultMenuContribution(List<String> path, Consumer<ActionEvent> actionConsumer) {
        this.path = path;
        this.actionConsumer = actionConsumer;
    }

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public void onAction(ActionEvent event) {
        actionConsumer.accept(event);
    }

}
