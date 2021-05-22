package com.helospark.tactview.core.timeline.proceduralclip;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.RequestFrameParameter;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class ProceduralVisualClip extends VisualTimelineClip {
    private String proceduralFactoryId;

    private DoubleProvider widthMultiplierProvider;
    private DoubleProvider heightMultiplierProvider;

    public ProceduralVisualClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval, TimelineClipType.IMAGE);
    }

    public ProceduralVisualClip(ProceduralVisualClip proceduralVisualClip, CloneRequestMetadata cloneRequestMetadata) {
        super(proceduralVisualClip, cloneRequestMetadata);
        this.widthMultiplierProvider = proceduralVisualClip.widthMultiplierProvider.deepClone();
        this.heightMultiplierProvider = proceduralVisualClip.heightMultiplierProvider.deepClone();
        this.proceduralFactoryId = proceduralVisualClip.proceduralFactoryId;
    }

    public ProceduralVisualClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage requestFrame(RequestFrameParameter request) {
        // TODO something is very wrong here
        throw new IllegalStateException();
    }

    public abstract ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition);

    @Override
    public ReadOnlyClipImage getFrameInternal(GetFrameRequest request) {
        TimelinePosition relativePosition = calculatePositionToRender(request);

        double widthMultiplier = widthMultiplierProvider.getValueAt(relativePosition);
        double heightMultiplier = heightMultiplierProvider.getValueAt(relativePosition);

        GetFrameRequest newFrameRequest = GetFrameRequest.builderFrom(request)
                .withExpectedWidth((int) (widthMultiplier * request.getExpectedWidth()))
                .withExpectedHeight((int) (heightMultiplier * request.getExpectedHeight()))
                .build();

        ReadOnlyClipImage result = createProceduralFrame(newFrameRequest, relativePosition);
        return applyEffects(relativePosition, result, newFrameRequest);
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
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        savedContent.put("proceduralFactoryId", proceduralFactoryId);
    }

    public String getProceduralFactoryId() {
        return proceduralFactoryId;
    }

    public void setProceduralFactoryId(String proceduralFactoryId) {
        this.proceduralFactoryId = proceduralFactoryId;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        widthMultiplierProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        heightMultiplierProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor expectedWidthMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(widthMultiplierProvider)
                .withName("Width multiplier")
                .withGroup("procedural clip size")
                .build();
        ValueProviderDescriptor expectedHeightMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(heightMultiplierProvider)
                .withName("Height multiplier")
                .withGroup("procedural clip size")
                .build();

        result.add(expectedWidthMultiplierDescriptor);
        result.add(expectedHeightMultiplierDescriptor);

        return result;
    }
}
