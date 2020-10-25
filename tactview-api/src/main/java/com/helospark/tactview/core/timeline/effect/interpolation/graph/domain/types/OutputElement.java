package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class OutputElement extends GraphElement {
    ConnectionIndex inputIndex = ConnectionIndex.random();

    public OutputElement() {
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
    }

    OutputElement(ConnectionIndex inputIndex) {
        this.inputIndex = inputIndex;
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
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
    public GraphElement deepClone() {
        OutputElement result = new OutputElement(inputIndex);
        copyCommonPropertiesTo(result);
        return result;
    }

}
