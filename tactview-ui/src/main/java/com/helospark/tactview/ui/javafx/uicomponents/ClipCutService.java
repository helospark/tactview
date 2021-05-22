package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

@Component
public class ClipCutService {
    private UiCommandInterpreterService commandInterpreter;
    private SelectedNodeRepository selectedNodeRepository;
    private UiTimelineManager uiTimelineManager;
    private TimelineManagerAccessor timelineManager;
    private LinkClipRepository linkClipRepository;

    public ClipCutService(UiCommandInterpreterService commandInterpreter, SelectedNodeRepository selectedNodeRepository, UiTimelineManager uiTimelineManager, TimelineManagerAccessor timelineManager,
            LinkClipRepository linkClipRepository) {
        this.commandInterpreter = commandInterpreter;
        this.selectedNodeRepository = selectedNodeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManager = timelineManager;
        this.linkClipRepository = linkClipRepository;
    }

    public void cutSelectedClipAtCurrentTimestamp() {
        Optional<String> primaryClip = selectedNodeRepository.getPrimarySelectedClip();
        primaryClip.ifPresent(clipId -> {
            cutClip(clipId, true);
        });
    }

    public void cutClip(String currentClipId, boolean includeLinked) {
        TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
        cutClipAtPosition(currentClipId, includeLinked, currentPosition);
    }

    public void cutClipAtPosition(String currentClipId, boolean includeLinked, TimelinePosition currentPosition) {
        List<String> linkedIds = linkClipRepository.getLinkedClips(currentClipId);
        List<String> clipIds = new ArrayList<>();
        if (includeLinked) {
            clipIds.addAll(linkedIds);
        }
        clipIds.add(currentClipId);

        CutClipCommand command = CutClipCommand.builder()
                .withTimelineManager(timelineManager)
                .withClipIds(clipIds)
                .withGlobalTimelinePosition(currentPosition)
                .withLinkedClipRepository(linkClipRepository)
                .build();
        commandInterpreter.sendWithResult(command);
    }
}
