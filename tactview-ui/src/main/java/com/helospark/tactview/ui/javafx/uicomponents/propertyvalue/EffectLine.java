package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.Node;

public abstract class EffectLine {
    protected UiCommandInterpreterService commandInterpreter;
    protected EffectParametersRepository effectParametersRepository;
    protected Node visibleNode;
    protected String descriptorId;
    protected Consumer<Boolean> disabledUpdater;
    protected Consumer<Object> updateFromValue;
    protected Consumer<TimelinePosition> keyframeConsumer;
    protected ValueProviderDescriptor descriptor;
    protected Supplier<Object> currentValueSupplier;

    public void setCommandInterpreter(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    public Supplier<Object> getCurrentValueSupplier() {
        return currentValueSupplier;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public void sendKeyframe(TimelinePosition position) {
        keyframeConsumer.accept(position);
    }

    public abstract void updateUi(TimelinePosition position);

    public Node getVisibleNode() {
        return visibleNode;
    }

    public Consumer<Object> getUpdateFromValue() {
        return updateFromValue;
    }

    public abstract void removeKeyframe(TimelinePosition currentPosition);

    public abstract void removeAllAndSetKeyframe(TimelinePosition currentPosition);

    public Consumer<TimelinePosition> getKeyframeConsumer() {
        return keyframeConsumer;
    }

    protected void disableUiIfNeeded(TimelinePosition position) {
        if (descriptor != null && descriptor.getEnabledIf().isPresent()) {
            Boolean disabled = !descriptor.getEnabledIf().get().apply(position);

            if (disabledUpdater == null) {
                visibleNode.setDisable(disabled);
            } else {
                disabledUpdater.accept(disabled);
            }

        }

    }
}