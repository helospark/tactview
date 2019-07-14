package com.helospark.tactview.ui.javafx.plugin;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.util.DialogHelper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

@Component
@Order(4900)
public class PluginInstallationMenuItemContributor implements MenuContribution, ScenePostProcessor {
    private Scene scene;

    private PluginManager pluginManager;
    private ExitWithSaveService exitWithSaveService;

    public PluginInstallationMenuItemContributor(PluginManager pluginManager, ExitWithSaveService exitWithSaveService) {
        this.pluginManager = pluginManager;
        this.exitWithSaveService = exitWithSaveService;
    }

    @Override
    public List<String> getPath() {
        return List.of("_Help", "Install plugin");
    }

    @Override
    public void onAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(scene.getWindow());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Plugin files", "*.zip"));

        if (file != null) {
            try {
                pluginManager.installPlugin(file);

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Restart");
                alert.setHeaderText("Plugin succesfully installed.");
                alert.setContentText("You have to restart TactView for the changes to take place. Restart now?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    exitWithSaveService.optionallySaveAndThenRun(() -> {
                        Platform.exit();
                        int restartStatusCode = 3;
                        System.exit(restartStatusCode);
                    });
                }

            } catch (Exception e) {
                DialogHelper.showExceptionDialog("Cannot install plugin", "Cannot install plugin", e);
            }
        }
    }

    @Override
    public void postProcess(Scene scene) {
        this.scene = scene;
    }

}
