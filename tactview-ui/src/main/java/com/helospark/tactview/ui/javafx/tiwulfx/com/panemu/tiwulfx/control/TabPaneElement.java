package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LeafElement.class, name = "leaf"),
        @JsonSubTypes.Type(value = SplitPaneElement.class, name = "splitPane")
})
public abstract class TabPaneElement {
    private String type;

    public TabPaneElement(@JsonProperty("type") String type) {
        this.type = type;
    }

}
