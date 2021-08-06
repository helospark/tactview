package com.helospark.tactview.ui.javafx.commands.impl.service;

import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

@Component
public class MoveByUnitService {
    private SelectedNodeRepository selectedNodeRepository;
    private TimelineManagerAccessor timelineManagerAccessor;
    private ProjectRepository projectRepository;
    private UiCommandInterpreterService commandInterpreter;

    public MoveByUnitService(SelectedNodeRepository selectedNodeRepository, TimelineManagerAccessor timelineManagerAccessor, ProjectRepository projectRepository,
            UiCommandInterpreterService commandInterpreter) {
        this.selectedNodeRepository = selectedNodeRepository;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.projectRepository = projectRepository;
        this.commandInterpreter = commandInterpreter;
    }

    public void moveByOneUnit(Direction direction) {
        List<String> selectedClipIds = selectedNodeRepository.getSelectedClipIds();
        if (selectedClipIds.size() > 0) {
            String firstClipId = selectedClipIds.get(0);
            TimelinePosition startPosition = timelineManagerAccessor.findClipById(firstClipId).get().getInterval().getStartPosition();

            if (direction.equals(Direction.LEFT) || direction.equals(Direction.RIGHT)) {
                String channelId = timelineManagerAccessor.findChannelForClipId(firstClipId).get().getId();
                TimelinePosition newPosition = direction.equals(Direction.LEFT) ? startPosition.subtract(projectRepository.getFrameTime()) : startPosition.add(projectRepository.getFrameTime());
                ClipMovedCommand clipMovedCommand = ClipMovedCommand.builder()
                        .withAdditionalClipIds(selectedClipIds)
                        .withClipId(firstClipId)
                        .withEnableJumpingToSpecialPosition(false)
                        .withIsRevertable(true)
                        .withMoreMoveExpected(false)
                        .withNewPosition(newPosition)
                        .withNewChannelId(channelId)
                        .withOriginalChannelId(channelId)
                        .withPreviousPosition(startPosition)
                        .withTimelineManager(timelineManagerAccessor)
                        .build();

                commandInterpreter.sendWithResult(clipMovedCommand).join();
            } else {
                String channelId = timelineManagerAccessor.findChannelForClipId(firstClipId).get().getId();
                Integer channelIndex = timelineManagerAccessor.findChannelIndexForClipId(firstClipId).get();
                Integer newChannelIndex = direction.equals(Direction.DOWN) ? channelIndex + 1 : channelIndex - 1;
                Optional<TimelineChannel> newChannel = timelineManagerAccessor.findChannelOnIndex(newChannelIndex);
                if (newChannel.isPresent()) {
                    ClipMovedCommand clipMovedCommand = ClipMovedCommand.builder()
                            .withAdditionalClipIds(selectedClipIds)
                            .withClipId(firstClipId)
                            .withEnableJumpingToSpecialPosition(false)
                            .withIsRevertable(true)
                            .withMoreMoveExpected(false)
                            .withNewPosition(startPosition)
                            .withNewChannelId(newChannel.get().getId())
                            .withOriginalChannelId(channelId)
                            .withPreviousPosition(startPosition)
                            .withTimelineManager(timelineManagerAccessor)
                            .build();

                    commandInterpreter.sendWithResult(clipMovedCommand).join();
                }
            }
        }
    }

    public static enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

}
