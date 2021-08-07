package com.helospark.tactview.ui.javafx.commands.impl.domain;

import java.util.Objects;

import com.helospark.tactview.core.timeline.TimelineClip;

public class ClipChannelPair {
    public TimelineClip clip;
    public String channel;

    public ClipChannelPair(TimelineClip clip, String channel) {
        this.clip = clip;
        this.channel = channel;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ClipChannelPair)) {
            return false;
        }
        ClipChannelPair castOther = (ClipChannelPair) other;
        return Objects.equals(clip, castOther.clip) && Objects.equals(channel, castOther.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clip, channel);
    }

}
