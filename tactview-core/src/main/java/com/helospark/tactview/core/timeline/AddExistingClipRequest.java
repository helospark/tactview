package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Generated;

public class AddExistingClipRequest {
    private TimelineClip clipToAdd;
    private TimelineChannel channel;
    private Optional<TimelinePosition> position;

    private List<ClipChannelPair> additionalClipsToAdd;

    @Generated("SparkTools")
    private AddExistingClipRequest(Builder builder) {
        this.clipToAdd = builder.clipToAdd;
        this.channel = builder.channel;
        this.position = builder.position;
        this.additionalClipsToAdd = builder.additionalClipsToAdd;
    }

    public TimelineClip getClipToAdd() {
        return clipToAdd;
    }

    public TimelineChannel getChannel() {
        return channel;
    }

    public List<ClipChannelPair> getAdditionalClipsToAdd() {
        return additionalClipsToAdd;
    }

    public Optional<TimelinePosition> getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "AddExistingClipRequest [clipToAdd=" + clipToAdd + ", channel=" + channel + ", position=" + position + ", additionalClipsToAdd=" + additionalClipsToAdd + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AddExistingClipRequest)) {
            return false;
        }
        AddExistingClipRequest castOther = (AddExistingClipRequest) other;
        return Objects.equals(clipToAdd, castOther.clipToAdd) && Objects.equals(channel, castOther.channel) && Objects.equals(position, castOther.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clipToAdd, channel, position);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineClip clipToAdd;
        private TimelineChannel channel;
        private Optional<TimelinePosition> position = Optional.empty();
        private List<ClipChannelPair> additionalClipsToAdd = Collections.emptyList();

        private Builder() {
        }

        public Builder withClipToAdd(TimelineClip clipToAdd) {
            this.clipToAdd = clipToAdd;
            return this;
        }

        public Builder withChannel(TimelineChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder withPosition(Optional<TimelinePosition> position) {
            this.position = position;
            return this;
        }

        public Builder withAdditionalClipsToAdd(List<ClipChannelPair> additionalClipsToAdd) {
            this.additionalClipsToAdd = additionalClipsToAdd;
            return this;
        }

        public AddExistingClipRequest build() {
            return new AddExistingClipRequest(this);
        }
    }

}
