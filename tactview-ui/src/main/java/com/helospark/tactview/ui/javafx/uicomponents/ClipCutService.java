package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

import javafx.scene.Node;

@Component
public class ClipCutService {
    private UiCommandInterpreterService commandInterpreter;
    private SelectedNodeRepository selectedNodeRepository;
    private UiTimelineManager uiTimelineManager;
    private TimelineManager timelineManager;

    public ClipCutService(UiCommandInterpreterService commandInterpreter, SelectedNodeRepository selectedNodeRepository, UiTimelineManager uiTimelineManager, TimelineManager timelineManager) {
        this.commandInterpreter = commandInterpreter;
        this.selectedNodeRepository = selectedNodeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManager = timelineManager;
    }

    public void cutSelectedClipAtCurrentTimestamp() {
        TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
        Optional<Node> primaryClip = selectedNodeRepository.getPrimarySelectedClip();
        primaryClip.ifPresent(clip -> {
            CutClipCommand command = CutClipCommand.builder()
                    .withTimelineManager(timelineManager)
                    .withClipId((String) clip.getUserData())
                    .withGlobalTimelinePosition(currentPosition)
                    .build();
            commandInterpreter.sendWithResult(command);
        });
    }
}
