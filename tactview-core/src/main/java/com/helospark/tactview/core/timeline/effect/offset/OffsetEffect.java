package com.helospark.tactview.core.timeline.effect.offset;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class OffsetEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider xOffsetProvider;
    private DoubleProvider yOffsetProvider;

    public OffsetEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public OffsetEffect(OffsetEffect offsetEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(offsetEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(offsetEffect, this, cloneRequestMetadata);
    }

    public OffsetEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double offsetXValue = xOffsetProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()) % 1.0;
        double offsetYValue = yOffsetProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()) % 1.0;
        int offsetX = (int) (offsetXValue * request.getCanvasWidth());
        int offsetY = (int) (offsetYValue * request.getCanvasHeight());

        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        ClipImage result = ClipImage.sameSizeAs(currentFrame);
        int width = result.getWidth();
        int height = result.getHeight();

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int newX = remapPixel(x, offsetX, width);
            int newY = remapPixel(y, offsetY, height);

            int[] color = new int[4];
            currentFrame.getPixelComponents(color, newX, newY);
            result.setPixel(color, x, y);
        });

        return result;
    }

    private int remapPixel(int position, int offset, int max) {
        int remapped = (position - offset);
        if (remapped < 0) {
            remapped = max + remapped;
        }
        if (remapped >= max) {
            remapped = remapped - max;
        }
        return remapped;
    }

    @Override
    protected void initializeValueProviderInternal() {
        xOffsetProvider = new DoubleProvider(-1.0, 1.0, new BezierDoubleInterpolator(0.5));
        yOffsetProvider = new DoubleProvider(-1.0, 1.0, new BezierDoubleInterpolator(0.5));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor xOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xOffsetProvider)
                .withName("X offset")
                .build();

        ValueProviderDescriptor yOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yOffsetProvider)
                .withName("Y offset")
                .build();

        return Arrays.asList(xOffsetProviderDescriptor, yOffsetProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new OffsetEffect(this, cloneRequestMetadata);
    }

}
