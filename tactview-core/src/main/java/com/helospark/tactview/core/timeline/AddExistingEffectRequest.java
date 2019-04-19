package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Generated;

public class AddExistingEffectRequest {
    private TimelineClip clipToAdd;
    private List<StatelessEffect> effects;

    @Generated("SparkTools")
    private AddExistingEffectRequest(Builder builder) {
        this.clipToAdd = builder.clipToAdd;
        this.effects = builder.effects;
    }

    public TimelineClip getClipToAdd() {
        return clipToAdd;
    }

    public List<StatelessEffect> getEffect() {
        return effects;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AddExistingEffectRequest)) {
            return false;
        }
        AddExistingEffectRequest castOther = (AddExistingEffectRequest) other;
        return Objects.equals(clipToAdd, castOther.clipToAdd) && Objects.equals(effects, castOther.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clipToAdd, effects);
    }

    @Override
    public String toString() {
        return "AddExistingEffectRequest [clipToAdd=" + clipToAdd + ", effect=" + effects + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineClip clipToAdd;
        private List<StatelessEffect> effects = Collections.emptyList();

        private Builder() {
        }

        public Builder withClipToAdd(TimelineClip clipToAdd) {
            this.clipToAdd = clipToAdd;
            return this;
        }

        public Builder withEffects(List<StatelessEffect> effect) {
            this.effects = effect;
            return this;
        }

        public AddExistingEffectRequest build() {
            return new AddExistingEffectRequest(this);
        }
    }

}
