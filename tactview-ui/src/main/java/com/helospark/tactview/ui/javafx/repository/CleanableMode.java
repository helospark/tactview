package com.helospark.tactview.ui.javafx.repository;

/**
 * Clean when escape is pressed.
 * @author helospark
 */
public interface CleanableMode {

    public void clean();

    public boolean isClean();

    public default int cleanPriority() {
        return 0;
    }

    public default boolean shouldConsumeCleanEvent() {
        return false;
    }

}
