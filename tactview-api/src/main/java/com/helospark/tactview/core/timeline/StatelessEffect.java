package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.util.StaticObjectMapper;

public abstract class StatelessEffect implements EffectAware, IntervalAware, IntervalSettable {
    protected String id;
    protected String factoryId;
    protected TimelineInterval interval;
    protected IntervalAware parentIntervalAware;

    public StatelessEffect(TimelineInterval interval) {
        id = UUID.randomUUID().toString();
        this.interval = interval;
    }

    public StatelessEffect(StatelessEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        if (cloneRequestMetadata.isDeepCloneId()) {
            this.id = effect.id;
        } else {
            this.id = UUID.randomUUID().toString();
        }
        this.interval = effect.interval;
        this.factoryId = effect.factoryId;
    }

    public StatelessEffect(JsonNode node, LoadMetadata loadMetadata) {
        this.id = node.get("id").asText();
        this.interval = StaticObjectMapper.toValue(node, loadMetadata, "interval", TimelineInterval.class);
        this.factoryId = node.get("factoryId").asText();
    }

    public Object generateSavedContent() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", id);
        result.put("interval", interval);
        result.put("factoryId", factoryId);

        Map<String, Object> saveableFields = new LinkedHashMap<>();
        ReflectionUtil.collectSaveableFields(this, saveableFields);
        result.put("savedFields", saveableFields);

        generateSavedContentInternal(result);

        return result;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getId() {
        return id;
    }

    public abstract void initializeValueProvider();

    public abstract List<ValueProviderDescriptor> getValueProviders();

    @Override
    public void setInterval(TimelineInterval timelineInterval) {
        this.interval = timelineInterval;
    }

    public void setParentIntervalAware(IntervalAware parentIntervalAware) {
        this.parentIntervalAware = parentIntervalAware;
    }

    @Override
    public TimelineInterval getGlobalInterval() {
        return this.interval.butAddOffset(parentIntervalAware.getInterval().getStartPosition());
    }

    public void notifyAfterResize() {
    }

    public void notifyAfterInitialized() {

    }

    public List<String> getClipDependency(TimelinePosition position) {
        return new ArrayList<>();
    }

    public abstract StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata);

    protected void generateSavedContentInternal(Map<String, Object> result) {
        // clients can optionally override if necessary
    }

    protected List<String> getChannelDependency(TimelinePosition position) {
        return List.of();
    }

}
