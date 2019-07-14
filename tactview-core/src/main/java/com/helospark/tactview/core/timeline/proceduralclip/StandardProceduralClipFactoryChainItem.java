package com.helospark.tactview.core.timeline.proceduralclip;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;

public class StandardProceduralClipFactoryChainItem implements ProceduralClipFactoryChainItem {
    private String proceduralEffectId;
    private String name;
    private Function<AddClipRequest, ProceduralVisualClip> creator;
    private BiFunction<JsonNode, LoadMetadata, ProceduralVisualClip> restore;

    public StandardProceduralClipFactoryChainItem(String proceduralEffectId, String name, Function<AddClipRequest, ProceduralVisualClip> creator,
            BiFunction<JsonNode, LoadMetadata, ProceduralVisualClip> restore) {
        this.proceduralEffectId = proceduralEffectId;
        this.creator = creator;
        this.name = name;
        this.restore = restore;
    }

    @Override
    public ProceduralVisualClip create(AddClipRequest request) {
        ProceduralVisualClip result = creator.apply(request);
        return result;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return Objects.equals(request.getProceduralClipId(), proceduralEffectId);
    }

    @Override
    public String getProceduralClipName() {
        return name;
    }

    @Override
    public String getProceduralClipId() {
        return proceduralEffectId;
    }

    @Override
    public ProceduralVisualClip restoreClip(JsonNode node, LoadMetadata loadMetadata) {
        return restore.apply(node, loadMetadata);
    }

    @Override
    public String getId() {
        return proceduralEffectId;
    }

}
