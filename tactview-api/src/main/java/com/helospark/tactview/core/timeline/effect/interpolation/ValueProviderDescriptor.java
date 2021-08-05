package com.helospark.tactview.core.timeline.effect.interpolation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ValueProviderDescriptor {
    private String name;
    private KeyframeableEffect keyframeableEffect;
    private List<ActivePredicate> activePredicate;
    private Optional<Function<TimelinePosition, Boolean>> showPredicate;
    private Map<Object, Object> renderHints;
    private Optional<Function<TimelinePosition, Boolean>> enabledIf;
    private Optional<String> group;

    private ValueProviderDescriptor(Builder builder) {
        this.name = builder.name;
        this.keyframeableEffect = builder.keyframeableEffect;
        this.activePredicate = builder.activePredicate;
        this.showPredicate = builder.showPredicate;
        this.renderHints = builder.renderHints;
        this.enabledIf = builder.enabledIf;
        this.group = builder.group;
    }

    public String getName() {
        return name;
    }

    public KeyframeableEffect getKeyframeableEffect() {
        return keyframeableEffect;
    }

    public List<ActivePredicate> getActivePredicate() {
        return activePredicate;
    }

    public Optional<Function<TimelinePosition, Boolean>> getShowPredicate() {
        return showPredicate;
    }

    public Map<Object, Object> getRenderHints() {
        return renderHints;
    }

    public Optional<Function<TimelinePosition, Boolean>> getEnabledIf() {
        return enabledIf;
    }

    public Optional<String> getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "ValueProviderDescriptor [name=" + name + ", keyframeableEffect=" + keyframeableEffect + ", activePredicate=" + activePredicate + ", showPredicate=" + showPredicate + ", renderHints="
                + renderHints + ", enabledIf=" + enabledIf + ", group=" + group + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFrom(ValueProviderDescriptor valueProviderDescriptor) {
        return new Builder(valueProviderDescriptor);
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String name;
        private KeyframeableEffect keyframeableEffect;
        private List<ActivePredicate> activePredicate = Collections.emptyList();
        private Optional<Function<TimelinePosition, Boolean>> showPredicate = Optional.empty();
        private Map<Object, Object> renderHints = Collections.emptyMap();
        private Optional<Function<TimelinePosition, Boolean>> enabledIf = Optional.empty();
        private Optional<String> group = Optional.empty();

        private Builder() {
        }

        private Builder(ValueProviderDescriptor valueProviderDescriptor) {
            this.name = valueProviderDescriptor.name;
            this.keyframeableEffect = valueProviderDescriptor.keyframeableEffect;
            this.activePredicate = valueProviderDescriptor.activePredicate;
            this.showPredicate = valueProviderDescriptor.showPredicate;
            this.renderHints = valueProviderDescriptor.renderHints;
            this.enabledIf = valueProviderDescriptor.enabledIf;
            this.group = valueProviderDescriptor.group;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withKeyframeableEffect(KeyframeableEffect keyframeableEffect) {
            this.keyframeableEffect = keyframeableEffect;
            return this;
        }

        public Builder withActivePredicate(List<ActivePredicate> activePredicate) {
            this.activePredicate = activePredicate;
            return this;
        }

        public Builder withShowPredicate(Function<TimelinePosition, Boolean> showPredicate) {
            this.showPredicate = Optional.ofNullable(showPredicate);
            return this;
        }

        public Builder withRenderHints(Map<Object, Object> renderHints) {
            this.renderHints = renderHints;
            return this;
        }

        public Builder withEnabledIf(Function<TimelinePosition, Boolean> enabledIf) {
            this.enabledIf = Optional.ofNullable(enabledIf);
            return this;
        }

        public Builder withGroup(String group) {
            this.group = Optional.ofNullable(group);
            return this;
        }

        public ValueProviderDescriptor build() {
            return new ValueProviderDescriptor(this);
        }
    }

}
