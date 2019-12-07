package com.helospark.tactview.core.timeline.effect.histogramequization;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.histogramequization.opencv.OpenCVHistogramEquizationRequest;
import com.helospark.tactview.core.timeline.effect.histogramequization.opencv.OpenCVHistogramEquizerImplementation;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class HistogramEquizationEffect extends StatelessVideoEffect {
    private OpenCVHistogramEquizerImplementation implementation;

    private BooleanProvider greyscaleProvider;
    private BooleanProvider adaptiveProvider;

    private IntegerProvider adaptiveClipLimitProvider;
    private IntegerProvider adaptiveKernelWidthProvider;
    private IntegerProvider adaptiveKernelHeightProvider;

    public HistogramEquizationEffect(TimelineInterval interval, OpenCVHistogramEquizerImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public HistogramEquizationEffect(HistogramEquizationEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public HistogramEquizationEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVHistogramEquizerImplementation implementation2) {
        super(node, loadMetadata);
        this.implementation = implementation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        OpenCVHistogramEquizationRequest nativeRequest = new OpenCVHistogramEquizationRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.grayscale = greyscaleProvider.getValueAt(request.getEffectPosition()) ? 1 : 0;
        nativeRequest.adaptive = adaptiveProvider.getValueAt(request.getEffectPosition()) ? 1 : 0;
        nativeRequest.adaptiveKernelWidth = adaptiveKernelWidthProvider.getValueAt(request.getEffectPosition());
        nativeRequest.adaptiveKernelHeight = adaptiveKernelHeightProvider.getValueAt(request.getEffectPosition());
        nativeRequest.adaptiveClipLimit = adaptiveClipLimitProvider.getValueAt(request.getEffectPosition());

        implementation.equizeHistogram(nativeRequest);

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        greyscaleProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        adaptiveProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));

        adaptiveClipLimitProvider = new IntegerProvider(1, 100, new MultiKeyframeBasedDoubleInterpolator(10.0));
        adaptiveKernelWidthProvider = new IntegerProvider(1, 20, new MultiKeyframeBasedDoubleInterpolator(8.0));
        adaptiveKernelHeightProvider = new IntegerProvider(1, 20, new MultiKeyframeBasedDoubleInterpolator(8.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor grayscaleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(greyscaleProvider)
                .withName("Grayscale")
                .build();

        ValueProviderDescriptor adaptiveDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(adaptiveProvider)
                .withName("Adaptive")
                .build();

        ValueProviderDescriptor adaptiveClipLimitDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(adaptiveClipLimitProvider)
                .withName("Adaptive clip limit")
                .build();

        ValueProviderDescriptor adaptiveKernelWidthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(adaptiveKernelWidthProvider)
                .withName("Adaptive kernel width")
                .build();

        ValueProviderDescriptor adaptiveKernelHeightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(adaptiveKernelHeightProvider)
                .withName("Adaptive kernel height")
                .build();

        return List.of(grayscaleDescriptor, adaptiveDescriptor, adaptiveClipLimitDescriptor, adaptiveKernelWidthDescriptor, adaptiveKernelHeightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new HistogramEquizationEffect(this, cloneRequestMetadata);
    }

}
