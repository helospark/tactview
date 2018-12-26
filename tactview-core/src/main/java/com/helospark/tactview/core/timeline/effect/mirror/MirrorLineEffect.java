package com.helospark.tactview.core.timeline.effect.mirror;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class MirrorLineEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private LineProvider lineProvider;

    public MirrorLineEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MirrorLineEffect(MirrorLineEffect mirrorLineEffect) {
        super(mirrorLineEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(mirrorLineEffect, this);
    }

    public MirrorLineEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {

        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        InterpolationLine line = lineProvider.getValueAt(request.getEffectPosition());

        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();

        Point start = line.start.multiply(width, height);
        Point end = line.end.multiply(width, height);

        int x1 = (int) start.x;
        int x2 = (int) end.x;
        int y1 = (int) start.y;
        int y2 = (int) end.y;

        int a = (y1 - y2);
        int b = (x2 - x1);
        int c = (x1 * y2 - x2 * y1);

        ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            double tmp = -2.0 * (a * x + b * y + c) / (a * a + b * b);

            if (tmp < 0) {
                int newX = (int) (tmp * a + x);
                int newY = (int) (tmp * b + y);
                copyColorFrom(currentFrame, result, x, y, newX, newY);
            } else {
                copyColorFrom(currentFrame, result, x, y, x, y);
            }
        });

        return result;
    }

    private void copyColorFrom(ReadOnlyClipImage currentFrame, ClipImage result, int x, int y, int newX, int newY) {
        if (currentFrame.inBounds(newX, newY)) {
            for (int i = 0; i < 4; ++i) {
                int current = currentFrame.getColorComponentWithOffset(newX, newY, i);
                result.setColorComponentByOffset(current, x, y, i);
            }
        }
    }

    @Override
    public void initializeValueProvider() {
        lineProvider = new LineProvider(createPointProvider(0.5, 0.0), createPointProvider(0.5, 1.0));
    }

    private PointProvider createPointProvider(double defaultX, double defaultY) {
        return new PointProvider(createDoubleProvider(defaultX), createDoubleProvider(defaultY));
    }

    private DoubleProvider createDoubleProvider(double defaultX) {
        return new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultX));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Mirror line")
                .build();

        return List.of(heightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new MirrorLineEffect(this);
    }

}
