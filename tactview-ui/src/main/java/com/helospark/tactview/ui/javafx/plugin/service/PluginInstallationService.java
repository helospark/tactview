package com.helospark.tactview.ui.javafx.plugin.service;

import java.io.File;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.plugin.RestartDialogOpener;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.util.DialogHelper;

import javafx.scene.Scene;
import javafx.stage.FileChooser;

@Service
public class PluginInstallationService implements ScenePostProcessor {
    private PluginManager pluginManager;
    private RestartDialogOpener restartDialogOpener;
    private Scene scene;

    public PluginInstallationService(PluginManager pluginManager, RestartDialogOpener restartDialogOpener) {
        this.pluginManager = pluginManager;
        this.restartDialogOpener = restartDialogOpener;
    }

    public void installPlugin() {
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(scene.getWindow());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Plugin files", "*.zip"));

        if (file != null) {
            try {
                pluginManager.installPlugin(file);

                restartDialogOpener.confirmRestart("Plugin succesfully installed.");

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
