package com.helospark.tactview.core.timeline.proceduralclip;

import java.util.Objects;
import java.util.function.Function;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.LayerMaskApplier;
import com.helospark.tactview.core.timeline.VisualTimelineClip;

public class StandardProceduralClipFactoryChainItem implements ProceduralClipFactoryChainItem {
    private String proceduralEffectId;
    private String name;
    private Function<AddClipRequest, VisualTimelineClip> creator;

    private LayerMaskApplier layerMaskApplier;

    public StandardProceduralClipFactoryChainItem(LayerMaskApplier layerMaskApplier, String proceduralEffectId, String name, Function<AddClipRequest, VisualTimelineClip> creator) {
        this.proceduralEffectId = proceduralEffectId;
        this.creator = creator;
        this.name = name;
        this.layerMaskApplier = layerMaskApplier;
    }

    @Override
    public VisualTimelineClip create(AddClipRequest request) {
        VisualTimelineClip result = creator.apply(request);
        result.setLayerMaskApplier(layerMaskApplier);
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

}
