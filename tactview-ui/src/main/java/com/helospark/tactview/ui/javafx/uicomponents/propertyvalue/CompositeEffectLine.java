package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.Node;

public class CompositeEffectLine extends EffectLine {
    private List<EffectLine> values;
    public Consumer<TimelinePosition> additionalUpdateUi;
    public Supplier<Object> currentValueSupplier;

    @Generated("SparkTools")
    private CompositeEffectLine(Builder builder) {
        this.commandInterpreter = builder.commandInterpreter;
        this.effectParametersRepository = builder.effectParametersRepository;
        this.visibleNode = builder.visibleNode;
        this.descriptorId = builder.descriptorId;
        this.updateFromValue = builder.updateFromValue;
        this.values = builder.values;
        this.additionalUpdateUi = builder.additionalUpdateUi;
        this.descriptor = builder.descriptor;
        this.currentValueSupplier = builder.currentValueSupplier;
    }

    @Override
    public void sendKeyframe(TimelinePosition position) {
        values.stream()
                .forEach(a -> a.sendKeyframe(position));
    }

    @Override
    public void removeKeyframe(TimelinePosition currentPosition) {
        values.stream()
                .forEach(a -> a.removeKeyframe(currentPosition));
    }

    @Override
    public void removeAllAndSetKeyframe(TimelinePosition currentPosition) {
        values.stream()
                .forEach(a -> a.removeAllAndSetKeyframe(currentPosition));
    }

    @Override
    public void updateUi(TimelinePosition position) {
        values.stream()
                .forEach(a -> a.updateUi(position));
        if (additionalUpdateUi != null) {
            additionalUpdateUi.accept(position);
        }
        disableUiIfNeeded(position);
    }

    public Object getCurrentValue() {
        return currentValueSupplier.get();
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
        private Consumer<Object> updateFromValue;
        private List<EffectLine> values = Collections.emptyList();
        private Consumer<TimelinePosition> additionalUpdateUi;
        private ValueProviderDescriptor descriptor;
        private Supplier<Object> currentValueSupplier;

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

        public Builder withUpdateFromValue(Consumer<Object> updateFromValue) {
            this.updateFromValue = updateFromValue;
            return this;
        }

        public Builder withValues(List<EffectLine> values) {
            this.values = values;
            return this;
        }

        public Builder withAdditionalUpdateUi(Consumer<TimelinePosition> additionalUpdateUi) {
            this.additionalUpdateUi = additionalUpdateUi;
            return this;
        }

        public Builder withDescriptor(ValueProviderDescriptor descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        public Builder withCurrentValueSupplier(Supplier<Object> currentValueSupplier) {
            this.currentValueSupplier = currentValueSupplier;
            return this;
        }

        public CompositeEffectLine build() {
            return new CompositeEffectLine(this);
        }
    }

}
