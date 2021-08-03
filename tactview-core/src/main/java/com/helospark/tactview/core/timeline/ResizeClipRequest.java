package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResizeClipRequest {
    private TimelineClip clip;
    private boolean left;
    private TimelinePosition position;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumSize;
    private boolean useSpecialPoints;
    private boolean moreResizeExpected;
    private List<String> ignoredSpecialSuggestionClips;
    private List<TimelineClip> ignoreIntersection;
    private boolean keepLeftSideOfClipAtSamePlace;

    private ResizeClipRequest(Builder builder) {
        this.clip = builder.clip;
        this.left = builder.left;
        this.position = builder.position;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.minimumSize = builder.minimumSize;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.ignoredSpecialSuggestionClips = builder.ignoredSpecialSuggestionClips;
        this.ignoreIntersection = builder.ignoreIntersection;
        this.keepLeftSideOfClipAtSamePlace = builder.keepLeftSideOfClipAtSamePlace;
    }

    public TimelineClip getClip() {
        return clip;
    }

    public boolean isLeft() {
        return left;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public TimelineLength getMaximumJumpLength() {
        return maximumJumpLength;
    }

    public boolean isUseSpecialPoints() {
        return useSpecialPoints;
    }

    public Optional<TimelineLength> getMinimumSize() {
        return Optional.ofNullable(minimumSize);
    }

    public boolean isMoreResizeExpected() {
        return moreResizeExpected;
    }

    public List<String> getIgnoredSpecialSuggestionClips() {
        return ignoredSpecialSuggestionClips;
    }

    public List<TimelineClip> getIgnoreIntersection() {
        return ignoreIntersection;
    }

    public boolean getKeepLeftSideOfClipAtSamePlace() {
        return keepLeftSideOfClipAtSamePlace;
    }

    @Override
    public String toString() {
        return "ResizeClipRequest [clip=" + clip + ", left=" + left + ", position=" + position + ", maximumJumpLength=" + maximumJumpLength + ", minimumSize=" + minimumSize + ", useSpecialPoints="
                + useSpecialPoints + ", moreResizeExpected=" + moreResizeExpected + ", ignoredSpecialSuggestionClips=" + ignoredSpecialSuggestionClips + ", ignoreIntersection=" + ignoreIntersection
                + ", keepLeftSideOfClipAtSamePlace=" + keepLeftSideOfClipAtSamePlace + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TimelineClip clip;
        private boolean left;
        private TimelinePosition position;
        private TimelineLength maximumJumpLength;
        private TimelineLength minimumSize;
        private boolean useSpecialPoints;
        private boolean moreResizeExpected;
        private List<String> ignoredSpecialSuggestionClips = Collections.emptyList();
        private List<TimelineClip> ignoreIntersection = Collections.emptyList();
        private boolean keepLeftSideOfClipAtSamePlace;
        private Builder() {
        }

        public Builder withClip(TimelineClip clip) {
            this.clip = clip;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public Builder withMinimumSize(TimelineLength minimumSize) {
            this.minimumSize = minimumSize;
            return this;
        }

        public Builder withUseSpecialPoints(boolean useSpecialPoints) {
            this.useSpecialPoints = useSpecialPoints;
            return this;
        }

        public Builder withMoreResizeExpected(boolean moreResizeExpected) {
            this.moreResizeExpected = moreResizeExpected;
            return this;
        }

        public Builder withIgnoredSpecialSuggestionClips(List<String> ignoredSpecialSuggestionClips) {
            this.ignoredSpecialSuggestionClips = ignoredSpecialSuggestionClips;
            return this;
        }

        public Builder withIgnoreIntersection(List<TimelineClip> ignoreIntersection) {
            this.ignoreIntersection = ignoreIntersection;
            return this;
        }

        public Builder withKeepLeftSideOfClipAtSamePlace(boolean keepLeftSideOfClipAtSamePlace) {
            this.keepLeftSideOfClipAtSamePlace = keepLeftSideOfClipAtSamePlace;
            return this;
        }

        public ResizeClipRequest build() {
            return new ResizeClipRequest(this);
        }
    }

}
