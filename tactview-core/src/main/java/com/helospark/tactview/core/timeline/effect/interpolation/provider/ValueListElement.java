package com.helospark.tactview.core.timeline.effect.interpolation.provider;

public class ValueListElement {
    private String id;
    private String text;

    public ValueListElement(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

}
