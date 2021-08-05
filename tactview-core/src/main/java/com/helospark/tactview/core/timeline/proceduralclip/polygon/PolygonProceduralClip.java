package com.helospark.tactview.core.timeline.proceduralclip.polygon;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderServiceRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class PolygonProceduralClip extends ProceduralVisualClip {
    private PolygonProvider polygonProvider;
    private DoubleProvider fuzzyEdgeProvider;
    private ColorProvider colorProvider;

    private PolygonRenderService polygonRenderService;

    public PolygonProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, PolygonRenderService polygonRenderService) {
        super(visualMediaMetadata, interval);
        this.polygonRenderService = polygonRenderService;
    }

    public PolygonProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, PolygonRenderService polygonRenderService) {
        super(metadata, node, loadMetadata);
        this.polygonRenderService = polygonRenderService;
    }

    public PolygonProceduralClip(PolygonProceduralClip polygonProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(polygonProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(polygonProceduralClip, this, cloneRequestMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        int fuzzyEdge = (int) (fuzzyEdgeProvider.getValueAt(relativePosition) * request.getExpectedWidth());
        Polygon polygon = polygonProvider.getValueAt(relativePosition);
        Color color = colorProvider.getValueAt(relativePosition);

        PolygonRenderServiceRequest serviceRequest = PolygonRenderServiceRequest.builder()
                .withColor(color)
                .withExpectedHeight(request.getExpectedHeight())
                .withExpectedWidth(request.getExpectedWidth())
                .withFuzzyEdge(fuzzyEdge)
                .withPolygon(polygon)
                .build();
        ReadOnlyClipImage result = polygonRenderService.drawPolygon(serviceRequest);

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        this.polygonProvider = new PolygonProvider(List.of());
        this.fuzzyEdgeProvider = new DoubleProvider(0.0, 0.3, new MultiKeyframeBasedDoubleInterpolator(0.0));
        this.colorProvider = new ColorProvider(createColorComponentProvider(1.0),
                createColorComponentProvider(1.0),
                createColorComponentProvider(1.0));
    }

    private DoubleProvider createColorComponentProvider(double defaultValue) {
        return new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor polygonProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(polygonProvider)
                .withName("polygon")
                .build();
        ValueProviderDescriptor fuzzyEdgeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fuzzyEdgeProvider)
                .withName("Fuzzy edge")
                .build();
        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("Color")
                .build();

        result.add(polygonProviderDescriptor);
        result.add(fuzzyEdgeDescriptor);
        result.add(colorProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new PolygonProceduralClip(this, cloneRequestMetadata);
    }

}
