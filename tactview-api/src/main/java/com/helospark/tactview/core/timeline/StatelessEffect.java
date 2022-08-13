package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.StaticObjectMapper;

public abstract class StatelessEffect implements EffectAware, IntervalAware, IntervalSettable {
    protected String id;
    protected String factoryId;
    protected TimelineInterval interval;
    protected IntervalAware parentIntervalAware;

    protected BooleanProvider enabledProvider;

    public StatelessEffect(TimelineInterval interval) {
        id = UUID.randomUUID().toString();
        this.interval = interval;
    }

    public StatelessEffect(StatelessEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, StatelessEffect.class, cloneRequestMetadata);

        this.interval = effect.interval;
        this.factoryId = effect.factoryId;
        if (cloneRequestMetadata.isDeepCloneId()) {
            this.id = effect.id;
        } else {
            this.id = cloneRequestMetadata.generateOrGetIdFromPrevious(effect.id);
        }
    }

    public StatelessEffect(JsonNode node, LoadMetadata loadMetadata) {
        this.id = node.get("id").asText();
        this.interval = StaticObjectMapper.toValue(node, loadMetadata, "interval", TimelineInterval.class);
        this.factoryId = node.get("factoryId").asText();
    }

    public Object generateSavedContent(SaveMetadata saveMetadata) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", id);
        result.put("interval", interval);
        result.put("factoryId", factoryId);

        Map<String, Object> saveableFields = new LinkedHashMap<>();
        ReflectionUtil.collectSaveableFields(this, saveableFields);
        result.put("savedFields", saveableFields);

        generateSavedContentInternal(result, saveMetadata);

        return result;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public String getId() {
        return id;
    }

    public void initializeValueProvider() {
        initializeValueProviderInternal();
        enabledProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    protected abstract void initializeValueProviderInternal();

    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor enabledProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(enabledProvider)
                .withName("Enabled")
                .build();

        List<ValueProviderDescriptor> result = new ArrayList<>();
        result.add(enabledProviderDescriptor);
        result.addAll(getValueProvidersInternal());

        return result;
    }

    protected abstract List<ValueProviderDescriptor> getValueProvidersInternal();

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

    public void preDestroy() {
    }

    public Set<String> getClipDependency(TimelinePosition position) {
        return new HashSet<>();
    }

    public abstract StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata);

    protected void generateSavedContentInternal(Map<String, Object> result, SaveMetadata saveMetadata) {
        // clients can optionally override if necessary
    }

    protected List<String> getChannelDependency(TimelinePosition position) {
        return List.of();
    }

    public boolean isEnabledAt(TimelinePosition position) {
        return enabledProvider.getValueWithoutScriptAt(position); // TODO: evaluation context
    }

}
