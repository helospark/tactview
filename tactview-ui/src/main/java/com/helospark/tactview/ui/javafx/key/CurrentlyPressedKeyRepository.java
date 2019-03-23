package com.helospark.tactview.ui.javafx.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Component
public class CurrentlyPressedKeyRepository implements ScenePostProcessor {
    private Set<KeyCode> pressedKeys = Collections.newSetFromMap(new ConcurrentHashMap<KeyCode, Boolean>());
    private List<Consumer<KeyCode>> keyCodeHandlers = new ArrayList<>();

    @Override
    public void postProcess(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode keycode = e.getCode();
            pressedKeys.add(keycode);
            keyCodeHandlers.stream()
                    .forEach(consumer -> consumer.accept(keycode));
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode keycode = e.getCode();
            pressedKeys.remove(keycode);
        });
    }

    public boolean isKeyDown(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void onKeyDown(Consumer<KeyCode> handler) {
        keyCodeHandlers.add(handler);
    }

}
