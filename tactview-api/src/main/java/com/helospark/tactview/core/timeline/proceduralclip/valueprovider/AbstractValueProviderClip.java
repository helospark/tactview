package com.helospark.tactview.core.timeline.proceduralclip.valueprovider;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class AbstractValueProviderClip extends ProceduralVisualClip {

    public AbstractValueProviderClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public AbstractValueProviderClip(AbstractValueProviderClip clipToCopy, CloneRequestMetadata cloneRequestMetadata) {
        super(clipToCopy, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(clipToCopy, this, cloneRequestMetadata);
    }

    public AbstractValueProviderClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        provideValues(request, relativePosition);
        return ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
    }

    protected abstract void provideValues(GetFrameRequest request, TimelinePosition relativePosition);

}
