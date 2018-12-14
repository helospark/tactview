package com.helospark.tactview.core.timeline.proceduralclip;

import java.nio.ByteBuffer;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class ProceduralVisualClip extends VisualTimelineClip {
    private String proceduralFactoryId;

    public ProceduralVisualClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval, TimelineClipType.IMAGE);
    }

    public ProceduralVisualClip(ProceduralVisualClip proceduralVisualClip) {
        super(proceduralVisualClip);
    }

    public ProceduralVisualClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        // TODO something is very wrong here
        throw new IllegalStateException();
    }

    public abstract ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition);

    @Override
    public ReadOnlyClipImage getFrameInternal(GetFrameRequest request) {
        TimelinePosition relativePosition = request.calculateRelativePositionFrom(this);
        ReadOnlyClipImage result = createProceduralFrame(request, relativePosition);
        return applyEffects(relativePosition, result, request);
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        savedContent.put("proceduralFactoryId", proceduralFactoryId);
    }

    public String getProceduralFactoryId() {
        return proceduralFactoryId;
    }

    public void setProceduralFactoryId(String proceduralFactoryId) {
        this.proceduralFactoryId = proceduralFactoryId;
    }

}
