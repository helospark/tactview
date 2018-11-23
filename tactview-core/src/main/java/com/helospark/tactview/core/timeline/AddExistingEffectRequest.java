package com.helospark.tactview.core.timeline;

import java.util.Objects;

import javax.annotation.Generated;

public class AddExistingEffectRequest {
    private TimelineClip clipToAdd;
    private StatelessEffect effect;

    @Generated("SparkTools")
    private AddExistingEffectRequest(Builder builder) {
        this.clipToAdd = builder.clipToAdd;
        this.effect = builder.effect;
    }

    public TimelineClip getClipToAdd() {
        return clipToAdd;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AddExistingEffectRequest)) {
            return false;
        }
        AddExistingEffectRequest castOther = (AddExistingEffectRequest) other;
        return Objects.equals(clipToAdd, castOther.clipToAdd) && Objects.equals(effect, castOther.effect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clipToAdd, effect);
    }

    @Override
    public String toString() {
        return "AddExistingEffectRequest [clipToAdd=" + clipToAdd + ", effect=" + effect + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineClip clipToAdd;
        private StatelessEffect effect;

        private Builder() {
        }

        public Builder withClipToAdd(TimelineClip clipToAdd) {
            this.clipToAdd = clipToAdd;
            return this;
        }

        public Builder withEffect(StatelessEffect effect) {
            this.effect = effect;
            return this;
        }

        public AddExistingEffectRequest build() {
            return new AddExistingEffectRequest(this);
        }
    }

}
