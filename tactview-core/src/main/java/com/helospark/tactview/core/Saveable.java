package com.helospark.tactview.core;

public interface Saveable {

    public default String saveId() {
        return this.getClass().getSimpleName();
    }

    public default String saveVersion() {
        return "v1";
    }

    public String generateSavedContent();

    public void loadContent(String data, String id, String version);

}
