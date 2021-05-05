package com.helospark.tactview.ui.javafx.save;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

@Component
public class ExitWithSaveService {
    private DirtyRepository dirtyRepository;
    private UiSaveHandler uiSaveHandler;
    private AlertDialogFactory alertDialogFactory;
    private boolean showSaveDialog;
    @Slf4j
    private Logger logger;

    public ExitWithSaveService(DirtyRepository dirtyRepository, UiSaveHandler uiSaveHandler, @Value("${show.dialog.dirty-save}") boolean showSaveDialog,
            AlertDialogFactory alertDialogFactory) {
        this.dirtyRepository = dirtyRepository;
        this.uiSaveHandler = uiSaveHandler;
        this.showSaveDialog = showSaveDialog;
        this.alertDialogFactory = alertDialogFactory;
    }

    public boolean optionallySaveAndThenRun(Runnable exitRunnable) {
        if (!showSaveDialog) {
            exitRunnable.run();
            return true;
        } else {
            boolean isDirty = dirtyRepository.isDirty();
            if (isDirty) {
                return openSaveOrCancelDialogAndRun(exitRunnable);
            } else {
                exitRunnable.run();
                return true;
            }
        }
    }

    private boolean openSaveOrCancelDialogAndRun(Runnable exitRunnable) {
        ButtonType saveAndExitButton = new ButtonType("save and close");
        ButtonType exitWithoutSaveButton = new ButtonType("close without save");
        ButtonType cancelButton = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.WARNING, "Save and exit", "You have unsaved changes. Should we save?");
        alert.getButtonTypes().setAll(saveAndExitButton, exitWithoutSaveButton, cancelButton);

        ButtonType result = alert.showAndWait().orElse(cancelButton);

        if (result == saveAndExitButton) {
            boolean saveSuccess = uiSaveHandler.save();
            if (saveSuccess) {
                exitRunnable.run();
                return true;
            } else {
                logger.warn("Unable to save, refusing to exiting");
                return false;
            }
        } else if (result == exitWithoutSaveButton) {
            exitRunnable.run();
            return true;
        } else { // cancel
            return false;
        }
    }

}
