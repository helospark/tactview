package com.helospark.tactview.core.timeline.proceduralclip;

import java.util.Objects;
import java.util.function.Function;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;

public class StandardProceduralClipFactoryChainItem implements ProceduralClipFactoryChainItem {
    private String proceduralEffectId;
    private String name;
    private Function<AddClipRequest, TimelineClip> creator;

    public StandardProceduralClipFactoryChainItem(String proceduralEffectId, String name, Function<AddClipRequest, TimelineClip> creator) {
        this.proceduralEffectId = proceduralEffectId;
        this.creator = creator;
        this.name = name;
    }

    @Override
    public TimelineClip create(AddClipRequest request) {
        return creator.apply(request);
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
