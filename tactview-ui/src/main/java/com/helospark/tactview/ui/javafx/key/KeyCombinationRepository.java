package com.helospark.tactview.ui.javafx.key;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.helospark.lightdi.annotation.Component;

import javafx.scene.input.KeyCombination;

@Component
public class KeyCombinationRepository {
    private Map<KeyCombination, GlobalShortcutHandler> combinations = new HashMap<>();

    public void registerKeyCombination(KeyCombination combination, GlobalShortcutHandler handler) {
        combinations.put(combination, handler);
    }

    public Stream<Entry<KeyCombination, GlobalShortcutHandler>> getCombinations() {
        return combinations.entrySet().stream();
    }

}
