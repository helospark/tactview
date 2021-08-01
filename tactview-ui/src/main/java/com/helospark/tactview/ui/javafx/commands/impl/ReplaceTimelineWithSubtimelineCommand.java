package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.subtimeline.SubtimelineFromTimelineFactory;
import com.helospark.tactview.core.timeline.subtimeline.audio.SubtimelineAudioClip;
import com.helospark.tactview.core.timeline.subtimeline.video.SubtimelineVisualClip;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ReplaceTimelineWithSubtimelineCommand implements UiCommand {
    private SubtimelineFromTimelineFactory subtimelineFromTimelineFactory;
    private TimelineManagerAccessor timelineManagerAccessor;
    private LinkClipRepository linkClipRepository;

    private List<ChannelClipPair> clipsRemoved = new ArrayList<>();
    private List<String> addedIds = new ArrayList<>();

    public ReplaceTimelineWithSubtimelineCommand(SubtimelineFromTimelineFactory subtimelineFromTimelineFactory, TimelineManagerAccessor timelineManagerAccessor,
            LinkClipRepository linkClipRepository) {
        this.subtimelineFromTimelineFactory = subtimelineFromTimelineFactory;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.linkClipRepository = linkClipRepository;
    }

    @Override
    public void execute() {
        synchronized (timelineManagerAccessor.getFullLock()) {
            SubtimelineVisualClip newVideoClip = subtimelineFromTimelineFactory.createSubtimelineVideoClipFromCurrentTimeline();
            SubtimelineAudioClip newAudioClip = subtimelineFromTimelineFactory.createSubtimelineAudioClipFromCurrentTimeline();

            for (var id : timelineManagerAccessor.getAllClipIds()) {
                TimelineChannel channel = timelineManagerAccessor.findChannelForClipId(id).get();
                TimelineClip clip = timelineManagerAccessor.findClipById(id).get().cloneClip(CloneRequestMetadata.fullCopy());
                clipsRemoved.add(new ChannelClipPair(channel, clip));
                timelineManagerAccessor.removeClip(id);
            }

            timelineManagerAccessor.addClip(timelineManagerAccessor.getChannels().get(0), newVideoClip);
            addedIds.add(newVideoClip.getId());

            timelineManagerAccessor.addClip(timelineManagerAccessor.getChannels().get(1), newAudioClip);
            addedIds.add(newAudioClip.getId());

            linkClipRepository.linkClip(newVideoClip.getId(), newAudioClip.getId());
        }
    }

    @Override
    public void revert() {
        addedIds.stream()
                .forEach(addedClipId -> timelineManagerAccessor.removeClip(addedClipId));

        for (var clipChannelPair : clipsRemoved) {
            timelineManagerAccessor.addClip(clipChannelPair.channel, clipChannelPair.clip);
        }
    }

    static class ChannelClipPair {
        TimelineChannel channel;
        TimelineClip clip;
        public ChannelClipPair(TimelineChannel channel, TimelineClip clip) {
            this.channel = channel;
            this.clip = clip;
        }

    }

}
