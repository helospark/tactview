package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveAllKeyframeCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;
    private String id;

    private Map<TimelinePosition, Object> previousValues;

    public RemoveAllKeyframeCommand(EffectParametersRepository effectParametersRepository, String id) {
        this.effectParametersRepository = effectParametersRepository;
        this.id = id;
    }

    @Override
    public void execute() {
        previousValues = effectParametersRepository.getAllKeyframes(id);
        previousValues.keySet()
                .stream()
                .forEach(key -> {
                    effectParametersRepository.removeKeyframe(id, key);
                });
    }

    @Override
    public void revert() {
        previousValues.entrySet()
                .stream()
                .forEach(entry -> {
                    KeyframeAddedRequest request = KeyframeAddedRequest.builder()
                            .withDescriptorId(id)
                            .withGlobalTimelinePosition(entry.getKey())
                            .withValue(String.valueOf(entry.getValue()))
                            .build();
                    effectParametersRepository.keyframeAdded(request);
                });
    }

}
