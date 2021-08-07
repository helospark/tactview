package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AddClipRequestMetaDataKey;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddClipsCommand implements UiCommand {
    private String channelId;
    private TimelinePosition position;
    private List<String> filePaths;
    private String proceduralClipId;
    private Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey;

    private final TimelineManagerAccessor timelineManager;

    private List<String> addedClipIds = new ArrayList<>();

    private AddClipsCommand(Builder builder) {
        this.channelId = builder.channelId;
        this.position = builder.position;
        this.filePaths = builder.filePaths;
        this.proceduralClipId = builder.proceduralClipId;
        this.addClipRequestMetadataKey = builder.addClipRequestMetadataKey;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        TimelinePosition positionToAddClipTo = position;
        for (var file : filePaths) {
            AddClipRequest addClipRequest = AddClipRequest.builder()
                    .withChannelId(channelId)
                    .withFilePath(file)
                    .withPosition(positionToAddClipTo)
                    .withProceduralClipId(null)
                    .withAddClipRequestMetadataKey(addClipRequestMetadataKey)
                    .build();

            TimelineClip result = timelineManager.addClip(addClipRequest);
            positionToAddClipTo = result.getInterval().getEndPosition();
            addedClipIds.add(result.getId());
        }
        if (proceduralClipId != null) {
            AddClipRequest addClipRequest = AddClipRequest.builder()
                    .withChannelId(channelId)
                    .withFilePath(null)
                    .withPosition(positionToAddClipTo)
                    .withProceduralClipId(proceduralClipId)
                    .build();

            TimelineClip result = timelineManager.addClip(addClipRequest);
            addedClipIds.add(result.getId());
        }
    }

    @Override
    public void revert() {
        for (var id : addedClipIds) {
            timelineManager.removeClip(id);
        }
    }

    public TimelinePosition getRequestedPosition() {
        return position;
    }

    public List<String> getAddedClipIds() {
        return addedClipIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String channelId;
        private TimelinePosition position;
        private List<String> filePaths = Collections.emptyList();
        private String proceduralClipId;
        private Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey = Collections.emptyMap();
        private TimelineManagerAccessor timelineManager;

        private Builder() {
        }

        public Builder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withFilePaths(List<String> filePaths) {
            this.filePaths = filePaths;
            return this;
        }

        public Builder withProceduralClipId(String proceduralClipId) {
            this.proceduralClipId = proceduralClipId;
            return this;
        }

        public Builder withAddClipRequestMetadataKey(Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey) {
            this.addClipRequestMetadataKey = addClipRequestMetadataKey;
            return this;
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public AddClipsCommand build() {
            return new AddClipsCommand(this);
        }
    }

}
