package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.MoveChannelRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class MoveChannelCommand implements UiCommand {
    private TimelineManagerAccessor timelineManagerAccessor;
    private int originalIndex;
    private int newIndex;

    public MoveChannelCommand(TimelineManagerAccessor timelineManager, int originalIndex, int newIndex) {
        this.timelineManagerAccessor = timelineManager;
        this.originalIndex = originalIndex;
        this.newIndex = newIndex;
    }

    @Override
    public void execute() {
        MoveChannelRequest request = MoveChannelRequest.builder()
                .withOriginalIndex(originalIndex)
                .withNewIndex(newIndex)
                .build();

        timelineManagerAccessor.moveChannel(request);
    }

    @Override
    public void revert() {
        MoveChannelRequest request = MoveChannelRequest.builder()
                .withOriginalIndex(newIndex)
                .withNewIndex(originalIndex)
                .build();

        timelineManagerAccessor.moveChannel(request);
    }

}
