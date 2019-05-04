package com.helospark.tactview.core.timeline.proceduralclip.script;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ScriptColorRequest {
    public TimelinePosition relativePosition;
    public double scale;
    public int expectedWidth;
    public int expectedHeight;

    @Generated("SparkTools")
    private ScriptColorRequest(Builder builder) {
        this.relativePosition = builder.relativePosition;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition relativePosition;
        private double scale;
        private int expectedWidth;
        private int expectedHeight;

        private Builder() {
        }

        public Builder withRelativePosition(TimelinePosition relativePosition) {
            this.relativePosition = relativePosition;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withExpectedWidth(int expectedWidth) {
            this.expectedWidth = expectedWidth;
            return this;
        }

        public Builder withExpectedHeight(int expectedHeight) {
            this.expectedHeight = expectedHeight;
            return this;
        }

        public ScriptColorRequest build() {
            return new ScriptColorRequest(this);
        }
    }
}
