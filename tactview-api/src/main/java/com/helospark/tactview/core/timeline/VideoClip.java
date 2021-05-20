package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;
import com.helospark.tactview.core.timeline.effect.rotate.RotateServiceRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class VideoClip extends VisualTimelineClip {
    private RotateService rotateService;

    protected VisualMediaMetadata mediaMetadata;
    protected TimelinePosition startPosition;

    protected BooleanProvider useRotationMetadataProvider;

    protected Optional<LowResolutionProxyData> lowResolutionProxySource = Optional.empty(); // We could also have multiple proxies

    public VideoClip(VisualMediaMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length, RotateService rotateService) {
        super(mediaMetadata, new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
        this.rotateService = rotateService;
    }

    public VideoClip(VideoClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.mediaMetadata = clip.mediaMetadata;
        this.backingSource = clip.backingSource;
        this.startPosition = clip.startPosition;
        this.rotateService = clip.rotateService;
    }

    public VideoClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata, RotateService rotateService) {
        super(metadata, savedClip, loadMetadata);
        this.mediaMetadata = metadata;
        this.backingSource = videoSource;
        this.startPosition = StaticObjectMapper.toValue(savedClip, loadMetadata, "startPosition", TimelinePosition.class);
        this.rotateService = rotateService;
        // savedClip.get("shouldHaveLowResolutionProxy").asBoolean(false); // TODO
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        super.generateSavedContentInternal(savedContent, saveMetadata);
        savedContent.put("startPosition", startPosition);
        savedContent.put("shouldHaveLowResolutionProxy", lowResolutionProxySource.isPresent());
    }

    @Override
    public ReadOnlyClipImage requestFrame(RequestFrameParameter frameRequest) {
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
        ByteBuffer frame = mediaSourceToUse.decoder.readFrames(request)
                .getFrame();

        ClipImage result = new ClipImage(frame, frameRequest.getWidth(), frameRequest.getHeight());
        if (isRotationEnabledAt(frameRequest.getPosition()) && !MathUtil.fuzzyEquals(getRotationFromMetadata(), 0.0)) {
            RotateServiceRequest serviceRequest = RotateServiceRequest.builder()
                    .withAngle(getRotationFromMetadata())
                    .withImage(result)
                    .withCenterX(0.5)
                    .withCenterY(0.5)
                    .build();

            ClipImage rotatedImage = rotateService.rotateExactSize(serviceRequest);
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(result.getBuffer());
            result = rotatedImage;
        }

        return result;
    }

    public Boolean isRotationEnabledAt(TimelinePosition position) {
        return useRotationMetadataProvider.getValueAt(position);
    }

    private double getRotationFromMetadata() {
        if (mediaMetadata instanceof VideoMetadata) {
            return ((VideoMetadata) mediaMetadata).getRotation();
        } else {
            return 0.0;
        }
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
        return true;
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

    // this is really a cut instead of a resize, but on UI it shows the exact same way as resize
    // maximum size a clip can be is the size of the videoclip
    @Override
    public TimelineInterval getIntervalAfterRescaleTo(boolean left, TimelinePosition position) {
        if (mediaMetadata instanceof VideoMetadata) {
            return intervalAfterResizeAsCut(left, position, mediaMetadata.getLength(), reverseTimeProvider.getValueAt(TimelinePosition.ofZero()));
        } else {
            return super.getIntervalAfterRescaleTo(left, position);
        }
    }

    @Override
    public void resize(boolean left, TimelineInterval position) {
        if (mediaMetadata instanceof VideoMetadata) {
            resizeAsCut(left, position);
        } else {
            super.resize(left, position);
        }
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        useRotationMetadataProvider = new BooleanProvider(new BezierDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor useRotationMetadataProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(useRotationMetadataProvider)
                .withName("Use rotation metadata")
                .withGroup("common")
                .build();
        result.add(useRotationMetadataProviderDescriptor);

        return result;
    }

}
