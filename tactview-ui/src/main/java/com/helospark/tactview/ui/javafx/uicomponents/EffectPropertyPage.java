package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.layout.GridPane;

public class EffectPropertyPage {
    private String componentId;
    private GridPane box;
    private List<Consumer<TimelinePosition>> updateFunctions;

    @Generated("SparkTools")
    private EffectPropertyPage(Builder builder) {
        this.box = builder.box;
        this.updateFunctions = builder.updateFunctions;
        this.componentId = builder.componentId;
    }

    public EffectPropertyPage(GridPane box, List<Consumer<TimelinePosition>> updateFunctions) {
        this.box = box;
        this.updateFunctions = updateFunctions;
    }

    public GridPane getBox() {
        return box;
    }

    public List<Consumer<TimelinePosition>> getUpdateFunctions() {
        return updateFunctions;
    }

    public String getComponentId() {
        return componentId;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String componentId;
        private GridPane box;
        private List<Consumer<TimelinePosition>> updateFunctions = new ArrayList<>();

        private Builder() {
        }

        public Builder withBox(GridPane box) {
            this.box = box;
            return this;
        }

        public GridPane getBox() {
            return box;
        }

        public Builder withComponentId(String componentId) {
            this.componentId = componentId;
            return this;
        }

        public Builder addUpdateFunctions(Consumer<TimelinePosition> updateFunction) {
            this.updateFunctions.add(updateFunction);
            return this;
        }

        public EffectPropertyPage build() {
            return new EffectPropertyPage(this);
        }
    }

}
