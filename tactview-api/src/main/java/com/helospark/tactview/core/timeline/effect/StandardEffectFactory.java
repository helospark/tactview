package com.helospark.tactview.core.timeline.effect;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.util.ReflectionUtil;

public class StandardEffectFactory implements EffectFactory {
    protected List<TimelineClipType> supportedClipTypes;
    protected TimelineEffectType effectType;
    protected boolean isFullWidth;

    private String supportedEffectId;
    private String name;
    private Function<CreateEffectRequest, StatelessEffect> factory;
    private BiFunction<JsonNode, LoadMetadata, StatelessEffect> restoreFactory;

    @Generated("SparkTools")
    private StandardEffectFactory(Builder builder) {
        this.supportedClipTypes = builder.supportedClipTypes;
        this.effectType = builder.effectType;
        this.supportedEffectId = builder.supportedEffectId;
        this.name = builder.name;
        this.factory = builder.factory;
        this.restoreFactory = builder.restoreFactory;
        this.isFullWidth = builder.isFullWidth;
    }

    @Override
    public TimelineEffectType getEffectType() {
        return effectType;
    }

    @Override
    public boolean doesSupport(CreateEffectRequest request) {
        return request.getEffectId().equals(supportedEffectId) && (supportedClipTypes == null || supportedClipTypes.contains(request.getTimelineClipType()));
    }

    @Override
    public StatelessEffect createEffect(CreateEffectRequest request) {
        StatelessEffect result = factory.apply(request);
        result.initializeValueProvider();
        return result;
    }

    @Override
    public StatelessEffect restoreEffect(JsonNode node, LoadMetadata loadMetadata) {
        StatelessEffect result = restoreFactory.apply(node, loadMetadata);
        result.initializeValueProvider();
        ReflectionUtil.realoadSavedFields(node.get("savedFields"), result, loadMetadata);
        return result;
    }

    @Override
    public String getEffectId() {
        return supportedEffectId;
    }

    @Override
    public String getEffectName() {
        return name;
    }

    @Override
    public List<TimelineClipType> getSupportedTypes() {
        return supportedClipTypes;
    }

    @Override
    public boolean isFullWidth() {
        return isFullWidth;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private List<TimelineClipType> supportedClipTypes = Collections.emptyList();
        private TimelineEffectType effectType;
        private String supportedEffectId;
        private String name;
        private Function<CreateEffectRequest, StatelessEffect> factory;
        private BiFunction<JsonNode, LoadMetadata, StatelessEffect> restoreFactory;
        private boolean isFullWidth;

        private Builder() {
        }

        public Builder withSupportedClipTypes(List<TimelineClipType> supportedClipTypes) {
            this.supportedClipTypes = supportedClipTypes;
            return this;
        }

        public Builder withEffectType(TimelineEffectType effectType) {
            this.effectType = effectType;
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

        public Builder withRestoreFactory(BiFunction<JsonNode, LoadMetadata, StatelessEffect> restoreFactory) {
            this.restoreFactory = restoreFactory;
            return this;
        }

        public Builder withIsFullWidth(boolean isFullWidth) {
            this.isFullWidth = isFullWidth;
            return this;
        }

        public StandardEffectFactory build() {
            return new StandardEffectFactory(this);
        }
    }

}
