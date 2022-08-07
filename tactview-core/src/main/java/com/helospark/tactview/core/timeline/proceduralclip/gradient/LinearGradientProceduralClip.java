package com.helospark.tactview.core.timeline.proceduralclip.gradient;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientRequest;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientService;
import com.helospark.tactview.core.util.ReflectionUtil;

public class LinearGradientProceduralClip extends ProceduralVisualClip {
    private LinearGradientService linearGradientService;

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

    private LineProvider lineProvider;

    public LinearGradientProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, LinearGradientService linearGradientService) {
        super(visualMediaMetadata, interval);
        this.linearGradientService = linearGradientService;
    }

    public LinearGradientProceduralClip(LinearGradientProceduralClip linearProceduralEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(linearProceduralEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(linearProceduralEffect, this, cloneRequestMetadata);
    }

    public LinearGradientProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, LinearGradientService linearGradientService) {
        super(metadata, node, loadMetadata);
        this.linearGradientService = linearGradientService;
    }

    @Override
    public ClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        InterpolationLine line = lineProvider.getValueAt(relativePosition, request.getEvaluationContext());

        Color startColor = startColorProvider.getValueAt(relativePosition, request.getEvaluationContext());
        Color endColor = endColorProvider.getValueAt(relativePosition, request.getEvaluationContext());

        LinearGradientRequest linearGradientRequest = LinearGradientRequest.builder()
                .withStartColor(startColor)
                .withEndColor(endColor)
                .withNormalizedLine(line)
                .withWidth(request.getExpectedWidth())
                .withHeight(request.getExpectedHeight())
                .build();

        return linearGradientService.render(linearGradientRequest);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        startColorProvider = createColorProvider(0.0, 0.0, 0.0);
        endColorProvider = createColorProvider(1.0, 1.0, 1.0);

        PointProvider topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.0), doubleProviderWithDefaultValue(0.0));
        PointProvider bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(1.0), doubleProviderWithDefaultValue(0.0));
        lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("Start color")
                .build();
        ValueProviderDescriptor endColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endColorProvider)
                .withName("End color")
                .build();
        ValueProviderDescriptor lineProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("Postion")
                .build();

        result.addAll(List.of(startColorDescriptor, endColorDescriptor, lineProviderDescriptor));
        return result;
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new LinearGradientProceduralClip(this, cloneRequestMetadata);
    }

}
