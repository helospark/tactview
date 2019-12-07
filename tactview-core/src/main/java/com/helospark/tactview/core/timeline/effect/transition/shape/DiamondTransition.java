package com.helospark.tactview.core.timeline.effect.transition.shape;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskBetweenTwoImageApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskAlphaToAlpha;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderServiceRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DiamondTransition extends AbstractVideoTransitionEffect {
    private PolygonRenderService polygonRenderService;
    private LayerMaskApplier layerMaskApplier;
    private LayerMaskAlphaToAlpha layerMaskAlphaToAlpha;

    private DoubleProvider fuzzinessProvider;

    public DiamondTransition(TimelineInterval interval, PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(interval);
        this.polygonRenderService = polygonRenderService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    public DiamondTransition(DiamondTransition whiteFlashTransition, CloneRequestMetadata cloneRequestMetadata) {
        super(whiteFlashTransition, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(whiteFlashTransition, this);
    }

    public DiamondTransition(JsonNode node, LoadMetadata loadMetadata, PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier,
            LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        super(node, loadMetadata);
        this.polygonRenderService = polygonRenderService;
        this.layerMaskApplier = layerMaskApplier;
        this.layerMaskAlphaToAlpha = layerMaskAlphaToAlpha;
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();

        int w = transitionRequest.getFirstFrame().getWidth();
        int h = transitionRequest.getFirstFrame().getHeight();

        double fuzziness = fuzzinessProvider.getValueAt(transitionRequest.getEffectPosition());
        double t = progress + fuzziness;

        List<Point> points = new ArrayList<>();

        Point center = new Point(0.5, 0.5);

        points.add(center.add(-t, 0));
        points.add(center.add(0, -t));
        points.add(center.add(t, 0));
        points.add(center.add(0, t));

        Polygon polygon = new Polygon(points);

        PolygonRenderServiceRequest polygonRequest = PolygonRenderServiceRequest.builder()
                .withColor(new Color(1.0, 1.0, 1.0))
                .withExpectedWidth(w)
                .withExpectedHeight(h)
                .withFuzzyEdge((int) (fuzziness * w))
                .withPolygon(polygon)
                .build();

        ReadOnlyClipImage gradientImage = polygonRenderService.drawPolygon(polygonRequest);

        LayerMaskBetweenTwoImageApplyRequest layerMaskRequest = LayerMaskBetweenTwoImageApplyRequest.builder()
                .withBottomFrame(transitionRequest.getFirstFrame())
                .withTopFrame(transitionRequest.getSecondFrame())
                .withCalculator(layerMaskAlphaToAlpha)
                .withMask(gradientImage)
                .build();

        ClipImage result = layerMaskApplier.mergeTwoImageWithLayerMask(layerMaskRequest);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(gradientImage.getBuffer());

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new DiamondTransition(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        super.initializeValueProviderInternal();

        fuzzinessProvider = new DoubleProvider(0.0, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.05));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = super.getValueProvidersInternal();

        ValueProviderDescriptor fuzzyDescriptor = ValueProviderDescriptor.builder()
                .withName("Fuzzy border")
                .withKeyframeableEffect(fuzzinessProvider)
                .build();

        result.add(fuzzyDescriptor);

        return result;
    }

}
