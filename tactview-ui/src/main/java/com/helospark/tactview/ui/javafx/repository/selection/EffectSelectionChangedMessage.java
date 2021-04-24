package com.helospark.tactview.ui.javafx.repository.selection;

public class EffectSelectionChangedMessage {
    private String effect;
    private ChangeType type;

    public EffectSelectionChangedMessage(String item, ChangeType type) {
        this.effect = item;
        this.type = type;
    }

    public String getEffect() {
        return effect;
    }

    public ChangeType getType() {
        return type;
    }

}
