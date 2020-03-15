package com.helospark.tactview.ui.javafx.save;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.util.logger.Slf4j;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

@Component
public class ExitWithSaveService {
    private DirtyRepository dirtyRepository;
    private UiSaveHandler uiSaveHandler;
    private boolean showSaveDialog;
    @Slf4j
    private Logger logger;

    public ExitWithSaveService(DirtyRepository dirtyRepository, UiSaveHandler uiSaveHandler, @Value("${show.dialog.dirty-save}") boolean showSaveDialog) {
        this.dirtyRepository = dirtyRepository;
        this.uiSaveHandler = uiSaveHandler;
        this.showSaveDialog = showSaveDialog;
    }

    public void optionallySaveAndThenRun(Runnable exitRunnable) {
        if (!showSaveDialog) {
            exitRunnable.run();
        } else {
            boolean isDirty = dirtyRepository.isDirty();
            if (isDirty) {
                openSaveOrCancelDialogAndRun(exitRunnable);
            } else {
                exitRunnable.run();
            }
        }
    }

    private void openSaveOrCancelDialogAndRun(Runnable exitRunnable) {
        ButtonType saveAndExitButton = new ButtonType("save and close");
        ButtonType exitWithoutSaveButton = new ButtonType("close without save");
        ButtonType cancelButton = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add("stylesheet.css");
        alert.setHeaderText(null);
        alert.setContentText("You have unsaved changes. Should we save?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().setAll(saveAndExitButton, exitWithoutSaveButton, cancelButton);
        alert.setTitle("Save and exit");

        ButtonType result = alert.showAndWait().orElse(cancelButton);

        if (result == saveAndExitButton) {
            boolean saveSuccess = uiSaveHandler.save();
            if (saveSuccess) {
                exitRunnable.run();
            } else {
                logger.warn("Unable to save, refusing to exiting");
                return;
            }
        } else if (result == exitWithoutSaveButton) {
            exitRunnable.run();
        } else { // cancel
            return;
        }
    }

}
