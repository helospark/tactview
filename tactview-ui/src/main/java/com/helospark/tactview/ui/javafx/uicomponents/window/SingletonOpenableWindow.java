package com.helospark.tactview.ui.javafx.uicomponents.window;

import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;

import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class SingletonOpenableWindow implements ScenePostProcessor {
    protected Stage stage;
    protected Scene parentScene;
    protected boolean isWindowOpen = false;

    public void open() {
        if (stage != null && stage.isShowing()) {
            throw new RuntimeException("Window already open");
        }
        stage = new Stage();
        stage.initOwner(parentScene.getWindow());
        stage.setScene(createScene());
        stage.setResizable(isResizable());
        stage.onCloseRequestProperty()
                .addListener(a -> this.close());
        stage.show();
        isWindowOpen = true;
    }

    protected abstract boolean isResizable();

    public void close() {
        stage.close();
        stage = null;
        isWindowOpen = false;
    }

    protected abstract Scene createScene();

    public abstract String getWindowId();

    @Override
    public void postProcess(Scene scene) {
        this.parentScene = scene;
    }
}
