package com.helospark.tactview.ui.javafx.repository.selection;

import javafx.scene.Node;

public class EffectSelectionChangedMessage {
    private Node effect;
    private ChangeType type;

    public EffectSelectionChangedMessage(Node item, ChangeType type) {
        this.effect = item;
        this.type = type;
    }

    public Node getEffect() {
        return effect;
    }

    public ChangeType getType() {
        return type;
    }

}
