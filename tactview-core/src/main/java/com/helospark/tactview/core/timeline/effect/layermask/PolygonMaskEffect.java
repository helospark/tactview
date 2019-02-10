package com.helospark.tactview.core.timeline.effect.layermask;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskAlphaToAlpha;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.PolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.PolygonRenderService.PolygonRenderServiceRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class PolygonMaskEffect extends StatelessVideoEffect {
    private PolygonProvider polygonProvider;
    private DoubleProvider fuzzyProvider;

    private PolygonRenderService polygonRenderService;
    private LayerMaskApplier layerMaskApplier;
    private LayerMaskAlphaToAlpha layerMaskAlphaToAlpha;

    public PolygonMaskEffect(TimelineInterval interval, PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(interval);
        this.polygonRenderService = polygonRenderService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    public PolygonMaskEffect(JsonNode node, LoadMetadata loadMetadata, PolygonRenderService polygonRenderService2, LayerMaskApplier layerMaskApplier2,
            LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(node, loadMetadata);
        this.polygonRenderService = polygonRenderService2;
        this.layerMaskApplier = layerMaskApplier2;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    public PolygonMaskEffect(PolygonMaskEffect polygonMaskEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(polygonMaskEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(polygonMaskEffect, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        int fuzzyEdge = (int) (fuzzyProvider.getValueAt(request.getEffectPosition()) * request.getCurrentFrame().getWidth());
        Polygon polygon = polygonProvider.getValueAt(request.getEffectPosition());

        if (polygon.getPoints().size() > 2) {

            PolygonRenderServiceRequest polygonDrawerServiceRequest = PolygonRenderServiceRequest.builder()
                    .withColor(Color.of(1.0, 1.0, 1.0))
                    .withExpectedWidth(request.getCurrentFrame().getWidth())
                    .withExpectedHeight(request.getCurrentFrame().getHeight())
                    .withFuzzyEdge(fuzzyEdge)
                    .withPolygon(polygon)
                    .build();
            ReadOnlyClipImage layerMask = polygonRenderService.drawPolygon(polygonDrawerServiceRequest);

            LayerMaskApplyRequest layerMaskApplyRequest = LayerMaskApplyRequest.builder()
                    .withCalculator(layerMaskAlphaToAlpha)
                    .withCurrentFrame(request.getCurrentFrame())
                    .withInvert(false)
                    .withMask(layerMask)
                    .withScaleLayerMask(false)
                    .build();
            ClipImage result = layerMaskApplier.createNewImageWithLayerMask(layerMaskApplyRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(layerMask.getBuffer());
            return result;
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }
    }

    @Override
    public void initializeValueProvider() {
        polygonProvider = new PolygonProvider(List.of());
        fuzzyProvider = new DoubleProvider(0.0, 0.3, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor polygonProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(polygonProvider)
                .withName("polygon")
                .build();
        ValueProviderDescriptor fuzzyEdgeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fuzzyProvider)
                .withName("Fuzzy edge")
                .build();

        return List.of(polygonProviderDescriptor, fuzzyEdgeDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PolygonMaskEffect(this, cloneRequestMetadata);
    }

}
