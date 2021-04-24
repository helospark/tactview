package com.helospark.tactview.ui.javafx.repository.selection;

public class ClipSelectionChangedMessage {
    private String clip;
    private ChangeType type;

    public ClipSelectionChangedMessage(String item, ChangeType type) {
        this.clip = item;
        this.type = type;
    }

    public String getClip() {
        return clip;
    }

    public ChangeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ClipSelectionChangedMessage [clip=" + clip + ", type=" + type + "]";
    }

}
