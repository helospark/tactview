package com.helospark.tactview.ui.javafx.plugin;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

@Component
public class RestartDialogOpener {
    private ExitWithSaveService exitWithSaveService;

    public RestartDialogOpener(ExitWithSaveService exitWithSaveService) {
        this.exitWithSaveService = exitWithSaveService;
    }

    public void confirmRestart(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Restart");
        alert.setHeaderText(message);
        alert.setContentText("You have to restart TactView for the changes to take place. Restart now?");

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
