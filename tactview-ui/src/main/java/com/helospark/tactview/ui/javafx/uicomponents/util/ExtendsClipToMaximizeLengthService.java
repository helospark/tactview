package com.helospark.tactview.ui.javafx.uicomponents.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.EffectResizedCommand;

@Component
public class ExtendsClipToMaximizeLengthService {
    private TimelineManagerAccessor timelineManagerAccessor;
    private UiCommandInterpreterService commandInterpreterService;

    public ExtendsClipToMaximizeLengthService(TimelineManagerAccessor timelineManagerAccessor, UiCommandInterpreterService commandInterpreterService) {
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.commandInterpreterService = commandInterpreterService;
    }

    public void extendEffectToClipSize(String clipId, StatelessEffect statelessEffect) {
        TimelineClip clip = timelineManagerAccessor.findClipById(clipId).get();

        EffectResizedCommand moveLeft = EffectResizedCommand.builder()
                .withEffectId(statelessEffect.getId())
                .withLeft(true)
                .withMoreResizeExpected(false)
                .withGlobalPosition(clip.getInterval().getStartPosition())
                .withRevertable(true)
                .withTimelineManager(timelineManagerAccessor)
                .withUseSpecialPoints(false)
                .withAllowResizeToDisplaceOtherEffects(true)
                .build();

        TimelinePosition clipRight = clip.getInterval().getEndPosition();

        EffectResizedCommand moveRight = EffectResizedCommand.builder()
                .withEffectId(statelessEffect.getId())
                .withLeft(false)
                .withMoreResizeExpected(false)
                .withGlobalPosition(clipRight)
                .withRevertable(true)
                .withTimelineManager(timelineManagerAccessor)
                .withUseSpecialPoints(false)
                .withAllowResizeToDisplaceOtherEffects(true)
                .build();

        CompositeCommand compositeCommand = new CompositeCommand(moveLeft, moveRight);

        commandInterpreterService.sendWithResult(compositeCommand);
    }

}
