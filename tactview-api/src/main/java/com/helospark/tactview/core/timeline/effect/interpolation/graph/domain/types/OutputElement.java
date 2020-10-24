package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class OutputElement extends GraphElement {
    ConnectionIndex inputIndex = ConnectionIndex.random();

    ReadOnlyClipImage result = null;

    public OutputElement() {
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
    }

    OutputElement(ConnectionIndex inputIndex) {
        this.inputIndex = inputIndex;
        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Result", GraphAcceptType.IMAGE));
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {
        ReadOnlyClipImage destinationImage = images.get(inputIndex);
        if (destinationImage != null) {
            result = ClipImage.copyOf(destinationImage);
        }
        return Map.of();
    }

    public ConnectionIndex getInputIndex() {
        return inputIndex;
    }

    public ReadOnlyClipImage getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "OutputElement [inputIndex=" + inputIndex + ", result=" + result + ", x=" + x + ", y=" + y + ", inputs=" + inputs + ", outputs=" + outputs + "]";
    }

    @Override
    public GraphElement deepClone() {
        OutputElement result = new OutputElement(inputIndex);
        copyCommonPropertiesTo(result);
        return result;
    }

}
