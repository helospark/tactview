package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class InputElement extends GraphElement {
    ConnectionIndex outputIndex = ConnectionIndex.random();

    public InputElement() {
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));
    }

    InputElement(ConnectionIndex outputIndex) {
        this.outputIndex = outputIndex;
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));
    }

    public InputElement(JsonNode data, LoadMetadata metadata) {
        super(data, metadata);
        this.outputIndex = new ConnectionIndex(data.get("outputIndex").asText());
    }

    @Override
    protected void serializeInternal(Map<String, Object> result, SaveMetadata saveMetadata) {
        result.put("outputIndex", outputIndex.getId());
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {
        if (request.input != null) {
            return Map.of(outputIndex, ClipImage.copyOf(request.input));
        } else {
            return Map.of();
        }
    }

    public ConnectionIndex getOutputIndex() {
        return outputIndex;
    }

    @Override
    public String toString() {
        return "InputElement [outputIndex=" + outputIndex + ", x=" + x + ", y=" + y + ", inputs=" + inputs + ", outputs=" + outputs + "]";
    }

    @Override
    public GraphElement deepClone() {
        InputElement result = new InputElement(outputIndex);
        copyCommonPropertiesTo(result);
        return result;
    }

}
