package com.helospark.tactview.ui.javafx.tabs;

public class TabActiveRequest {
    private String editorId;

    public TabActiveRequest(String curveEditorId) {
        this.editorId = curveEditorId;
    }

    public String getEditorId() {
        return editorId;
    }

}
