package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class OutputElement extends GraphElement implements GraphNodeOutputMarker {
    ConnectionIndex inputIndex = ConnectionIndex.random();

    public OutputElement() {
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
    }

    OutputElement(ConnectionIndex inputIndex) {
        this.inputIndex = inputIndex;
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
    }

    public OutputElement(JsonNode data, LoadMetadata metadata) {
        super(data, metadata);
        this.inputIndex = new ConnectionIndex(data.get("inputIndex").asText());
    }

    @Override
    protected void serializeInternal(Map<String, Object> result, SaveMetadata saveMetadata) {
        result.put("inputIndex", inputIndex.getId());
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {
        return Map.of();
    }

    public ConnectionIndex getInputIndex() {
        return inputIndex;
    }

    @Override
    public String toString() {
        return "OutputElement [inputIndex=" + inputIndex + "]";
    }

    @Override
    public GraphElement deepClone(GraphElementCloneRequest cloneRequest) {
        OutputElement result = new OutputElement(cloneRequest.remap(inputIndex));
        copyCommonPropertiesTo(result, cloneRequest);
        return result;
    }

}
