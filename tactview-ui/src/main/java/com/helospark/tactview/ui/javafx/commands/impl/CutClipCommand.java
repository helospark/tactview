package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class CutClipCommand implements UiCommand {
    private List<String> clipIds;
    private TimelinePosition globalTimelinePosition;

    private TimelineManager timelineManager;
    private LinkClipRepository linkClipRepository;

    private List<TimelineClip> originalCuttedClip = new ArrayList<>();
    private List<TimelineChannel> originalChannel = new ArrayList<>();

    private Map<String, String> originalLinkedClips = new HashMap<>();
    private List<List<String>> cuttedPartIds = new ArrayList<>();
    private Map<String, String> createdLinks = new HashMap<>();

    @Generated("SparkTools")
    private CutClipCommand(Builder builder) {
        this.clipIds = builder.clipIds;
        this.globalTimelinePosition = builder.globalTimelinePosition;
        this.timelineManager = builder.timelineManager;
        this.linkClipRepository = builder.linkClipRepository;
    }

    @Override
    public void execute() {
        for (String clipId : clipIds) {
            List<String> linkedWithClips = linkClipRepository.getLinkedClips(clipId);
            for (String linkedId : linkedWithClips) {
                if (clipIds.contains(linkedId)) {
                    originalLinkedClips.put(clipId, linkedId);
                }
            }
        }
        for (String clipId : clipIds) {
            originalCuttedClip.add(timelineManager.findClipById(clipId).orElseThrow().cloneClip(CloneRequestMetadata.fullCopy()));
            originalChannel.add(timelineManager.findChannelForClipId(clipId).orElseThrow());
            List<String> currentClipCuttedParts = this.timelineManager.cutClip(clipId, globalTimelinePosition)
                    .stream()
                    .map(clip -> clip.getId())
                    .collect(Collectors.toList());
            cuttedPartIds.add(currentClipCuttedParts);
        }
        for (int i = 0; i < clipIds.size(); ++i) {
            String linkedClip = originalLinkedClips.get(clipIds.get(i));
            if (linkedClip != null) {
                int linkedIndex = clipIds.indexOf(linkedClip);
                List<String> first = cuttedPartIds.get(i);
                List<String> second = cuttedPartIds.get(linkedIndex);

                for (int k = 0; k < first.size(); ++k) {
                    linkClipRepository.linkClip(first.get(k), second.get(k));
                    createdLinks.put(first.get(k), second.get(k));
                }
            }
        }
    }

    @Override
    public void revert() {
        cuttedPartIds.stream()
                .flatMap(clips -> clips.stream())
                .forEach(clipId -> timelineManager.removeClip(clipId));
        for (int i = 0; i < originalCuttedClip.size(); ++i) {
            timelineManager.addClip(originalChannel.get(i), originalCuttedClip.get(i));
        }
        for (var entry : originalLinkedClips.entrySet()) {
            linkClipRepository.linkClip(entry.getKey(), entry.getValue());
        }
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private List<String> clipIds;
        private TimelinePosition globalTimelinePosition;
        private TimelineManager timelineManager;
        private LinkClipRepository linkClipRepository;

        private Builder() {
        }

        public Builder withClipIds(List<String> clipId) {
            this.clipIds = clipId;
            return this;
        }

        public Builder withLinkedClipRepository(LinkClipRepository linkClipRepository) {
            this.linkClipRepository = linkClipRepository;
            return this;
        }

        public Builder withGlobalTimelinePosition(TimelinePosition globalTimelinePosition) {
            this.globalTimelinePosition = globalTimelinePosition;
            return this;
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public CutClipCommand build() {
            return new CutClipCommand(this);
        }
    }
}
