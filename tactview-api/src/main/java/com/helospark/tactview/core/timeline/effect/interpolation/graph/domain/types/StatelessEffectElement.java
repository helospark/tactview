package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphAcceptType;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class StatelessEffectElement extends GraphElement {
    ConnectionIndex inputIndex = ConnectionIndex.random();
    ConnectionIndex outputIndex = ConnectionIndex.random();
    StatelessVideoEffect effect;

    List<ConnectionIndex> additionalClipInideces = new ArrayList<>(1);

    public StatelessEffectElement(StatelessVideoEffect effect) {
        this.effect = effect;

        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Input", GraphAcceptType.IMAGE));
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));

        for (var desc : effect.getValueProviders()) {
            if (desc.getKeyframeableEffect() instanceof DependentClipProvider) {
                ConnectionIndex connectionIndex = ConnectionIndex.random();
                ((DependentClipProvider) desc.getKeyframeableEffect()).keyframeAdded(TimelinePosition.ofZero(), connectionIndex.getId());
                this.inputs.put(connectionIndex, new GraphConnectionDescriptor(desc.getName(), GraphAcceptType.IMAGE));
                additionalClipInideces.add(connectionIndex);
            }
        }

    }

    public StatelessEffectElement(ConnectionIndex inputIndex, ConnectionIndex outputIndex, StatelessVideoEffect effect) {
        this.effect = effect;
        this.inputIndex = inputIndex;
        this.outputIndex = outputIndex;

        this.inputs.put(inputIndex, new GraphConnectionDescriptor("Input", GraphAcceptType.IMAGE));
        this.outputs.put(outputIndex, new GraphConnectionDescriptor("Output", GraphAcceptType.IMAGE));
    }

    @Override
    public Map<ConnectionIndex, ReadOnlyClipImage> render(Map<ConnectionIndex, ReadOnlyClipImage> images, EffectGraphInputRequest request) {

        ReadOnlyClipImage inputImage = images.get(inputIndex);

        if (inputImage == null) {
            System.out.println("No input image for " + inputIndex);
            inputImage = ClipImage.fromSize(request.expectedWidth, request.expectedHeight);
        }

        Map<String, ReadOnlyClipImage> additionalClips = new LinkedHashMap<>();
        for (var additionalClipIndex : additionalClipInideces) {
            ReadOnlyClipImage providedImage = images.get(additionalClipIndex);
            if (providedImage != null) {
                additionalClips.put(additionalClipIndex.getId(), providedImage);
            }
        }

        StatelessEffectRequest effectRequest = StatelessEffectRequest.builder()
                .withCanvasHeight(request.expectedWidth)
                .withCanvasHeight(request.expectedHeight)
                .withClipPosition(request.position)
                .withCurrentFrame(inputImage)
                .withEffectChannel(0)
                .withEffectPosition(request.position)
                .withScale(request.scale)
                .withRequestedClips(additionalClips)
                .build();

        ReadOnlyClipImage result = effect.createFrameExternal(effectRequest);

        return Map.of(outputIndex, result);
    }

    public StatelessVideoEffect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "StatelessEffectElement [inputIndex=" + inputIndex + ", outputIndex=" + outputIndex + ", effect=" + effect + ", x=" + x + ", y=" + y + ", inputs=" + inputs + ", outputs=" + outputs
                + "]";
    }

    @Override
    public GraphElement deepClone() {
        StatelessEffectElement result = new StatelessEffectElement(inputIndex, outputIndex, (StatelessVideoEffect) this.effect.cloneEffect(CloneRequestMetadata.ofDefault()));
        copyCommonPropertiesTo(result);
        return result;
    }
}
