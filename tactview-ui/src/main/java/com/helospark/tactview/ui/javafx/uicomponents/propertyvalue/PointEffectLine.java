package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.Node;

public class PointEffectLine extends EffectLine {
    private EffectLine xCoordinate;
    private EffectLine yCoordinate;

    @Generated("SparkTools")
    private PointEffectLine(Builder builder) {
        this.commandInterpreter = builder.commandInterpreter;
        this.effectParametersRepository = builder.effectParametersRepository;
        this.visibleNode = builder.visibleNode;
        this.descriptorId = builder.descriptorId;
        this.xCoordinate = builder.xCoordinate;
        this.yCoordinate = builder.yCoordinate;
    }

    @Override
    public void sendKeyframe(TimelinePosition position) {
        xCoordinate.sendKeyframe(position);
        yCoordinate.sendKeyframe(position);
    }

    @Override
    public void updateUi(TimelinePosition position) {
        xCoordinate.updateUi(position);
        yCoordinate.updateUi(position);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private UiCommandInterpreterService commandInterpreter;
        private EffectParametersRepository effectParametersRepository;
        private Node visibleNode;
        private String descriptorId;
        private EffectLine xCoordinate;
        private EffectLine yCoordinate;

        private Builder() {
        }

        public Builder withCommandInterpreter(UiCommandInterpreterService commandInterpreter) {
            this.commandInterpreter = commandInterpreter;
            return this;
        }

        public Builder withEffectParametersRepository(EffectParametersRepository effectParametersRepository) {
            this.effectParametersRepository = effectParametersRepository;
            return this;
        }

        public Builder withVisibleNode(Node visibleNode) {
            this.visibleNode = visibleNode;
            return this;
        }

        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }

        public Builder withXCoordinate(EffectLine xCoordinate) {
            this.xCoordinate = xCoordinate;
            return this;
        }

        public Builder withYCoordinate(EffectLine yCoordinate) {
            this.yCoordinate = yCoordinate;
            return this;
        }

        public PointEffectLine build() {
            return new PointEffectLine(this);
        }
    }

}
