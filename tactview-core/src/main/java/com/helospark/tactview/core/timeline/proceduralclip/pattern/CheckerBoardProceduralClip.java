package com.helospark.tactview.core.timeline.proceduralclip.pattern;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class CheckerBoardProceduralClip extends ProceduralVisualClip {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider xScaleProvider;
    private DoubleProvider yScaleProvider;

    private DoubleProvider xOffsetProvider;
    private DoubleProvider yOffsetProvider;

    private ColorProvider color1Provider;
    private ColorProvider color2Provider;

    public CheckerBoardProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public CheckerBoardProceduralClip(CheckerBoardProceduralClip checkerBoardProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(checkerBoardProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(checkerBoardProceduralClip, this);
    }

    public CheckerBoardProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {

        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        int width = result.getWidth();
        int height = result.getHeight();

        double xOffset = xOffsetProvider.getValueAt(relativePosition);
        double yOffset = yOffsetProvider.getValueAt(relativePosition);

        double xScale = xScaleProvider.getValueAt(relativePosition);
        double yScale = yScaleProvider.getValueAt(relativePosition);

        Color color1 = color1Provider.getValueAt(relativePosition);
        Color color2 = color2Provider.getValueAt(relativePosition);

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            double normalizedX = (double) x / width + xOffset;
            double normalizedY = (double) y / width + yOffset;

            int xType = (int) Math.floor(normalizedX / xScale);
            int yType = (int) Math.floor(normalizedY / yScale);

            Color colorToUse;

            if ((Math.abs(yType % 2) == Math.abs(xType % 2))) {
                colorToUse = color1;
            } else {
                colorToUse = color2;
            }

            result.setRed((int) (colorToUse.red * 255.0), x, y);
            result.setGreen((int) (colorToUse.green * 255.0), x, y);
            result.setBlue((int) (colorToUse.blue * 255.0), x, y);
            result.setAlpha(255, x, y);
        });
        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new CheckerBoardProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        xScaleProvider = new DoubleProvider(0.001, 10.0, new MultiKeyframeBasedDoubleInterpolator(0.05));
        yScaleProvider = new DoubleProvider(0.001, 10.0, new MultiKeyframeBasedDoubleInterpolator(0.05));

        xOffsetProvider = new DoubleProvider(-10.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        yOffsetProvider = new DoubleProvider(-10.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(0.0));

        color1Provider = ColorProvider.fromDefaultValue(0, 0, 0);
        color2Provider = ColorProvider.fromDefaultValue(1, 1, 1);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor xScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xScaleProvider)
                .withName("X scale")
                .build();
        ValueProviderDescriptor yScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yScaleProvider)
                .withName("Y scale")
                .build();
        ValueProviderDescriptor xOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xOffsetProvider)
                .withName("X offset")
                .build();
        ValueProviderDescriptor yOffsetProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yOffsetProvider)
                .withName("Y Offset")
                .build();
        ValueProviderDescriptor color1ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(color1Provider)
                .withName("Color 1")
                .build();
        ValueProviderDescriptor color2ProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(color2Provider)
                .withName("Color 2")
                .build();

        result.add(xScaleProviderDescriptor);
        result.add(yScaleProviderDescriptor);
        result.add(xOffsetProviderDescriptor);
        result.add(yOffsetProviderDescriptor);
        result.add(color1ProviderDescriptor);
        result.add(color2ProviderDescriptor);

        return result;
    }

}
