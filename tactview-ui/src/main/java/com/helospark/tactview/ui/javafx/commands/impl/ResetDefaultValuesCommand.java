package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ResetDefaultValuesCommand implements UiCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetDefaultValuesCommand.class);
    private EffectParametersRepository effectParametersRepository;
    private final String descriptorId;

    private final Map<String, Object> previousInterpolatorClones = new HashMap<>();

    public ResetDefaultValuesCommand(EffectParametersRepository effectParametersRepository, String descriptorId) {
        this.effectParametersRepository = effectParametersRepository;
        this.descriptorId = descriptorId;
    }

    @Override
    public void execute() {
        KeyframeableEffect previousValue = effectParametersRepository.getKeyframeableEffect(descriptorId);

        if (previousValue != null) {
            List<KeyframeableEffect> children = previousValue.getChildren();

            for (var a : children) {
                String id = a.getId();
                EffectInterpolator clonedInterpolator = a.getInterpolator().deepClone(CloneRequestMetadata.fullCopy());

                previousInterpolatorClones.put(id, clonedInterpolator);

                effectParametersRepository.resetToDefaultValue(id);
            }

            EffectInterpolator previousInterpolator = previousValue.getInterpolator();
            if (previousInterpolator != null) {
                previousInterpolatorClones.put(descriptorId, previousInterpolator.deepClone(CloneRequestMetadata.fullCopy()));
            } else {
                LOGGER.error("No previous value present for interpolator");
            }
            effectParametersRepository.resetToDefaultValue(descriptorId);
        }
    }

    @Override
    public void revert() {
        for (var entry : previousInterpolatorClones.entrySet()) {
            effectParametersRepository.changeInterpolatorToInstance(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean isRevertable() {
        return true;
    }

    @Override
    public String toString() {
        return "ResetDefaultValuesCommand [effectParametersRepository=" + effectParametersRepository + ", descriptorId=" + descriptorId + ", previousInterpolatorClones=" + previousInterpolatorClones
                + "]";
    }

}
