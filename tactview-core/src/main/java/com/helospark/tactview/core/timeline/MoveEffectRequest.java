package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

public class MoveEffectRequest {
    private String effectId;
    private TimelinePosition globalNewPosition;
    private Optional<TimelineLength> maximumJumpToSpecialPositions;
    private List<TimelinePosition> additionalSpecialPositions;
    private boolean moreMoveExpected;

    @Generated("SparkTools")
    private MoveEffectRequest(Builder builder) {
        this.effectId = builder.effectId;
        this.globalNewPosition = builder.globalNewPosition;
        this.maximumJumpToSpecialPositions = builder.maximumJumpToSpecialPositions;
        this.additionalSpecialPositions = builder.additionalSpecialPositions;
        this.moreMoveExpected = builder.moreMoveExpected;
    }

    public String getEffectId() {
        return effectId;
    }

    public TimelinePosition getGlobalNewPosition() {
        return globalNewPosition;
    }

    public Optional<TimelineLength> getMaximumJumpToSpecialPositions() {
        return maximumJumpToSpecialPositions;
    }

    public boolean isMoreMoveExpected() {
        return moreMoveExpected;
    }

    public List<TimelinePosition> getAdditionalSpecialPositions() {
        return additionalSpecialPositions;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String effectId;
        private TimelinePosition globalNewPosition;
        private Optional<TimelineLength> maximumJumpToSpecialPositions = Optional.empty();
        private List<TimelinePosition> additionalSpecialPositions = Collections.emptyList();
        private boolean moreMoveExpected;

        private Builder() {
        }

        public Builder withEffectId(String effectId) {
            this.effectId = effectId;
            return this;
        }

        public Builder withGlobalNewPosition(TimelinePosition globalNewPosition) {
            this.globalNewPosition = globalNewPosition;
            return this;
        }

        public Builder withMaximumJumpToSpecialPositions(Optional<TimelineLength> maximumJumpToSpecialPositions) {
            this.maximumJumpToSpecialPositions = maximumJumpToSpecialPositions;
            return this;
        }

        public Builder withAdditionalSpecialPositions(List<TimelinePosition> additionalSpecialPositions) {
            this.additionalSpecialPositions = additionalSpecialPositions;
            return this;
        }

        public Builder withMoreMoveExpected(boolean moreMoveExpected) {
            this.moreMoveExpected = moreMoveExpected;
            return this;
        }

        public MoveEffectRequest build() {
            return new MoveEffectRequest(this);
        }
    }
}