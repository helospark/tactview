package com.helospark.tactview.core.timeline.effect.erodedilate;

import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateImplementation;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;

public class ErodeDilateEffect extends StatelessVideoEffect {
    private DoubleProvider kernelWidthProvider;
    private DoubleProvider kernelHeightProvider;
    private ValueListProvider<ValueListElement> erodeOrDelodeProvider;
    private ValueListProvider<IdIdableValueListElement> shapeProvider;

    private OpenCVErodeDilateImplementation implementation;

    public ErodeDilateEffect(TimelineInterval interval, OpenCVErodeDilateImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        ClipFrameResult result = ClipFrameResult.sameSizeAs(currentFrame);
        int kernelWidth = (int) (kernelWidthProvider.getValueAt(request.getEffectPosition()) * currentFrame.getWidth());
        int kernelHeight = (int) (kernelHeightProvider.getValueAt(request.getEffectPosition()) * currentFrame.getHeight());
        boolean erode = erodeOrDelodeProvider.getValueAt(request.getEffectPosition()).getId().equals("erode");
        int shape = shapeProvider.getValueAt(request.getEffectPosition()).intId;

        OpenCVErodeDilateRequest nativeRequest = new OpenCVErodeDilateRequest();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.kernelWidth = kernelWidth;
        nativeRequest.kernelHeight = kernelHeight;
        nativeRequest.erode = erode;
        nativeRequest.shape = shape;

        implementation.erodeDilate(nativeRequest);

        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        kernelWidthProvider = new DoubleProvider(1 / 4000.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(10.0 / 1920));
        kernelHeightProvider = new DoubleProvider(1 / 4000.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(10.0 / 1080));
        erodeOrDelodeProvider = new ValueListProvider<>(createErodeOrDilate(), new StringInterpolator("dilate"));
        shapeProvider = new ValueListProvider<>(createShape(), new StringInterpolator("ellipse"));

        ValueProviderDescriptor kernelWidthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("Kernel width")
                .build();
        ValueProviderDescriptor kernelHeightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("Kernel height")
                .build();
        ValueProviderDescriptor typeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(erodeOrDelodeProvider)
                .withName("Operation")
                .build();
        ValueProviderDescriptor shapeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shapeProvider)
                .withName("Shape")
                .build();

        return List.of(kernelWidthDescriptor, kernelHeightDescriptor, typeDescriptor, shapeDescriptor);
    }

    private List<IdIdableValueListElement> createShape() {
        IdIdableValueListElement cross = new IdIdableValueListElement("cross", "cross", 0);
        IdIdableValueListElement rectangle = new IdIdableValueListElement("rectangle", "rectangle", 1);
        IdIdableValueListElement ellipse = new IdIdableValueListElement("ellipse", "ellipse", 2);

        return List.of(cross, rectangle, ellipse);
    }

    private List<ValueListElement> createErodeOrDilate() {
        ValueListElement erode = new ValueListElement("erode", "erode");
        ValueListElement dilate = new ValueListElement("dilate", "dilate");
        return List.of(erode, dilate);
    }

    static class IdIdableValueListElement extends ValueListElement {
        private Integer intId;

        public IdIdableValueListElement(String id, String text, Integer intId) {
            super(id, text);
            this.intId = intId;
        }

    }

}
