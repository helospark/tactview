package com.helospark.tactview.core.timeline.proceduralclip.audio;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;

public class StandardProceduralAudioFactoryChainItem implements ProceduralAudioClipFactoryChainItem {
    private final String proceduralEffectId;
    private final String name;
    private final Function<AddClipRequest, ProceduralAudioClip> creator;
    private final BiFunction<JsonNode, LoadMetadata, ProceduralAudioClip> restore;

    public StandardProceduralAudioFactoryChainItem(String proceduralEffectId, String name, Function<AddClipRequest, ProceduralAudioClip> creator,
            BiFunction<JsonNode, LoadMetadata, ProceduralAudioClip> restore) {
        this.proceduralEffectId = proceduralEffectId;
        this.creator = creator;
        this.name = name;
        this.restore = restore;
    }

    @Override
    public ProceduralAudioClip create(AddClipRequest request) {
        ProceduralAudioClip result = creator.apply(request);
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
    public ProceduralAudioClip restoreClip(JsonNode node, LoadMetadata loadMetadata) {
        return restore.apply(node, loadMetadata);
    }

    @Override
    public String getId() {
        return proceduralEffectId;
    }

}
