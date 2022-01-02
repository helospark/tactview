package com.helospark.tactview.core.timeline;

import java.util.Objects;

public class ClipChannelIdPair {
    public TimelineClip clip;
    public String channel;

    public ClipChannelIdPair(TimelineClip clip, String channel) {
        this.clip = clip;
        this.channel = channel;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ClipChannelIdPair)) {
            return false;
        }
        ClipChannelIdPair castOther = (ClipChannelIdPair) other;
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
