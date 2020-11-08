package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphNodeOutputMarker;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.ImageToLoopbackRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.OpencvL4V2LoopbackImplementation;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class CameraOutputToV4L2LoopbackElement extends GraphElement implements GraphNodeOutputMarker {
    private OpencvL4V2LoopbackImplementation loopbackImplementation;
    private ConnectionIndex input;

    private ValueListProvider<ValueListElement> devicesProvider;

    public CameraOutputToV4L2LoopbackElement(OpencvL4V2LoopbackImplementation loopbackImplementation) {
        input = ConnectionIndex.random();
        this.inputs.put(input, new GraphConnectionDescriptor("Input", GraphAcceptType.IMAGE));
        this.loopbackImplementation = loopbackImplementation;
    }

    public CameraOutputToV4L2LoopbackElement(JsonNode data, LoadMetadata metadata, OpencvL4V2LoopbackImplementation loopbackImplementation) {
        super(data, metadata);
        this.input = new ConnectionIndex(data.get("input").asText());
        this.loopbackImplementation = loopbackImplementation;
    }

    public CameraOutputToV4L2LoopbackElement(OpencvL4V2LoopbackImplementation loopbackImplementation, ConnectionIndex input) {
        this.input = input;
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
    public GraphElement deepClone(GraphElementCloneRequest cloneRequst) {
        return new CameraOutputToV4L2LoopbackElement(loopbackImplementation, cloneRequst.remap(this.input));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        List<ValueProviderDescriptor> result = super.getDescriptors();

        File devFolder = new File("/dev");
        List<ValueListElement> videoDevices = Arrays.stream(devFolder.listFiles())
                .filter(a -> a.getName().startsWith("video"))
                .map(a -> new ValueListElement(a.getAbsolutePath(), a.getAbsolutePath()))
                .collect(Collectors.toList());

        String defaultDevice = videoDevices.size() > 0 ? videoDevices.get(0).getId() : "";
        devicesProvider = new ValueListProvider<>(videoDevices, new StepStringInterpolator(defaultDevice));

        ValueProviderDescriptor descriptor = ValueProviderDescriptor.builder()
                .withName("Loopback device")
                .withKeyframeableEffect(devicesProvider)
                .withShowPredicate(a -> videoDevices.size() > 0)
                .build();

        result.add(descriptor);

        return result;
    }

    @Override
    protected void serializeInternal(Map<String, Object> result, SaveMetadata saveMetadata) {
        result.put("input", input.getId());
    }

}
