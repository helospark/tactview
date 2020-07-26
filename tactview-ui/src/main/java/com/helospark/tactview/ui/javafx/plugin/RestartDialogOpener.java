package com.helospark.tactview.ui.javafx.plugin;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

@Component
public class RestartDialogOpener {
    private ExitWithSaveService exitWithSaveService;
    private AlertDialogFactory alertDialogFactory;

    public RestartDialogOpener(ExitWithSaveService exitWithSaveService, AlertDialogFactory alertDialogFactory) {
        this.exitWithSaveService = exitWithSaveService;
        this.alertDialogFactory = alertDialogFactory;
    }

    public void confirmRestart(String message) {
        Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.CONFIRMATION, "Restart", "You have to restart TactView for the changes to take place. Restart now?");
        alert.setHeaderText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            exitWithSaveService.optionallySaveAndThenRun(() -> {
                Platform.exit();
                int restartStatusCode = 3;
                System.exit(restartStatusCode);
            });
        }
    }

}
