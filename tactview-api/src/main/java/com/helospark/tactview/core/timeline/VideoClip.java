package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class VideoClip extends VisualTimelineClip {
    protected VisualMediaMetadata mediaMetadata;
    protected TimelinePosition startPosition;

    protected Optional<LowResolutionProxyData> lowResolutionProxySource = Optional.empty(); // We could also have multiple proxies

    public VideoClip(VisualMediaMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(mediaMetadata, new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    public VideoClip(VideoClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.mediaMetadata = clip.mediaMetadata;
        this.backingSource = clip.backingSource;
        this.startPosition = clip.startPosition;
    }

    public VideoClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
        this.mediaMetadata = metadata;
        this.backingSource = videoSource;
        this.startPosition = StaticObjectMapper.toValue(savedClip, loadMetadata, "startPosition", TimelinePosition.class);
        // savedClip.get("shouldHaveLowResolutionProxy").asBoolean(false); // TODO
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        super.generateSavedContentInternal(savedContent);
        savedContent.put("startPosition", startPosition);
        savedContent.put("shouldHaveLowResolutionProxy", lowResolutionProxySource.isPresent());
    }

    @Override
    public ByteBuffer requestFrame(RequestFrameParameter frameRequest) {
        VisualMediaMetadata metadataToUse;
        VisualMediaSource mediaSourceToUse;

        if (frameRequest.isLowResolutionPreview() && lowResolutionProxySource.isPresent()) {
            metadataToUse = lowResolutionProxySource.get().mediaMetadata;
            mediaSourceToUse = lowResolutionProxySource.get().source;
        } else {
            metadataToUse = mediaMetadata;
            mediaSourceToUse = backingSource;
        }

        VideoMediaDataRequest request = VideoMediaDataRequest.builder()
                .withFilePath(mediaSourceToUse.backingFile)
                .withHeight(frameRequest.getHeight())
                .withWidth(frameRequest.getWidth())
                .withMetadata(metadataToUse)
                .withStart(frameRequest.getPosition())
                .withUseApproximatePosition(frameRequest.useApproximatePosition())
                .build();
        return mediaSourceToUse.decoder.readFrames(request)
                .getFrame();
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public VisualMediaSource getBackingSource() {
        return backingSource;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    public boolean containsLowResolutionProxy() {
        return lowResolutionProxySource.isPresent();
    }

    public void setLowResolutionProxy(LowResolutionProxyData proxyData) {
        this.lowResolutionProxySource = Optional.ofNullable(proxyData);
    }

    @Override
    public boolean isResizable() {
        return mediaMetadata.isResizable();
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new VideoClip(this, cloneRequestMetadata);
    }

    public static class LowResolutionProxyData {
        VisualMediaSource source;
        VisualMediaMetadata mediaMetadata;

        public LowResolutionProxyData(VisualMediaSource source, VisualMediaMetadata mediaMetadata) {
            this.source = source;
            this.mediaMetadata = mediaMetadata;
        }

    }

    @Override
    public String toString() {
        return "VideoClip [mediaMetadata=" + mediaMetadata + ", startPosition=" + startPosition + ", lowResolutionProxySource=" + lowResolutionProxySource + ", getBackingSource()="
                + getBackingSource() + "]";
    }

}
