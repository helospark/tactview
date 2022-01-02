package com.helospark.tactview.core.timeline;

import java.util.Objects;

public class ClipChannelPair {
    public TimelineClip clip;
    public TimelineChannel channel;

    public ClipChannelPair(TimelineClip clip, TimelineChannel channel) {
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

    @Override
    public String toString() {
        return "ClipChannelPair [clip=" + clip + ", channel=" + channel + "]";
    }

}
