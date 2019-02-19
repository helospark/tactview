package com.helospark.tactview.ui.javafx.key;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Component
public class CurrentlyPressedKeyRepository implements ScenePostProcessor {
    private Set<KeyCode> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<KeyCode, Boolean>());

    @Override
    public void postProcess(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode keycode = e.getCode();
            pressedKeys.add(keycode);
            System.out.println("KEY PRESSED = " + keycode);
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode keycode = e.getCode();
            pressedKeys.remove(keycode);
            System.out.println("KEY RELEASED = " + keycode);
        });
    }

    public boolean isKeyDown(KeyCode keyCode) {
        System.out.println("Querying " + keyCode + " pressed keys " + pressedKeys);
        return pressedKeys.contains(keyCode);
    }

}
