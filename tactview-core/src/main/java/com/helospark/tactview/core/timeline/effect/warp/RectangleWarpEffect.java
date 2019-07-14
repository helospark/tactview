package com.helospark.tactview.core.timeline.effect.warp;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.RectangleProvider;
import com.helospark.tactview.core.timeline.effect.warp.rasterizer.Simple2DRasterizer;
import com.helospark.tactview.core.timeline.effect.warp.rasterizer.SimpleTriangle;
import com.helospark.tactview.core.timeline.effect.warp.rasterizer.SimpleVertex;
import com.helospark.tactview.core.timeline.effect.warp.rasterizer.TriangleRasterizationRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RectangleWarpEffect extends StatelessVideoEffect {
    private Simple2DRasterizer simple2DRasterizer;

    private RectangleProvider rectangleProvider;

    public RectangleWarpEffect(TimelineInterval interval, Simple2DRasterizer simple2DRasterizer) {
        super(interval);
        this.simple2DRasterizer = simple2DRasterizer;
    }

    public RectangleWarpEffect(RectangleWarpEffect rectangleWarpEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(rectangleWarpEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(rectangleWarpEffect, this);
    }

    public RectangleWarpEffect(JsonNode node, LoadMetadata loadMetadata, Simple2DRasterizer simple2dRasterizer) {
        super(node, loadMetadata);
        this.simple2DRasterizer = simple2dRasterizer;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ClipImage result = ClipImage.fromSize(request.getCanvasWidth(), request.getCanvasHeight());
        Rectangle rectangle = rectangleProvider.getValueAt(request.getEffectPosition());

        List<Point> points = rectangle.points
                .stream()
                .map(a -> a.multiply(request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight()))
                .collect(Collectors.toList());

        SimpleVertex a = SimpleVertex.builder()
                .withColor(Color.of(1.0, 1.0, 1.0))
                .withPosition(new Vector2D(points.get(0).x, points.get(0).y))
                .withTextureCoordinates(new Vector2D(0, 0))
                .build();
        SimpleVertex b = SimpleVertex.builder()
                .withColor(Color.of(1.0, 1.0, 1.0))
                .withPosition(new Vector2D(points.get(1).x, points.get(1).y))
                .withTextureCoordinates(new Vector2D(1, 0))
                .build();
        SimpleVertex c = SimpleVertex.builder()
                .withColor(Color.of(1.0, 1.0, 1.0))
                .withPosition(new Vector2D(points.get(2).x, points.get(2).y))
                .withTextureCoordinates(new Vector2D(1, 1))
                .build();
        SimpleVertex d = SimpleVertex.builder()
                .withColor(Color.of(1.0, 1.0, 1.0))
                .withPosition(new Vector2D(points.get(3).x, points.get(3).y))
                .withTextureCoordinates(new Vector2D(0, 1))
                .build();

        drawTriangle(request, result, a, c, b);
        drawTriangle(request, result, a, d, c);

        return result;
    }

    private void drawTriangle(StatelessEffectRequest request, ClipImage result, SimpleVertex a, SimpleVertex b, SimpleVertex c) {
        SimpleTriangle simpleTriangle = new SimpleTriangle(a, b, c, request.getCurrentFrame());
        TriangleRasterizationRequest rasterizationRequest = new TriangleRasterizationRequest(result, simpleTriangle);
        simple2DRasterizer.rasterizeTriangle(rasterizationRequest);
    }

    @Override
    public void initializeValueProvider() {
        rectangleProvider = RectangleProvider.createDefaultFullImageWithNormalizedPosition();
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor rectangleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rectangleProvider)
                .withName("Rectangle")
                .build();

        return List.of(rectangleDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new RectangleWarpEffect(this, cloneRequestMetadata);
    }

}
