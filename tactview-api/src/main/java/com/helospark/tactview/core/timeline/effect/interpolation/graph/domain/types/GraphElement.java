package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class GraphElement {
    public double x, y; // display logic, eventually move to UI module

    public Map<ConnectionIndex, GraphConnectionDescriptor> inputs = new LinkedHashMap<>();
    public Map<ConnectionIndex, GraphConnectionDescriptor> outputs = new LinkedHashMap<>();

    public abstract Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request);

    public Map<String, Object> serialize() {
        return Map.of();
    }

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

    public abstract GraphElement deepClone();

    protected void copyCommonPropertiesTo(GraphElement element) {
        element.x = this.x;
        element.y = this.y;
        element.inputs = this.inputs.entrySet().stream().collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue())); // TODO: new id
        element.outputs = this.outputs.entrySet().stream().collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue()));
    }

}
