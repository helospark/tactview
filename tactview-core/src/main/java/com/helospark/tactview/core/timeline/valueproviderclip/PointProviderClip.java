package com.helospark.tactview.core.timeline.valueproviderclip;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.proceduralclip.valueprovider.AbstractValueProviderClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class PointProviderClip extends AbstractValueProviderClip {
    private PointProvider pointProvider;

    public PointProviderClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public PointProviderClip(AbstractValueProviderClip clipToCopy, CloneRequestMetadata cloneRequestMetadata) {
        super(clipToCopy, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(clipToCopy, this, cloneRequestMetadata);
    }

    public PointProviderClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    protected void provideValues(GetFrameRequest request, TimelinePosition relativePosition) {
        Point point = pointProvider.getValueAt(relativePosition, request.getEvaluationContext());
        request.addDynamic(getId(), "point", point);
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new PointProviderClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        pointProvider = PointProvider.ofNormalizedImagePosition(0.5, 0.5);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

        ValueProviderDescriptor pointProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(pointProvider)
                .withName("Point")
                .build();

        result.add(pointProviderDescriptor);

        return result;
    }

}
