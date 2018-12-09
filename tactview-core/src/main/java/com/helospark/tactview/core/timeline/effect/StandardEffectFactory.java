package com.helospark.tactview.core.timeline.effect;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClipType;

public class StandardEffectFactory implements EffectFactory {
    protected List<TimelineClipType> supportedClipTypes;

    private String supportedEffectId;
    private String name;
    private Function<CreateEffectRequest, StatelessEffect> factory;
    private Function<JsonNode, StatelessEffect> restoreFactory;

    @Generated("SparkTools")
    private StandardEffectFactory(Builder builder) {
        this.supportedClipTypes = builder.supportedClipTypes;
        this.supportedEffectId = builder.supportedEffectId;
        this.name = builder.name;
        this.factory = builder.factory;
        this.restoreFactory = builder.restoreFactory;
    }

    @Override
    public boolean doesSupport(CreateEffectRequest request) {
        return request.getEffectId().equals(supportedEffectId) && (supportedClipTypes == null || supportedClipTypes.contains(request.getTimelineClipType()));
    }

    @Override
    public StatelessEffect createEffect(CreateEffectRequest request) {
        StatelessEffect result = factory.apply(request);

        return result;
    }

    @Override
    public StatelessEffect restoreEffect(JsonNode node) {
        return restoreFactory.apply(node);
    }

    @Override
    public String getEffectId() {
        return supportedEffectId;
    }

    @Override
    public String getEffectName() {
        return name;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private List<TimelineClipType> supportedClipTypes = Collections.emptyList();
        private String supportedEffectId;
        private String name;
        private Function<CreateEffectRequest, StatelessEffect> factory;
        private Function<JsonNode, StatelessEffect> restoreFactory;

        private Builder() {
        }

        public Builder withSupportedClipTypes(List<TimelineClipType> supportedClipTypes) {
            this.supportedClipTypes = supportedClipTypes;
            return this;
        }

        public Builder withSupportedEffectId(String supportedEffectId) {
            this.supportedEffectId = supportedEffectId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFactory(Function<CreateEffectRequest, StatelessEffect> factory) {
            this.factory = factory;
            return this;
        }

        public Builder withRestoreFactory(Function<JsonNode, StatelessEffect> restoreFactory) {
            this.restoreFactory = restoreFactory;
            return this;
        }

        public StandardEffectFactory build() {
            return new StandardEffectFactory(this);
        }
    }

}
