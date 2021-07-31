package com.helospark.tactview.ui.javafx.key;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.hotkey.KeyDescriptor;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

@Component
public class KeyCombinationRepository {
    private Map<KeyCombination, GlobalShortcutHandler> combinations = new HashMap<>();
    private Map<KeyCode, GlobalFilterShortcutInfo> globalFilters = new HashMap<>();

    public void registerKeyCombination(KeyDescriptor combination, GlobalShortcutHandler handler) {
        combinations.put(combination.getCombination(), handler);
    }

    public Stream<Entry<KeyCombination, GlobalShortcutHandler>> getCombinations() {
        return combinations.entrySet().stream();
    }

    public void registerGlobalKeyFilters(KeyDescriptor combination, GlobalShortcutHandler handler, Set<Class<? extends Node>> disabledOnNodes) {
        globalFilters.put(combination.getCombination().getCode(), new GlobalFilterShortcutInfo(handler, disabledOnNodes)); // TODO: handle combination
    }

    public Map<KeyCode, GlobalFilterShortcutInfo> getGlobalFilters() {
        return globalFilters;
    }

    public static class GlobalFilterShortcutInfo {
        public GlobalShortcutHandler handler;
        public Set<Class<? extends Node>> disabledOnNodes = new HashSet<>(0);

        public GlobalFilterShortcutInfo(GlobalShortcutHandler handler, Set<Class<? extends Node>> disabledOnNodes) {
            this.handler = handler;
            this.disabledOnNodes = disabledOnNodes;
        }

    }

}
