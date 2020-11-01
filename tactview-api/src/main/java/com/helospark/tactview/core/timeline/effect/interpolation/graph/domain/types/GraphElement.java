package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class GraphElement {
    private static final TypeReference<Map<ConnectionIndex, GraphConnectionDescriptor>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    public double x, y; // display logic, eventually move to UI module

    private String factoryId;

    public Map<ConnectionIndex, GraphConnectionDescriptor> inputs = new LinkedHashMap<>();
    public Map<ConnectionIndex, GraphConnectionDescriptor> outputs = new LinkedHashMap<>();

    public abstract Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request);

    public GraphElement() {
    }

    public GraphElement(JsonNode data, LoadMetadata loadMetadata) {
        this.x = data.get("x").asDouble();
        this.y = data.get("y").asDouble();
        this.inputs = loadMetadata.getObjectMapperUsed().convertValue(data.get("inputs"), MAP_TYPE_REFERENCE);
        this.outputs = loadMetadata.getObjectMapperUsed().convertValue(data.get("outputs"), MAP_TYPE_REFERENCE);
    }

    public Map<String, Object> serialize(SaveMetadata saveMetadata) {
        Map<String, Object> result = new HashMap<>();
        result.put("x", x);
        result.put("y", y);
        result.put("inputs", inputs);
        result.put("outputs", outputs);
        result.put("factoryId", factoryId);

        serializeInternal(result, saveMetadata);

        return result;
    }

    protected abstract void serializeInternal(Map<String, Object> result, SaveMetadata saveMetadata);

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Map<ConnectionIndex, GraphConnectionDescriptor> getInputs() {
        return inputs;
    }

    public Map<ConnectionIndex, GraphConnectionDescriptor> getOutputs() {
        return outputs;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public abstract GraphElement deepClone(GraphElementCloneRequest cloneRequst);

    protected void copyCommonPropertiesTo(GraphElement element, GraphElementCloneRequest cloneRequest) {
        element.x = this.x;
        element.y = this.y;
        element.inputs = this.inputs.entrySet().stream().collect(Collectors.toMap(a -> cloneRequest.remap(a.getKey()), a -> a.getValue()));
        element.outputs = this.outputs.entrySet().stream().collect(Collectors.toMap(a -> cloneRequest.remap(a.getKey()), a -> a.getValue()));
    }

    public String getFactoryId() {
        return factoryId;
    }

    public static class GraphElementCloneRequest {
        Map<GraphIndex, GraphIndex> remappedGraphIds;
        Map<ConnectionIndex, ConnectionIndex> remappedConnectionIds;

        public GraphElementCloneRequest(Map<GraphIndex, GraphIndex> remappedGraphIds, Map<ConnectionIndex, ConnectionIndex> remappedConnectionIds) {
            this.remappedGraphIds = remappedGraphIds;
            this.remappedConnectionIds = remappedConnectionIds;
        }

        public ConnectionIndex remap(ConnectionIndex index) {
            return this.remappedConnectionIds.get(index);
        }

        public GraphIndex remap(GraphIndex index) {
            return this.remappedGraphIds.get(index);
        }
    }

}
