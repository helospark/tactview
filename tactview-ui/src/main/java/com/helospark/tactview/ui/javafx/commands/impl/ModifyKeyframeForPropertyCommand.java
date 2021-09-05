package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.ModifyKeyframeRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ModifyKeyframeForPropertyCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private ModifyKeyframeRequest request;
    private KeyframeableEffect<?> keyframeableEffect;
    private Object previousValue;
    private boolean executed = false;

    public ModifyKeyframeForPropertyCommand(EffectParametersRepository effectParametersRepository, ModifyKeyframeRequest request) {
        this.effectParametersRepository = effectParametersRepository;
        this.request = request;
    }

    @Override
    public void execute() {
        keyframeableEffect = effectParametersRepository.getKeyframeableEffect(request.getDescriptorId());

        previousValue = keyframeableEffect.getValueAt(request.getOriginalTimelinePosition());
        if (previousValue != null && keyframeableEffect != null) {

            KeyframeAddedRequest addKeyframeRequest = KeyframeAddedRequest
                    .builder()
                    .withDescriptorId(keyframeableEffect.getId())
                    .withGlobalTimelinePosition(request.getNewTimelinePosition())
                    .withRevertable(false)
                    .withValue(request.getValue())
                    .build();

            effectParametersRepository.removeKeyframe(keyframeableEffect.getId(), request.getOriginalTimelinePosition());
            effectParametersRepository.keyframeAdded(addKeyframeRequest);

            executed = true;
        }
    }

    @Override
    public void revert() {
        if (executed) {
            effectParametersRepository.removeKeyframe(keyframeableEffect.getId(), request.getNewTimelinePosition());

            KeyframeAddedRequest addKeyframeRequest = KeyframeAddedRequest
                    .builder()
                    .withDescriptorId(keyframeableEffect.getId())
                    .withGlobalTimelinePosition(request.getRevertTimelinePosition())
                    .withRevertable(false)
                    .withValue(request.getRevertValue())
                    .build();

            effectParametersRepository.keyframeAdded(addKeyframeRequest);
        }
    }

    @Override
    public boolean isRevertable() {
        return request.isRevertable();
    }

    @Override
    public String toString() {
        return "ModifyKeyframeForPropertyCommand [effectParametersRepository=" + effectParametersRepository + ", request=" + request + ", keyframeableEffect=" + keyframeableEffect + ", previousValue="
                + previousValue + ", executed=" + executed + "]";
    }

}
