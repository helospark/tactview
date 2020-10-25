package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphNodeOutputMarker;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.ImageToLoopbackRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.OpencvL4V2LoopbackImplementation;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class CameraOutputToV4L2LoopbackElement extends GraphElement implements GraphNodeOutputMarker {
    private OpencvL4V2LoopbackImplementation loopbackImplementation;
    private ConnectionIndex input;

    public CameraOutputToV4L2LoopbackElement(OpencvL4V2LoopbackImplementation loopbackImplementation) {
        input = ConnectionIndex.random();
        this.inputs.put(input, new GraphConnectionDescriptor("Input", GraphAcceptType.IMAGE));
        this.loopbackImplementation = loopbackImplementation;
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {
        ReadOnlyClipImage image = images.get(input);
        if (image != null) {
            ImageToLoopbackRequest nativeRequest = new ImageToLoopbackRequest();
            nativeRequest.loopbackDevice = "/dev/video3";
            nativeRequest.width = image.getWidth();
            nativeRequest.height = image.getHeight();
            nativeRequest.image = image.getBuffer();

            synchronized (loopbackImplementation) {
                this.loopbackImplementation.sendImageToLoopbackCamera(nativeRequest);
            }
        }
        return Map.of();
    }

    @Override
    public GraphElement deepClone() {
        return null;
    }

}
