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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BezierPolygonProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskAlphaToAlpha;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonRenderServiceRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class BezierMaskEffect extends StatelessVideoEffect {
    private BezierPolygonProvider polygonProvider;
    private DoubleProvider fuzzyProvider;
    private BooleanProvider invertProvider;
    private BooleanProvider layerMaskEnabledProvider;

    private BezierPolygonRenderService bezierPolygonRenderService;
    private LayerMaskApplier layerMaskApplier;
    private LayerMaskAlphaToAlpha layerMaskAlphaToAlpha;

    public BezierMaskEffect(TimelineInterval interval, BezierPolygonRenderService bezierPolygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(interval);
        this.bezierPolygonRenderService = bezierPolygonRenderService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    public BezierMaskEffect(JsonNode node, LoadMetadata loadMetadata, BezierPolygonRenderService bezierPolygonRenderService, LayerMaskApplier layerMaskApplier2,
            LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(node, loadMetadata);
        this.bezierPolygonRenderService = bezierPolygonRenderService;
        this.layerMaskApplier = layerMaskApplier2;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    public BezierMaskEffect(BezierMaskEffect polygonMaskEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(polygonMaskEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(polygonMaskEffect, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        int fuzzyEdge = (int) (fuzzyProvider.getValueAt(request.getEffectPosition()) * request.getCurrentFrame().getWidth());
        BezierPolygon polygon = polygonProvider.getValueAt(request.getEffectPosition()).multiplyPoints(new Point(request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight()));
        boolean invert = invertProvider.getValueAt(request.getEffectPosition());
        boolean layerMaskEnabled = layerMaskEnabledProvider.getValueAt(request.getEffectPosition());

        if (polygon.getPoints().size() > 2 && layerMaskEnabled) {

            BezierPolygonRenderServiceRequest polygonDrawerServiceRequest = BezierPolygonRenderServiceRequest.builder()
                    .withColor(Color.of(1.0, 1.0, 1.0))
                    .withExpectedWidth(request.getCurrentFrame().getWidth())
                    .withExpectedHeight(request.getCurrentFrame().getHeight())
                    .withFuzzyEdge(fuzzyEdge)
                    .withPolygon(polygon)
                    .build();
            ReadOnlyClipImage layerMask = bezierPolygonRenderService.drawBezierPolygon(polygonDrawerServiceRequest);

            LayerMaskApplyRequest layerMaskApplyRequest = LayerMaskApplyRequest.builder()
                    .withCalculator(layerMaskAlphaToAlpha)
                    .withCurrentFrame(request.getCurrentFrame())
                    .withInvert(invert)
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
        invertProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        layerMaskEnabledProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        polygonProvider = new BezierPolygonProvider(List.of());
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
        ValueProviderDescriptor invertProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(invertProvider)
                .withName("Invert")
                .build();
        ValueProviderDescriptor layerMaskEnabledDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(layerMaskEnabledProvider)
                .withName("Layer mask enabled")
                .build();

        return List.of(layerMaskEnabledDescriptor, polygonProviderDescriptor, fuzzyEdgeDescriptor, invertProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new BezierMaskEffect(this, cloneRequestMetadata);
    }

}
