package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class CompositeCommand implements UiCommand {
    private List<UiCommand> children;

    public CompositeCommand(UiCommand... children) {
        this.children = Arrays.asList(children);
    }

    public CompositeCommand(List<? extends UiCommand> commands) {
        this.children = (List<UiCommand>) commands;
    }

    @Override
    public void execute() {
        children.stream()
                .forEach(child -> child.execute());
    }

    @Override
    public void revert() {
        for (int i = children.size() - 1; i >= 0; --i) {
            children.get(i).revert();
        }
    }

    @Override
    public String toString() {
        return "CompositeCommand [children=" + children + "]";
    }

}
