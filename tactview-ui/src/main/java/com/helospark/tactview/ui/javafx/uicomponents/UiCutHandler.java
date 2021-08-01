package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.core.timeline.message.NotificationMessage.Level;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditModeRepository;

@Component
public class UiCutHandler {
    private UiTimelineManager uiTimelineManager;
    private TimelineManagerAccessor timelineManager;
    private LinkClipRepository linkClipRepository;
    private UiCommandInterpreterService commandInterpreter;
    private SelectedNodeRepository selectedNodeRepository;
    private TimelineEditModeRepository timelineEditModeRepository;
    private MessagingService messagingService;

    public UiCutHandler(UiTimelineManager uiTimelineManager, TimelineManagerAccessor timelineManager, LinkClipRepository linkClipRepository, UiCommandInterpreterService commandInterpreter,
            TimelineEditModeRepository timelineEditModeRepository, SelectedNodeRepository selectedNodeRepository, MessagingService messagingService) {
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManager = timelineManager;
        this.linkClipRepository = linkClipRepository;
        this.commandInterpreter = commandInterpreter;
        this.timelineEditModeRepository = timelineEditModeRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.messagingService = messagingService;
    }

    public void cutAllAtCurrentPosition() {
        TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
        List<String> intersectingClips = timelineManager.findIntersectingClips(currentPosition);

        if (intersectingClips.size() > 0) {
            CutClipCommand command = CutClipCommand.builder()
                    .withClipIds(intersectingClips)
                    .withGlobalTimelinePosition(currentPosition)
                    .withLinkedClipRepository(linkClipRepository)
                    .withTimelineManager(timelineManager)
                    .build();
            commandInterpreter.sendWithResult(command);
        }

    }

    public void cutSelectedUntilCursor(boolean isLeft) {
        TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();

        List<TimelineClip> elementsToCut = timelineManager.resolveClipIdsWithAllLinkedClip(selectedNodeRepository.getSelectedClipIds())
                .stream()
                .filter(a -> a.getInterval().contains(currentPosition))
                .collect(Collectors.toList());

        if (elementsToCut.size() > 0) {
            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipIds(elementsToCut.stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withLeft(isLeft)
                    .withUseSpecialPoints(false)
                    .withPosition(currentPosition)
                    .withMoreResizeExpected(false)
                    .withOriginalPosition(isLeft ? elementsToCut.get(0).getInterval().getStartPosition() : elementsToCut.get(0).getInterval().getEndPosition())
                    .withRevertable(true)
                    .withTimelineEditMode(timelineEditModeRepository.getMode())
                    .withTimelineManager(timelineManager)
                    .build();
            commandInterpreter.sendWithResult(command);
        } else {
            messagingService.sendMessage(new NotificationMessage("No selected clips intersecting the playhead", "Unable to cut", Level.WARNING));
        }
    }

}
