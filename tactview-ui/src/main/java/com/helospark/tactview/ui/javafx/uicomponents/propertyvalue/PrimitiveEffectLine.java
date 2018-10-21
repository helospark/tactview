package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;

import javafx.scene.Node;

public class PrimitiveEffectLine extends EffectLine {
    public Consumer<TimelinePosition> updateFunction;
    public Supplier<String> currentValueProvider;

    @Generated("SparkTools")
    private PrimitiveEffectLine(Builder builder) {
        this.commandInterpreter = builder.commandInterpreter;
        this.effectParametersRepository = builder.effectParametersRepository;
        this.descriptorId = builder.descriptorId;
        this.visibleNode = builder.visibleNode;
        this.updateFunction = builder.updateFunction;
        this.currentValueProvider = builder.currentValueProvider;
    }

    @Override
    public void sendKeyframe(TimelinePosition position) {
        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(descriptorId)
                .withGlobalTimelinePosition(position)
                .withValue(currentValueProvider.get()).build();

        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
    }

    @Override
    public void updateUi(TimelinePosition position) {
        updateFunction.accept(position);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private UiCommandInterpreterService commandInterpreter;
        private EffectParametersRepository effectParametersRepository;
        private String descriptorId;
        private Node visibleNode;
        private Consumer<TimelinePosition> updateFunction;
        private Supplier<String> currentValueProvider;
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
        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }
        public Builder withVisibleNode(Node visibleNode) {
            this.visibleNode = visibleNode;
            return this;
        }
        public Builder withUpdateFunction(Consumer<TimelinePosition> updateFunction) {
            this.updateFunction = updateFunction;
            return this;
        }
        public Builder withCurrentValueProvider(Supplier<String> currentValueProvider) {
            this.currentValueProvider = currentValueProvider;
            return this;
        }
        public PrimitiveEffectLine build() {
            return new PrimitiveEffectLine(this);
        }
    }

}
