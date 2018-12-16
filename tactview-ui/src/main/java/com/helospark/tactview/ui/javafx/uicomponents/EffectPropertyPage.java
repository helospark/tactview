package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.layout.GridPane;

public class EffectPropertyPage {
    private String componentId;
    private GridPane box;
    private List<Consumer<TimelinePosition>> updateFunctions;
    private Map<String, Consumer<Boolean>> keyframeEnabledConsumer;

    @Generated("SparkTools")
    private EffectPropertyPage(Builder builder) {
        this.box = builder.box;
        this.updateFunctions = builder.updateFunctions;
        this.componentId = builder.componentId;
        this.keyframeEnabledConsumer = builder.keyframeEnabledConsumer;
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

    public Map<String, Consumer<Boolean>> getKeyframeEnabledConsumer() {
        return keyframeEnabledConsumer;
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
        private Map<String, Consumer<Boolean>> keyframeEnabledConsumer = new HashMap<>();

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

        public Builder addKeyframeEnabledConsumer(String id, Consumer<Boolean> keyframeEnabledConsumer) {
            this.keyframeEnabledConsumer.put(id, keyframeEnabledConsumer);
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
