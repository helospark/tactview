package com.helospark.tactview.core.timeline.effect.distort;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ReflectionUtil;

// Logic adapted from https://github.com/lbalazscs/Pixelitor
public class GlassTilesEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider sizeXProvider;
    private DoubleProvider sizeYProvider;

    private PointProvider shiftProvider;

    private DoubleProvider curvatureXProvider;
    private DoubleProvider curvatureYProvider;

    public GlassTilesEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public GlassTilesEffect(GlassTilesEffect lensDistortEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lensDistortEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lensDistortEffect, this, cloneRequestMetadata);
    }

    public GlassTilesEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage frame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(frame);

        int width = frame.getWidth();
        int height = frame.getHeight();

        double sizeX = sizeXProvider.getValueAt(request.getEffectPosition()) / request.getScale();
        double sizeY = sizeYProvider.getValueAt(request.getEffectPosition()) / request.getScale();

        Point shift = shiftProvider.getValueAt(request.getEffectPosition()).multiply(request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight());

        double curvatureX = curvatureXProvider.getValueAt(request.getEffectPosition()) * request.getScale();
        double curvatureY = curvatureYProvider.getValueAt(request.getEffectPosition()) * request.getScale();

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            double mappedX = MathUtil.clamp(x + (curvatureX * FastMath.tan(x * sizeX - shift.x)), 0, frame.getWidth() - 1);
            double mappedY = MathUtil.clamp(y + (curvatureY * FastMath.tan(y * sizeY + shift.y)), 0, frame.getHeight() - 1);

            for (int i = 0; i < 4; ++i) {
                int colorValue = frame.getColorComponentWithOffsetUsingInterpolation(mappedX, mappedY, i);
                result.setColorComponentByOffset(colorValue, x, y, i);
            }
        });

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        sizeXProvider = new DoubleProvider(0.0, 0.02, new MultiKeyframeBasedDoubleInterpolator(0.004));
        sizeYProvider = new DoubleProvider(0.0, 0.02, new MultiKeyframeBasedDoubleInterpolator(0.006));

        shiftProvider = PointProvider.ofNormalizedImagePosition(0, 0);

        curvatureXProvider = new DoubleProvider(0.01, 100.0, new MultiKeyframeBasedDoubleInterpolator(5.0));
        curvatureYProvider = new DoubleProvider(0.01, 100.0, new MultiKeyframeBasedDoubleInterpolator(5.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor sizeXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sizeXProvider)
                .withName("Size X")
                .build();
        ValueProviderDescriptor sizeYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sizeYProvider)
                .withName("Size Y")
                .build();
        ValueProviderDescriptor shiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shiftProvider)
                .withName("Shift")
                .build();
        ValueProviderDescriptor curvatureXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(curvatureXProvider)
                .withName("Curvature X")
                .build();
        ValueProviderDescriptor curvatureYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(curvatureYProvider)
                .withName("Curvature Y")
                .build();

        return List.of(sizeXProviderDescriptor, sizeYProviderDescriptor, shiftProviderDescriptor, curvatureXProviderDescriptor, curvatureYProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlassTilesEffect(this, cloneRequestMetadata);
    }

}
