package com.helospark.tactview.ui.javafx.commands;

public interface UiCommand {

    public void execute();

    public void revert();

    public default void redo() {
        execute();
    }

    public default boolean isRevertable() {
        return true;
    }

    public default void preDestroy() {

    }
}
