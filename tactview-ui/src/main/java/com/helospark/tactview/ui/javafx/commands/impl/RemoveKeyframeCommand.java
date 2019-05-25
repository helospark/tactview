package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Optional;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.KeyframeRemovedRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveKeyframeCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private KeyframeRemovedRequest request;
    private Optional<Object> previousValue;

    public RemoveKeyframeCommand(EffectParametersRepository effectParametersRepository, KeyframeRemovedRequest request) {
        this.effectParametersRepository = effectParametersRepository;
        this.request = request;
    }

    @Override
    public void execute() {
        // TODO: this is a bug
        TimelinePosition offset = effectParametersRepository.findGlobalPositionForValueProvider(request.getDescriptorId()).get();
        TimelinePosition localPosition = request.getLocalTimelinePosition();
        TimelinePosition globalPosition = request.getGlobalTimelinePosition();

        if (localPosition == null) {
            localPosition = globalPosition.subtract(offset);
        }
        if (globalPosition == null) {
            globalPosition = localPosition.add(offset);
        }

        previousValue = effectParametersRepository.getKeyframeableEffectValue(request.getDescriptorId(), localPosition);
        effectParametersRepository.removeKeyframe(request.getDescriptorId(), globalPosition);
    }

    @Override
    public void revert() {
        if (previousValue.isPresent()) {
            KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                    .withDescriptorId(request.getDescriptorId())
                    .withGlobalTimelinePosition(request.getGlobalTimelinePosition())
                    .withValue(previousValue.map(String::valueOf).get())
                    .withRevertable(true)
                    .build();
            effectParametersRepository.keyframeAdded(keyframeAddedRequest);
        }
    }

    @Override
    public String toString() {
        return "RemoveKeyframeCommand [effectParametersRepository=" + effectParametersRepository + ", request=" + request + ", previousValue=" + previousValue + "]";
    }

}
